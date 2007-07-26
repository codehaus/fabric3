/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.loader.composite;

import java.net.URI;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.CompositeService;
import org.fabric3.scdl.ModelObject;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;

/**
 * Verifies loading of a service definition from an XML-based assembly
 *
 * @version $Rev$ $Date$
 */
public class CompositeServiceLoaderTestCase extends TestCase {
    private final String serviceName = "service";
    private final String componentName = "component";
    private final String componentServiceName = "component/service";
    private final URI componentURI = URI.create("component");
    private final URI componentServiceURI = URI.create("component#service");

    private CompositeServiceLoader loader;
    private LoaderContext loaderContext;
    private XMLStreamReader mockReader;
    private LoaderRegistry mockRegistry;

    public void testPromotedComponent() throws LoaderException, XMLStreamException {
        expect(mockReader.getAttributeValue(null, "name")).andReturn(serviceName);
        expect(mockReader.getAttributeValue(null, "promote")).andReturn(componentName);
        expect(mockReader.next()).andReturn(END_ELEMENT);
        replay(mockReader);
        CompositeService serviceDefinition = loader.load(mockReader, loaderContext);
        assertNotNull(serviceDefinition);
        assertEquals(serviceName, serviceDefinition.getName());
        assertEquals(componentURI, serviceDefinition.getTarget());
    }

    public void testPromotedService() throws LoaderException, XMLStreamException {
        expect(mockReader.getAttributeValue(null, "name")).andReturn(serviceName);
        expect(mockReader.getAttributeValue(null, "promote")).andReturn(componentServiceName);
        expect(mockReader.next()).andReturn(END_ELEMENT);
        replay(mockReader);
        CompositeService serviceDefinition = loader.load(mockReader, loaderContext);
        assertNotNull(serviceDefinition);
        assertEquals(serviceName, serviceDefinition.getName());
        assertEquals(componentServiceURI, serviceDefinition.getTarget());
    }

    public void testMultipleBindings() throws LoaderException, XMLStreamException {
        expect(mockReader.getAttributeValue(null, "name")).andReturn(serviceName);
        expect(mockReader.getAttributeValue(null, "promote")).andReturn(componentName);
        expect(mockReader.next()).andReturn(START_ELEMENT);
        expect(mockReader.next()).andReturn(START_ELEMENT);
        expect(mockReader.next()).andReturn(END_ELEMENT);
        replay(mockReader);

        BindingDefinition binding = new BindingDefinition() {
        };
        expect(mockRegistry.load(mockReader, ModelObject.class, loaderContext)).andReturn(binding).times(2);
        replay(mockRegistry);

        ServiceDefinition serviceDefinition = loader.load(mockReader, loaderContext);
        assertEquals(2, serviceDefinition.getBindings().size());
    }

    public void testWithInterface() throws LoaderException, XMLStreamException {
        ServiceContract sc = new ServiceContract() {
        };
        expect(mockReader.getAttributeValue(null, "name")).andReturn(serviceName);
        expect(mockReader.getAttributeValue(null, "promote")).andReturn(componentName);
        expect(mockReader.next()).andReturn(START_ELEMENT);
        expect(mockRegistry.load(mockReader, ModelObject.class, loaderContext)).andReturn(sc);
        expect(mockReader.next()).andReturn(END_ELEMENT);

        replay(mockReader);
        replay(mockRegistry);

        ServiceDefinition serviceDefinition = loader.load(mockReader, loaderContext);
        assertSame(sc, serviceDefinition.getServiceContract());
    }

    protected void setUp() throws Exception {
        super.setUp();
        mockReader = EasyMock.createStrictMock(XMLStreamReader.class);
        mockRegistry = EasyMock.createMock(LoaderRegistry.class);
        loader = new CompositeServiceLoader(mockRegistry);
        loaderContext = EasyMock.createMock(LoaderContext.class);
        EasyMock.replay(loaderContext);
    }
}
