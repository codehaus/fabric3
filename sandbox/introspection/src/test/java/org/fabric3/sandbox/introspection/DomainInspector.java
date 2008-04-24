/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.sandbox.introspection;

import java.util.Set;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnector;

import org.fabric3.scdl.Composite;

/**
 * @version $Rev$ $Date$
 */
public class DomainInspector {
    private static final String host = "localhost";
    private static final short port = 1099;

    public static void main(String[] args) throws Exception {
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/server");
        RMIConnector rmiConnector = new RMIConnector(url, null);
        rmiConnector.connect();

        MBeanServerConnection con = rmiConnector.getMBeanServerConnection();

        Set names = con.queryNames(new ObjectName("*:service=LogicalComponentManagerMBean,*"), null);
        ObjectName name = (ObjectName) names.iterator().next();
        Composite composite = (Composite) con.getAttribute(name, "DomainComposite");

        for (String componentName : composite.getComponents().keySet()) {
            System.out.println(componentName);
        }
    }
}
