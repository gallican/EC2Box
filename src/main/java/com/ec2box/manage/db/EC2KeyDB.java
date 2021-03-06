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
package com.ec2box.manage.db;

import com.ec2box.manage.model.EC2Key;
import com.ec2box.manage.model.SortedSet;
import com.ec2box.manage.util.DBUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class to manage private keys for AWS servers
 */
public class EC2KeyDB {


    public static final String SORT_BY_KEY_NM="key_nm";
    public static final String SORT_BY_EC2_REGION="ec2_region";

    /**
     * returns private key information for admin user
     *
     * @param adminId  id of the admin user
     * @param sortedSet object that defines sort order
     * @return sorted identity list
     */
    public static SortedSet getEC2KeySet(Long adminId,  SortedSet sortedSet) {

        List<EC2Key> ec2KeyList = new ArrayList<EC2Key>();


        String orderBy = "";
        if (sortedSet.getOrderByField() != null && !sortedSet.getOrderByField().trim().equals("")) {
            orderBy = "order by " + sortedSet.getOrderByField() + " " + sortedSet.getOrderByDirection();
        }


        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from ec2_keys where admin_id=? "+ orderBy);
            stmt.setLong(1, adminId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                EC2Key ec2Key = new EC2Key();
                ec2Key.setId(rs.getLong("id"));
                ec2Key.setAdminId(rs.getLong("admin_id"));
                ec2Key.setKeyNm(rs.getString("key_nm"));
                ec2Key.setEc2Region(rs.getString("ec2_region"));
                ec2KeyList.add(ec2Key);
            }

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);

        sortedSet.setItemList(ec2KeyList);


        return sortedSet;

    }






    /**
     * returns private key information for admin user
     *
     * @param adminId  id of the admin user
     * @param keyNm key name
     * @param ec2Region ec2 region
     * @return key information
     */
    public static EC2Key getEC2KeyByKeyNm(Long adminId, String keyNm, String ec2Region) {

        EC2Key ec2Key = null;

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from ec2_keys where admin_id=? and key_nm like ? and ec2_region like ?");
            stmt.setLong(1, adminId);
            stmt.setString(2, keyNm);
            stmt.setString(3, ec2Region);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ec2Key = new EC2Key();
                ec2Key.setId(rs.getLong("id"));
                ec2Key.setAdminId(rs.getLong("admin_id"));
                ec2Key.setKeyNm(rs.getString("key_nm"));
                ec2Key.setEc2Region(rs.getString("ec2_region"));
            }

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return ec2Key;

    }


    /**
     * returns private keys information for region and admin user
     *
     * @param adminId  id of the admin user
     * @param ec2Region ec2 region
     * @return key information
     */
    public static List<EC2Key> getEC2KeyByRegion(Long adminId, String ec2Region) {
        List<EC2Key> ec2KeyList = new ArrayList<EC2Key>();

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("select * from ec2_keys where admin_id=? and ec2_region like ?");
            stmt.setLong(1, adminId);
            stmt.setString(2, ec2Region);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                EC2Key ec2Key = new EC2Key();
                ec2Key.setId(rs.getLong("id"));
                ec2Key.setAdminId(rs.getLong("admin_id"));
                ec2Key.setKeyNm(rs.getString("key_nm"));
                ec2Key.setEc2Region(rs.getString("ec2_region"));
                ec2KeyList.add(ec2Key);
            }

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


        return ec2KeyList;

    }

    /**
     * inserts private key information for admin user
     *
     * @param adminId  id of the admin user
     * @param ec2Key private key information
     */
    public static Long insertEC2Key(Long adminId, EC2Key ec2Key) {

        Connection con = null;
        Long ec2KeyId=null;
        try {
            con = DBUtils.getConn();

            PreparedStatement stmt = con.prepareStatement("insert into ec2_keys (admin_id, key_nm, ec2_region) values (?,?,?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setLong(1, adminId);
            stmt.setString(2, ec2Key.getKeyNm());
            stmt.setString(3, ec2Key.getEc2Region());
            stmt.execute();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs != null && rs.next()) {
                ec2KeyId = rs.getLong(1);
            }

            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);
        return ec2KeyId;


    }





    /**
     * saves private key information for admin user
     *
     * @param adminId  id of the admin user
     * @param ec2Key private key information
     */
    public static Long saveEC2Key(Long adminId, EC2Key ec2Key) {

        Long ec2KeyId=null;

        //get id for key if exists
        EC2Key ec2KeyTmp = getEC2KeyByKeyNm(adminId,ec2Key.getKeyNm(), ec2Key.getEc2Region());
        if(ec2KeyTmp!=null){
            ec2KeyId=ec2KeyTmp.getId();
        //else insert if it doesn't exist
        }else{
            ec2KeyId=insertEC2Key(adminId,ec2Key);
        }
        ec2KeyTmp=null;

        return ec2KeyId;


    }




    /**
     * deletes private key information for admin user
     *
     * @param identityId db generated id for private key
     */
    public static void deleteEC2Key(Long identityId) {

        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement("delete from ec2_keys where id=?");
            stmt.setLong(1, identityId);
            stmt.execute();
            DBUtils.closeStmt(stmt);

        } catch (Exception e) {
            e.printStackTrace();
        }
        DBUtils.closeConn(con);


    }
}
