/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.mock;

import java.util.LinkedList;
import java.util.List;

import org.easymock.EasyMock;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.contract.ContractProcessor;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.scdl.ServiceDefinition;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class MockComponentTypeLoaderImplTest extends TestCase {

    public void testLoad() throws Exception {
        
        IntrospectionContext context = EasyMock.createMock(IntrospectionContext.class);
        EasyMock.expect(context.getTargetClassLoader()).andReturn(getClass().getClassLoader());
        EasyMock.replay(context);

        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        ContractProcessor processor = new DefaultContractProcessor(helper);
        MockComponentTypeLoader componentTypeLoader = new MockComponentTypeLoaderImpl(helper, processor);
        
        List<String> mockedInterfaces = new LinkedList<String>();
        mockedInterfaces.add("org.fabric3.mock.Foo");
        mockedInterfaces.add("org.fabric3.mock.Bar");
        mockedInterfaces.add("org.fabric3.mock.Baz");
        
        MockComponentType componentType = componentTypeLoader.load(mockedInterfaces, context);
        
        assertNotNull(componentType);
        java.util.Map<String, ServiceDefinition> services = componentType.getServices();
        
        assertEquals(3, services.size());
        
        ServiceDefinition service = services.get("service0");
        assertNotNull(service);
        assertEquals("Foo", service.getName());
        assertEquals("org.fabric3.mock.Foo", service.getServiceContract().getQualifiedInterfaceName());

        service = services.get("service1");
        assertNotNull(service);
        assertEquals("Bar", service.getName());
        assertEquals("org.fabric3.mock.Bar", service.getServiceContract().getQualifiedInterfaceName());

        service = services.get("service2");
        assertNotNull(service);
        assertEquals("Baz", service.getName());
        assertEquals("org.fabric3.mock.Baz", service.getServiceContract().getQualifiedInterfaceName());
        
        
    }

}
