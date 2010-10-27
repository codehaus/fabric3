Fabric3
=======

Fabric3 is a project to implement a runtime that supports composite applications
written using Service Component Architecture V1.1 (SCA).

Structure
---------
The project has a modular structure based on a build using Apache Maven.
The structure comprises of a embeddable kernel that provides core component
management and wiring services. This kernel is hosted in variety of different 
runtime platforms such as a server, Tomcat, and a Maven-based 
integration framework. Component containers and network bindings 
are provided through extension modules.

Building
--------
To build the source, check out modules/trunk (and tests/trunk for integration tests)
from SVN, and execute:

 $ mvn clean install


