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

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.ModelObject;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ReferenceDefinition;

/**
 * Verifies loading of a reference definition from an XML-based assembly
 *
 * @version $Rev$ $Date$
 */
public class ReferenceLoaderTestCase extends TestCase {
    private String promoted;
    private ReferenceLoader loader;
    private XMLStreamReader mockReader;
    private LoaderRegistry mockRegistry;
    private LoaderContext ctx;

    public void testWithNoInterface() throws LoaderException, XMLStreamException {
        String name = "referenceDefinition";
        EasyMock.expect(mockReader.getAttributeValue(null, "name")).andReturn(name);
        EasyMock.expect(mockReader.getAttributeValue(null, "promote")).andReturn(promoted);
        EasyMock.expect(mockReader.getAttributeValue(null, "multiplicity")).andReturn("0..1");
        EasyMock.expect(mockReader.getAttributeValue(org.fabric3.spi.Constants.FABRIC3_NS, "key")).andReturn("test");
        EasyMock.expect(mockReader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.replay(mockReader);
        ReferenceDefinition referenceDefinition = loader.load(mockReader, ctx);
        assertNotNull(referenceDefinition);
        assertEquals(name, referenceDefinition.getName());
    }

    public void testPromote() throws LoaderException, XMLStreamException {
        String name = "referenceDefinition";
        EasyMock.expect(mockReader.getAttributeValue(null, "name")).andReturn(name);
        EasyMock.expect(mockReader.getAttributeValue(null, "promote")).andReturn(promoted);
        EasyMock.expect(mockReader.getAttributeValue(null, "multiplicity")).andReturn("0..1");
        EasyMock.expect(mockReader.getAttributeValue(org.fabric3.spi.Constants.FABRIC3_NS, "key")).andReturn("test");
        EasyMock.expect(mockReader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.replay(mockReader);
        ReferenceDefinition referenceDefinition = loader.load(mockReader, ctx);
        assertEquals("Component#Service", referenceDefinition.getPromoted().get(0).toString());
    }

    public void testComponentTypeService() throws LoaderException, XMLStreamException {
        String name = "reference";
        EasyMock.expect(mockReader.getAttributeValue(null, "name")).andReturn(name);
        EasyMock.expect(mockReader.getAttributeValue(null, "promote")).andReturn(promoted);
        EasyMock.expect(mockReader.getAttributeValue(null, "multiplicity")).andReturn("0..1");
        EasyMock.expect(mockReader.getAttributeValue(org.fabric3.spi.Constants.FABRIC3_NS, "key")).andReturn("test");
        EasyMock.expect(mockReader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.replay(mockReader);
        ReferenceDefinition referenceDefinition = loader.load(mockReader, ctx);
        assertTrue(ReferenceDefinition.class.equals(referenceDefinition.getClass()));
    }

    public void testMultipleBindings() throws LoaderException, XMLStreamException {
        String name = "referenceDefinition";
        EasyMock.expect(mockReader.getAttributeValue(null, "name")).andReturn(name);
        EasyMock.expect(mockReader.getAttributeValue(null, "promote")).andReturn(promoted);
        EasyMock.expect(mockReader.getAttributeValue(null, "multiplicity")).andReturn("0..1");
        EasyMock.expect(mockReader.getAttributeValue(org.fabric3.spi.Constants.FABRIC3_NS, "key")).andReturn("test");
        EasyMock.expect(mockReader.next()).andReturn(XMLStreamConstants.START_ELEMENT).times(2);
        EasyMock.expect(mockReader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.replay(mockReader);

        BindingDefinition binding = new BindingDefinition() {
        };
        EasyMock.expect(mockRegistry.load(
                EasyMock.eq(mockReader),
                EasyMock.eq(ModelObject.class), EasyMock.isA(LoaderContext.class)))
                .andReturn(binding).times(2);
        EasyMock.replay(mockRegistry);

        ReferenceDefinition referenceDefinition = loader.load(mockReader, ctx);
        assertEquals(2, referenceDefinition.getBindings().size());
    }

    public void testWithInterface() throws LoaderException, XMLStreamException {
        String name = "referenceDefinition";
        ServiceContract sc = new ServiceContract() {
        };
        EasyMock.expect(mockReader.getAttributeValue(null, "name")).andReturn(name);
        EasyMock.expect(mockReader.getAttributeValue(null, "promote")).andReturn(promoted);
        EasyMock.expect(mockReader.getAttributeValue(null, "multiplicity")).andReturn("0..1");
        EasyMock.expect(mockReader.getAttributeValue(org.fabric3.spi.Constants.FABRIC3_NS, "key")).andReturn("test");
        EasyMock.expect(mockReader.next()).andReturn(XMLStreamConstants.START_ELEMENT);
        EasyMock.expect(mockRegistry.load(mockReader, ModelObject.class, ctx)).andReturn(sc);
        EasyMock.expect(mockReader.next()).andReturn(XMLStreamConstants.END_ELEMENT);

        EasyMock.replay(mockReader);
        EasyMock.replay(mockRegistry);

        ReferenceDefinition referenceDefinition = loader.load(mockReader, ctx);
        assertNotNull(referenceDefinition);
        assertEquals(name, referenceDefinition.getName());
        assertSame(sc, referenceDefinition.getServiceContract());
    }

    protected void setUp() throws Exception {
        super.setUp();
        promoted = "Component/Service";
        mockReader = EasyMock.createStrictMock(XMLStreamReader.class);
        mockRegistry = EasyMock.createMock(LoaderRegistry.class);
        loader = new ReferenceLoader(mockRegistry);
        ctx = EasyMock.createMock(LoaderContext.class);
        EasyMock.replay(ctx);
    }
}
