Fabric3 Tutorials

I. Introduction

The tutorials are intended to demonstrate the capabilities of the Fabric3 runtime. The tutorials contain individual projects designed to showcase specific features:

* WebCalc - This tutorial demonstrates how to create a basic SCA application with a web UI. WebCalc is a simple calculator assembled from add, subtract, delete, and multiple services with a servlet-based UI. 

* BigBank Loan Application - This tutorial showcases advanced features of the Fabric3 runtime. It is intended to provide a complete, real-world application that demonstrates SCA and Fabric3 best-practices.

II. Getting Started

To get started, download the Fabric3 Standalone Runtime from http://fabric3.codehaus.org/Downloads and follow the installation directions. Maven 2.0.8 or later is also required to build the tutorials and can be downloaded from http://maven.apache.org/download.html. Once the Fabric3 runtime and Maven have been installed, build and deploy the desired application:

* To build WebCalc, go to the webcalc folder and execute: mvn clean install. Note internet access is required the first time the project is built so Maven can download the required project dependencies. Remote access can be turned off for subsequent builds by executing: mvn -o clean install.

* To build BigBank, go to the bigbank folder and execute: mvn -r clean install. The '-r' command is required so all BigBank modules will be built. Note internet access is required the first time the project is built so Maven can download the required project dependencies. Remote access can be turned off for subsequent builds by executing: mvn -o clean install.

After the projects have been built, the can be deployed to the Fabric3 runtime as follows:

* Start the runtime from the /bin directory by executing: java -jar server.jar standalone.

* If WebCalc is being deployed, copy the WebCalc war from the /target output directory to the Fabric3 /deploy directory. The runtime will write a message to the console after the jar has been deployed. The calculator UI can be accessed at http://localhost:8181/calculator/entry.html.

* If BigBank is being deployed, copy the fabric3-tutorial-bigbank-0.5ALPHA2-SNAPSHOT.jar (located in /bigbank/bigbank/target) and the fabric3-tutorial-bigbank-webclient-0.5ALPHA2-SNAPSHOT.war (located in /bigbank/bigbank-webclient/target) archived to the Fabric3 /deploy directory. They can either be copied and installed together or individually. If they are copied and installed separately, the bigbank jar must be installed first, followed by the web archive since the latter depends on services provided by the former. The BigBank UI can be accessed from http://localhost:8181/lending/applicationForm.jsp.   

II. Reporting Issues

* If you experience a problem or would like to suggest improvements, send a note to the user list (http://xircles.codehaus.org/projects/fabric3/lists) or file a JIRA issue (http://jira.codehaus.org/browse/FABRICTHREE). 


 
   


