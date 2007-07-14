#!/bin/bash

mvn clean install -f fabric3-api/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi

mvn clean install -f fabric3-host-api/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi
mvn clean install -f fabric3-spi/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi
mvn clean install -f fabric3-policy-spi/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi

mvn clean install -f fabric3-extension/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi
mvn clean install -f fabric3-jmx/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi
mvn clean install -f fabric3-transform/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi
mvn clean install -f fabric3-fabric/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi
mvn clean install -f fabric3-pojo/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi

mvn clean install -f fabric3-jetty/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi

mvn clean install -f fabric3-install-jxta/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi
mvn clean install -f fabric3-jxta/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi
mvn clean install -f fabric3-discovery-jxta/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi
mvn clean install -f fabric3-messaging-jxta/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi

mvn clean install -f fabric3-messaging-jms/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi

mvn clean install -f fabric3-binding-burlap/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi
mvn clean install -f fabric3-binding-hessian/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi
mvn clean install -f fabric3-binding-jms/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi
mvn clean install -f fabric3-binding-ws/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi

mvn clean install -f fabric3-console/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi

mvn clean install -f fabric3-groovy/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi

mvn clean install -f fabric3-interface-wsdl/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi

mvn clean install -f fabric3-maven/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi
mvn clean install -f fabric3-scdl4j/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi

mvn clean install -f fabric3-contribution-plugin/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi
mvn clean install -f fabric3-webapp-plugin/pom.xml
if [ $? != 0 ] 
then 
    exit $? 
fi
