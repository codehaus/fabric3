#!/bin/bash

declare -a modules

modules[0]=fabric3-api/pom.xml
modules[1]=fabric3-host-api/pom.xml
modules[2]=fabric3-spi/pom.xml
modules[3]=fabric3-policy-spi/pom.xml
modules[4]=fabric3-extension/pom.xml
modules[5]=fabric3-jmx/pom.xml
modules[6]=fabric3-transform/pom.xml
modules[7]=fabric3-fabric/pom.xml
modules[8]=fabric3-pojo/pom.xml
modules[9]=fabric3-jetty/pom.xml
modules[10]=fabric3-install-jxta/pom.xml
modules[11]=fabric3-jxta/pom.xml
modules[12]=fabric3-discovery-jxta/pom.xml
modules[13]=fabric3-messaging-jxta/pom.xml
modules[14]=fabric3-messaging-jms/pom.xml
modules[15]=fabric3-binding-burlap/pom.xml
modules[16]=fabric3-binding-hessian/pom.xml
modules[17]=fabric3-binding-jms/pom.xml
modules[18]=fabric3-binding-ws/pom.xml
modules[19]=fabric3-console/pom.xml
modules[20]=fabric3-groovy/pom.xml
modules[21]=fabric3-interface-wsdl/pom.xml
modules[22]=fabric3-maven/pom.xml
modules[23]=fabric3-contribution-plugin/pom.xml
modules[24]=fabric3-webapp-plugin/pom.xml

rm -r ~/.m2/repository/org/codehaus/fabric3

for module in ${modules[@]} 
do 
   mvn clean package -f $module 
   if [ $? != 0 ] 
   then
       exit $?
   fi
done

echo "Modules built succesfully"
