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
package org.fabric3.fabric.implementation.composite;

import java.net.MalformedURLException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.osoa.sca.Constants.SCA_NS;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.scdl.CompositeImplementation;

/**
 * @version $Rev$ $Date$
 */
public class ImplementationCompositeLoaderTestCase extends TestCase {
    private static final QName IMPLEMENTATION_COMPOSITE = new QName(SCA_NS, "implementation.composite");

    private ImplementationCompositeLoader loader;
    private XMLStreamReader reader;
    private QName name;
    private NamespaceContext namespaceContext;
    private LoaderContext context;

    public void testName() throws LoaderException, XMLStreamException, MalformedURLException {
        expect(reader.getName()).andReturn(IMPLEMENTATION_COMPOSITE);
        expect(reader.getAttributeValue(null, "name")).andReturn(name.getLocalPart());
        expect(reader.getNamespaceContext()).andReturn(namespaceContext);
        expect(reader.next()).andReturn(END_ELEMENT);
        expect(context.getTargetNamespace()).andReturn(name.getNamespaceURI());
        replay(reader, namespaceContext, context);

        CompositeImplementation impl = loader.load(reader, context);
        verify(reader, namespaceContext, context);
        assertEquals(name, impl.getName());
    }

    public void testWithArtifact() throws LoaderException, XMLStreamException, MalformedURLException {
        expect(reader.getName()).andReturn(IMPLEMENTATION_COMPOSITE);
        expect(reader.getAttributeValue(null, "name")).andReturn(name.getLocalPart());
        expect(reader.getNamespaceContext()).andReturn(namespaceContext);
        expect(reader.next()).andReturn(END_ELEMENT);
        expect(context.getTargetNamespace()).andReturn(name.getNamespaceURI());
        replay(reader, namespaceContext, context);

        CompositeImplementation impl = loader.load(reader, context);
        verify(reader, namespaceContext, context);
        assertEquals(name, impl.getName());
    }

    public void testWithScdlLocation() throws LoaderException, XMLStreamException, MalformedURLException {
        expect(reader.getName()).andReturn(IMPLEMENTATION_COMPOSITE);
        expect(reader.getAttributeValue(null, "name")).andReturn(name.getLocalPart());
        expect(reader.getNamespaceContext()).andReturn(namespaceContext);
        expect(reader.next()).andReturn(END_ELEMENT);
        expect(context.getTargetNamespace()).andReturn(name.getNamespaceURI());
        replay(reader, namespaceContext, context);

        CompositeImplementation impl = loader.load(reader, context);
        verify(reader, namespaceContext, context);
        assertEquals(name, impl.getName());
    }

    public void testWithJarLocation() throws LoaderException, XMLStreamException, MalformedURLException {
        expect(reader.getName()).andReturn(IMPLEMENTATION_COMPOSITE);
        expect(reader.getAttributeValue(null, "name")).andReturn(name.getLocalPart());
        expect(reader.getNamespaceContext()).andReturn(namespaceContext);
        expect(reader.next()).andReturn(END_ELEMENT);
        expect(context.getTargetNamespace()).andReturn(name.getNamespaceURI());
        replay(reader, namespaceContext, context);

        CompositeImplementation impl = loader.load(reader, context);
        verify(reader, namespaceContext, context);
        assertEquals(name, impl.getName());
    }

    protected void setUp() throws Exception {
        super.setUp();
        reader = createMock(XMLStreamReader.class);
        namespaceContext = createMock(NamespaceContext.class);
        context = createMock(LoaderContext.class);
        loader = new ImplementationCompositeLoader(null);
        name = new QName("http://example.com/xmlns", "foo");
    }
}
