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

import java.io.InputStream;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.easymock.EasyMock;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderRegistry;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class ImplementationMockLoaderTest extends TestCase {

    public void testLoad() throws Exception {
        
        MockComponentTypeLoader componentTypeLoader = EasyMock.createMock(MockComponentTypeLoader.class);
        LoaderRegistry loaderRegistry = EasyMock.createMock(LoaderRegistry.class);
        IntrospectionContext context = EasyMock.createMock(IntrospectionContext.class);
        
        ImplementationMockLoader loader = new ImplementationMockLoader(loaderRegistry, componentTypeLoader);
        
        InputStream stream = getClass().getClassLoader().getResourceAsStream("META-INF/mock.composite");
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        
        while(reader.hasNext()) {
            if(reader.next() == XMLStreamConstants.START_ELEMENT && ImplementationMock.IMPLEMENTATION_MOCK.equals(reader.getName())) {
                break;
            }
        }
        
        ImplementationMock implementationMock = loader.load(reader, context);
        assertNotNull(implementationMock);
        
        List<String> interfaces = implementationMock.getMockedInterfaces();
        assertEquals(3, interfaces.size());
        assertEquals("org.fabric3.mock.test.Foo", interfaces.get(0));
        assertEquals("org.fabric3.mock.test.Bar", interfaces.get(1));
        assertEquals("org.fabric3.mock.test.Baz", interfaces.get(2));
        
    }

}
