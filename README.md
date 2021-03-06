EC2Box
======

About
-----
A web-based ssh terminal to execute commands and make changes to multiple EC2 sessions simultaneously running on Amazon Web Services (AWS).

Demo
-----
http://youtu.be/T4SBisCz91M

Prerequisites
-------------
SQLite3
http://www.sqlite.org/download.html

    sudo apt-get install sqlite3 sqlite3-dev 

**Should already be installed in Mac OS X v10.5 or greater

Java JDK 1.6 or greater
http://www.oracle.com/technetwork/java/javase/overview/index.html

Maven 3 or greater  ( Only needed if building from source )
http://maven.apache.org

To Run Bundled with Jetty
------
If your not big on the idea of building from source...

Download ec2box-jetty-vXX.XX.tar.gz

https://github.com/skavanagh/EC2Box/releases

Export environment variables

     export JAVA_HOME=/path/to/jdk
     export PATH=$JAVA_HOME/bin:$PATH

Start EC2Box

        ./startEC2Box.sh

To Build from Source 
------
Export environment variables

    export JAVA_HOME=/path/to/jdk
    export M2_HOME=/path/to/maven
    export PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH

In the directory that contains the pom.xml run

	mvn package jetty:run

**Note: Doing a mvn clean will delete the SQLite DB and wipe out all the data.

Using EC2Box
------
Open browser to http://localhost:8090

Login with 

	username:admin 
	password:changeme

Steps:

1. Set your AWS credentials
2. Import the private key used on your EC2 systems (note: The EC2 server will only show if the private key has been imported)
3. Set the EC2 regions of the residing systems
4. Start composite-ssh sessions or create and execute a script across multiple sessions


Author
------
**Sean Kavanagh** 

+ sean.p.kavanagh6@gmail.com
+ https://twitter.com/spkavanagh6


[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/0566eda40886c71548228fe00a8feed9 "githalytics.com")](http://githalytics.com/skavanagh/EC2Box)
