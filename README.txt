Fabric3
=======

Fabric3 is a project to implement a federated runtime supporting applications
written using the Service Component Architecture V1.0.

The project was originally based on a snapshot of the Apache Tuscany project
taken around the end of March 2007 (r520715 in Apache Subversion). Development
of the two projects has diverged since then.


Structure
---------
The project has a modular structure based on a build using Apache Maven.
We will periodically publish releases and unstable SNAPSHOTs to the online
Maven repositories to allow individual modules to built on their own. The
structure comprises of a embeddable kernel that provides core component
management and wiring services within a single address space. This kernel
is hosted in variety of different runtime platforms such as a standalone
client or server, a web application, or various test frameworks. Most
component containers and network bindings are provided through extension
modules.

Building
--------
The modules directory contains individual modules that are intended to
be built, tested and released independently. In general, to build any
modules, check it out from SVN, cd to the root directory, and run

 $ mvn package

