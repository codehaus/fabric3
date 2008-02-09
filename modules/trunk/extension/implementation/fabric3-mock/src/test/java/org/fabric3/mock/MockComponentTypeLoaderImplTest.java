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
package org.fabric3.mock;

import java.util.LinkedList;
import java.util.List;

import org.easymock.EasyMock;
import org.fabric3.pojo.scdl.JavaMappedService;
import org.fabric3.introspection.IntrospectionContext;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class MockComponentTypeLoaderImplTest extends TestCase {

    public void testLoad() throws Exception {
        
        IntrospectionContext context = EasyMock.createMock(IntrospectionContext.class);
        EasyMock.expect(context.getTargetClassLoader()).andReturn(getClass().getClassLoader());
        EasyMock.replay(context);
        
        MockComponentTypeLoader componentTypeLoader = new MockComponentTypeLoaderImpl();
        
        List<String> mockedInterfaces = new LinkedList<String>();
        mockedInterfaces.add("org.fabric3.mock.Foo");
        mockedInterfaces.add("org.fabric3.mock.Bar");
        mockedInterfaces.add("org.fabric3.mock.Baz");
        
        MockComponentType componentType = componentTypeLoader.load(mockedInterfaces, context);
        
        assertNotNull(componentType);
        java.util.Map<String, JavaMappedService> services = componentType.getServices();
        
        assertEquals(3, services.size());
        
        JavaMappedService service = services.get("service0");
        assertNotNull(service);
        assertEquals("org.fabric3.mock.Foo", service.getServiceInterface());
        
        service = services.get("service1");
        assertNotNull(service);
        assertEquals("org.fabric3.mock.Bar", service.getServiceInterface());
        
        service = services.get("service2");
        assertNotNull(service);
        assertEquals("org.fabric3.mock.Baz", service.getServiceInterface());
        
        
    }

}
