Fabric3
=======

Fabric3 is a project to implement a federated runtime supporting applications
written using the Service Component Architecture V1.0.

The project was originally based on a snapshot of the Apache Tuscany project
taken around the end of March 2007 (r520715 in Apache Subversion). Development
of the two projects has diverged substantially since then.


Structure
---------
The project has a modular structure based on a build using Apache Maven.
We will periodically publish releases and unstable SNAPSHOTs to the online
Maven repositories to allow individual modules to built on their own. The
structure comprises of a embeddable kernel that provides core component
management and wiring services. This kernel is hosted in variety of different 
runtime platforms such as a server, a web application, and a Maven-based 
integration framework. Component containers and network bindings 
are provided through extension modules.

Building
--------
In general, to build the source, check out modules/trunk (and tests/trunk 
for integration tests) from SVN, and execute

 $ mvn clean install


