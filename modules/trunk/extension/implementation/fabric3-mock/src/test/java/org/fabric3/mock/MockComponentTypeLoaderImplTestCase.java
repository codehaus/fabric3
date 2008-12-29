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

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import org.fabric3.model.type.service.JavaServiceContract;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.spi.introspection.ValidationContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.contract.ContractProcessor;

/**
 * @version $Revision$ $Date$
 */
public class MockComponentTypeLoaderImplTestCase extends TestCase {

    @SuppressWarnings({"unchecked"})
    public void testLoad() throws Exception {

        IntrospectionContext context = EasyMock.createMock(IntrospectionContext.class);
        EasyMock.expect(context.getTargetClassLoader()).andReturn(getClass().getClassLoader());
        EasyMock.replay(context);

        IntrospectionHelper helper = EasyMock.createMock(IntrospectionHelper.class);
        EasyMock.expect(helper.mapTypeParameters(EasyMock.isA(Class.class))).andReturn(new TypeMapping()).atLeastOnce();
        EasyMock.expect(helper.isAnnotationPresent(EasyMock.isA(Class.class), EasyMock.isA(Class.class))).andReturn(false).atLeastOnce();
        EasyMock.replay(helper);

        ContractProcessor processor = EasyMock.createMock(ContractProcessor.class);
        ServiceContract controlContract = new JavaServiceContract(IMocksControl.class);
        ServiceContract fooContract = new JavaServiceContract(Foo.class);
        EasyMock.expect(processor.introspect(EasyMock.isA(TypeMapping.class),
                                             EasyMock.eq(IMocksControl.class),
                                             EasyMock.isA(ValidationContext.class))).andReturn(controlContract);
        EasyMock.expect(processor.introspect(EasyMock.isA(TypeMapping.class),
                                             EasyMock.eq(Foo.class),
                                             EasyMock.isA(ValidationContext.class))).andReturn(fooContract);
        EasyMock.replay(processor);

        MockComponentTypeLoader componentTypeLoader = new MockComponentTypeLoaderImpl(helper, processor);

        List<String> mockedInterfaces = new LinkedList<String>();
        mockedInterfaces.add("org.fabric3.mock.Foo");

        MockComponentType componentType = componentTypeLoader.load(mockedInterfaces, context);

        assertNotNull(componentType);
        java.util.Map<String, ServiceDefinition> services = componentType.getServices();

        assertEquals(2, services.size());    // 4 because the mock service is added implicitly

        ServiceDefinition service = services.get("Foo");
        assertNotNull(service);
        assertEquals("org.fabric3.mock.Foo", service.getServiceContract().getQualifiedInterfaceName());


    }

}
