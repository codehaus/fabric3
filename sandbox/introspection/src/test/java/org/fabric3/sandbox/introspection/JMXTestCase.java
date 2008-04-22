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
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.fabric3.monitor.impl.NullMonitorFactory;
import org.fabric3.sandbox.introspection.impl.IntrospectionFactoryImpl;
import org.fabric3.services.xmlfactory.impl.DefaultXMLFactoryImpl;

/**
 * @version $Rev$ $Date$
 */
public class JMXTestCase extends TestCase {
    private MBeanServer mbServer;
    private IntrospectionFactory factory;

    public void testAssemblyIsRegistered() throws MalformedObjectNameException {

/*
        Set names = mbServer.queryNames(ObjectName.getInstance("*:service=LogicalComponentManager,*"), null);
        assertEquals(1, names.size());
*/

    }

    protected void setUp() throws Exception {
        super.setUp();

        mbServer = MBeanServerFactory.newMBeanServer();
        factory = new IntrospectionFactoryImpl(new NullMonitorFactory(), new DefaultXMLFactoryImpl(), null, mbServer);
    }
}
