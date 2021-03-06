/**
 * Copyright 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ec2box.manage.action;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.ec2box.manage.db.*;
import com.ec2box.manage.model.*;
import com.ec2box.manage.util.AdminUtil;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Action to manage systems
 */
public class SystemAction extends ActionSupport implements ServletRequestAware {

    SortedSet sortedSet = new SortedSet();
    HostSystem hostSystem = new HostSystem();
    Script script = null;
    HttpServletRequest servletRequest;

    @Action(value = "/manage/viewSystems",
            results = {
                    @Result(name = "success", location = "/manage/view_systems.jsp")
            }
    )
    public String viewSystems() {

        Long adminId = AdminUtil.getAdminId(servletRequest);


        List<String> ec2RegionList = EC2RegionDB.getEC2Regions(adminId);


        try {
            //get AWS credentials from DB
            AWSCred awsCred = AWSCredDB.getAWSCred(adminId);

            if (awsCred != null) {
                //set  AWS credentials for service
                BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsCred.getAccessKey(), awsCred.getSecretKey());


                for (String ec2Region : ec2RegionList) {
                    //create service

                    AmazonEC2 service = new AmazonEC2Client(awsCredentials);
                    service.setEndpoint(ec2Region);

                    DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();

                    //only return systems that have keys set
                    List<String> keyValues = new ArrayList<String>();
                    for (EC2Key ec2Key : EC2KeyDB.getEC2KeyByRegion(adminId, ec2Region)) {
                        keyValues.add(ec2Key.getKeyNm());
                    }
                    Filter filter = new Filter("key-name", keyValues);

                    describeInstancesRequest.withFilters(filter);
                    DescribeInstancesResult describeInstancesResult = service.describeInstances(describeInstancesRequest);

                    List<HostSystem> hostSystemList = new ArrayList<HostSystem>();
                    for (Reservation res : describeInstancesResult.getReservations()) {
                        for (Instance instance : res.getInstances()) {

                            HostSystem hostSystem = new HostSystem();

                            //check to see if system exists
                            HostSystem existingSystem = SystemDB.getSystem(instance.getInstanceId(), adminId);
                            //set user if existing system
                            if (existingSystem != null) {
                                hostSystem.setUser(existingSystem.getUser());
                            }


                            hostSystem.setInstanceId(instance.getInstanceId());
                            hostSystem.setHost(instance.getPublicDnsName());
                            hostSystem.setKeyNm(instance.getKeyName());
                            hostSystem.setEc2Region(ec2Region);
                            hostSystem.setState(instance.getState().getName());
                            hostSystem.setAdminId(adminId);

                            for (Tag tag : instance.getTags()) {
                                if ("Name".equals(tag.getKey())) {
                                    hostSystem.setDisplayNm(tag.getValue());
                                }
                            }
                            hostSystemList.add(hostSystem);
                        }
                    }

                    //set ec2 systems
                    SystemDB.setSystems(hostSystemList, ec2Region, adminId);

                }
            }


        } catch (AmazonServiceException ex) {
            addActionError(ex.getMessage());

        }


        sortedSet = SystemDB.getSystemSet(sortedSet, adminId);
        if (script != null && script.getId() != null) {
            script = ScriptDB.getScript(script.getId(), adminId);
        }

        return SUCCESS;
    }

    @Action(value = "/manage/saveSystem",
            results = {
                    @Result(name = "input", location = "/manage/view_systems.jsp"),
                    @Result(name = "success", location = "/manage/viewSystems.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}&script.id=${script.id}", type = "redirect")
            }
    )
    public String saveSystem() {

        if (hostSystem.getId() != null) {
            hostSystem.setAdminId(AdminUtil.getAdminId(servletRequest));
            SystemDB.updateSystem(hostSystem);
        }
        return SUCCESS;
    }

    @Action(value = "/manage/deleteSystem",
            results = {
                    @Result(name = "success", location = "/manage/viewSystems.action?sortedSet.orderByDirection=${sortedSet.orderByDirection}&sortedSet.orderByField=${sortedSet.orderByField}", type = "redirect")
            }
    )
    public String deleteSystem() {

        if (hostSystem.getId() != null) {
            SystemDB.deleteSystem(hostSystem.getId(), AdminUtil.getAdminId(servletRequest));
        }
        return SUCCESS;
    }

    /**
     * Validates all fields for adding a host system
     */
    public void validateSaveSystem() {

        if (hostSystem == null
                || hostSystem.getUser() == null
                || hostSystem.getUser().trim().equals("")) {
            addFieldError("hostSystem.user", "Required");
        }

        if (!this.getFieldErrors().isEmpty()) {

            sortedSet = SystemDB.getSystemSet(sortedSet, AdminUtil.getAdminId(servletRequest));
        }

    }


    public HostSystem getHostSystem() {
        return hostSystem;
    }

    public void setHostSystem(HostSystem hostSystem) {
        this.hostSystem = hostSystem;
    }

    public SortedSet getSortedSet() {
        return sortedSet;
    }

    public void setSortedSet(SortedSet sortedSet) {
        this.sortedSet = sortedSet;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }
}
