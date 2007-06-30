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
package org.fabric3.fabric.loader;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.osoa.sca.Constants.SCA_NS;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.model.type.BindingDefinition;
import org.fabric3.spi.model.type.ServiceContract;
import org.fabric3.spi.model.type.ServiceDefinition;
import org.fabric3.spi.model.type.ModelObject;

/**
 * Verifies loading of a service definition from an XML-based assembly
 *
 * @version $Rev$ $Date$
 */
public class ServiceLoaderTestCase extends TestCase {
    private static final QName SERVICE = new QName(SCA_NS, "service");
    private ServiceLoader loader;
    private LoaderContext loaderContext;
    private XMLStreamReader mockReader;
    private LoaderRegistry mockRegistry;

    public void testWithNoInterface() throws LoaderException, XMLStreamException {
        String name = "serviceDefinition";
        expect(mockReader.getName()).andReturn(SERVICE).anyTimes();
        expect(mockReader.getAttributeValue(null, "name")).andReturn(name);
        expect(mockReader.getAttributeValue(null, "promote")).andReturn(null);
        expect(mockReader.next()).andReturn(END_ELEMENT);
        expect(mockReader.getName()).andReturn(SERVICE).anyTimes();
        replay(mockReader);
        ServiceDefinition serviceDefinition = loader.load(mockReader, loaderContext);
        assertNotNull(serviceDefinition);
        assertEquals("#" + name, serviceDefinition.getUri().toString());
    }

    public void testComponentTypeService() throws LoaderException, XMLStreamException {
        String name = "service";
        expect(mockReader.getName()).andReturn(SERVICE).anyTimes();
        expect(mockReader.getAttributeValue(null, "name")).andReturn(name);
        expect(mockReader.getAttributeValue(null, "promote")).andReturn(null);
        expect(mockReader.next()).andReturn(END_ELEMENT);
        expect(mockReader.getName()).andReturn(SERVICE).anyTimes();
        replay(mockReader);
        ServiceDefinition serviceDefinition = loader.load(mockReader, loaderContext);
        assertTrue(ServiceDefinition.class.equals(serviceDefinition.getClass()));
    }

    public void testMultipleBindings() throws LoaderException, XMLStreamException {
        String name = "serviceDefinition";
        expect(mockReader.getName()).andReturn(SERVICE).anyTimes();
        expect(mockReader.getAttributeValue(null, "name")).andReturn(name);
        expect(mockReader.getAttributeValue(null, "promote")).andReturn("component/target");
        expect(mockReader.next()).andReturn(START_ELEMENT);
        expect(mockReader.next()).andReturn(START_ELEMENT);
        expect(mockReader.next()).andReturn(END_ELEMENT);
        expect(mockReader.getName()).andReturn(SERVICE).anyTimes();
        replay(mockReader);

        BindingDefinition binding = new BindingDefinition() {
        };
        expect(mockRegistry.load(mockReader, ModelObject.class, loaderContext)).andReturn(binding).times(2);
        replay(mockRegistry);

        ServiceDefinition serviceDefinition = loader.load(mockReader, loaderContext);
        assertEquals(2, serviceDefinition.getBindings().size());
    }

    public void testWithInterface() throws LoaderException, XMLStreamException {
        String name = "serviceDefinition";
        String target = "target";
        ServiceContract sc = new ServiceContract() {
        };
        expect(mockReader.getName()).andReturn(SERVICE).anyTimes();
        expect(mockReader.getAttributeValue(null, "name")).andReturn(name);
        expect(mockReader.getAttributeValue(null, "promote")).andReturn(target);
        expect(mockReader.next()).andReturn(START_ELEMENT);
        expect(mockRegistry.load(mockReader, ModelObject.class, loaderContext)).andReturn(sc);
        expect(mockReader.next()).andReturn(END_ELEMENT);
        expect(mockReader.getName()).andReturn(SERVICE);

        replay(mockReader);
        replay(mockRegistry);

        ServiceDefinition serviceDefinition = loader.load(mockReader, loaderContext);
        assertNotNull(serviceDefinition);
        assertEquals("#" + name, serviceDefinition.getUri().toString());
        assertSame(sc, serviceDefinition.getServiceContract());
    }

    public void testWithNoReference() throws LoaderException, XMLStreamException {
        String name = "serviceDefinition";
        ServiceContract sc = new ServiceContract() {
        };
        expect(mockReader.getName()).andReturn(SERVICE).anyTimes();
        expect(mockReader.getAttributeValue(null, "name")).andReturn(name);
        expect(mockReader.getAttributeValue(null, "promote")).andReturn(null);
        expect(mockReader.next()).andReturn(START_ELEMENT);
        expect(mockRegistry.load(mockReader, ModelObject.class, loaderContext)).andReturn(sc);
        expect(mockReader.next()).andReturn(END_ELEMENT);
        expect(mockReader.getName()).andReturn(SERVICE);

        replay(mockReader);
        replay(mockRegistry);

        ServiceDefinition serviceDefinition = loader.load(mockReader, loaderContext);
        assertNotNull(serviceDefinition);
        assertEquals("#" + name, serviceDefinition.getUri().toString());
        assertSame(sc, serviceDefinition.getServiceContract());
    }

    protected void setUp() throws Exception {
        super.setUp();
        mockReader = EasyMock.createStrictMock(XMLStreamReader.class);
        mockRegistry = EasyMock.createMock(LoaderRegistry.class);
        loader = new ServiceLoader(mockRegistry);
        loaderContext = EasyMock.createMock(LoaderContext.class);
        EasyMock.replay(loaderContext);
    }
}
