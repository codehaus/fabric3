Fabric3 Tutorials

I. Introduction

The tutorials are intended to demonstrate the capabilities of the Fabric3 runtime. The tutorials contain individual projects designed to showcase specific features:

* WebCalc - This tutorial demonstrates how to create a basic SCA application with a web UI. WebCalc is a simple calculator assembled from add, subtract, delete, and multiple services with a servlet-based UI. 

* BigBank Loan Application - This tutorial showcases advanced features of the Fabric3 runtime. It is intended to provide a complete, real-world application that demonstrates SCA and Fabric3 best-practices.

II. Prerequisites 

* The Fabric3 standalone runtime, downloadable from http://fabric3.codehaus.org/Downloads.
* The Fabric3 Tutorials distribution (this package)
* WebCalc and BigBank require the Web Profile, downloadable from http://repository.codehaus.org/org/codehaus/fabric3/profile-web/0.5.1/profile-web-0.5.1-bin.zip.
* BigBank requires the JPA profile, downloadable from http://repository.codehaus.org/org/codehaus/fabric3/profile-jpa/0.5.1/profile-jpa-0.5.1-bin.zip.
* BigBank requires the H2 database extension, downloadable from http://repository.codehaus.org/org/codehaus/fabric3/fabric3-db-h2/0.5.1/fabric3-db-h2-0.5.1.jar.
* The build system requires Maven 2.0.8 or later. It can be downloaded from http://maven.apache.org/download.html.

III. Runtime and Tutorial Installation

* Install the standalone runtime by unzipping the distribution to a directory.

* Install the Web and JPA profiles by unzipping the contents of the profile archives to the /extension and /host directories where the standalone runtime is installed.

* Install the H2 Database extension by copying it to the /user directory where the standalone runtime is installed (this was downloaded in a previous step).

* Unzip the tutorials distribution. It will contain the WebCalc and BigBank projects. 

IV. Building and Deploying WebCalc

* To build WebCalc, go to the webcalc project folder and execute: mvn clean install. Note internet access is required the first time the project is built so Maven can download the required project dependencies. Remote access can be turned off for subsequent builds by executing: mvn -o clean install.

* After the WebCalc project has been built, deploy it by copying the war from the /target output directory to the Fabric3 runtime /deploy directory. 

* Start the runtime from the /bin directory by executing: java -jar server.jar standalone. The runtime will write a message to the console after the war has been deployed. 

* The calculator UI can be accessed at http://localhost:8181/calculator/entry.html.

V. Building and Deploying BigBank

* To build BigBank, go to the bigbank project folder and execute: mvn -r clean install. The '-r' command is required so all BigBank modules will be built. Note internet access is required the first time the project is built so Maven can download the required project dependencies. Remote access can be turned off for subsequent builds by executing: mvn -o clean install.

* Install the BigBank datasource configuration by copying datasource.xml (located in /bigbank/config) to the /user directory where the standalone runtime is installed. 

* Copy the fabric3-tutorial-bigbank-0.5.1.jar (located in /bigbank/bigbank/target) and the fabric3-tutorial-bigbank-webclient-0.5.1.war (located in /bigbank/bigbank-webclient/target) archived to the Fabric3 /deploy directory. 

* Start the runtime from the /bin directory by executing: java -jar server.jar standalone.

* The BigBank UI can be accessed from http://localhost:8181/lending/applicationForm.jsp.   

V. Reporting Issues

* If you experience a problem or would like to suggest improvements, send a note to the user list (http://xircles.codehaus.org/projects/fabric3/lists) or file a JIRA issue (http://jira.codehaus.org/browse/FABRICTHREE). 


 
   


