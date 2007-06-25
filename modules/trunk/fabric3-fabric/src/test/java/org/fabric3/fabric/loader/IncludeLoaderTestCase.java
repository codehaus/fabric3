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

import java.net.URL;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import org.osoa.sca.Constants;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.model.type.CompositeComponentType;
import org.fabric3.spi.model.type.Include;

/**
 * @version $Rev$ $Date$
 */
public class IncludeLoaderTestCase extends TestCase {
    private static final QName INCLUDE = new QName(Constants.SCA_NS, "include");

    private LoaderRegistry registry;
    private IncludeLoader loader;
    private XMLStreamReader reader;
    private LoaderContext context;
    private URL base;
    private URL includeURL;
    private ClassLoader cl;

    public void testNoLocation() throws LoaderException, XMLStreamException {
        String name = "foo";
        expect(reader.getName()).andReturn(INCLUDE);
        expect(reader.getAttributeValue(null, "name")).andReturn(name);
        expect(reader.getAttributeValue(null, "scdlLocation")).andReturn(null);
        expect(reader.getAttributeValue(null, "scdlResource")).andReturn(null);
        expect(reader.next()).andReturn(END_ELEMENT);

        expect(context.getClassLoader()).andReturn(cl);
        replay(registry, reader, context);

        try {
            loader.load(reader, context);
            fail();
        } catch (MissingIncludeException e) {
            // OK expected
        }
        verify(registry, reader, context);
    }

    public void testWithAbsoluteScdlLocation() throws LoaderException, XMLStreamException {
        String name = "foo";
        expect(reader.getName()).andReturn(INCLUDE);
        expect(reader.getAttributeValue(null, "name")).andReturn(name);
        expect(reader.getAttributeValue(null, "scdlLocation")).andReturn("http://example.com/include.scdl");
        expect(reader.getAttributeValue(null, "scdlResource")).andReturn(null);
        expect(reader.next()).andReturn(END_ELEMENT);

        expect(context.getScdlLocation()).andReturn(base);
        expect(context.getClassLoader()).andReturn(cl);

        expect(registry.load(
                eq(includeURL),
                eq(CompositeComponentType.class),
                isA(LoaderContext.class)))
                .andReturn(null);
        replay(registry, reader, context);

        Include include = loader.load(reader, context);
        assertEquals(name, include.getName());
        assertEquals(includeURL, include.getScdlLocation());
        verify(registry, reader, context);
    }

    public void testWithRelativeScdlLocation() throws LoaderException, XMLStreamException {
        String name = "foo";
        expect(reader.getName()).andReturn(INCLUDE);
        expect(reader.getAttributeValue(null, "name")).andReturn(name);
        expect(reader.getAttributeValue(null, "scdlLocation")).andReturn("include.scdl");
        expect(reader.getAttributeValue(null, "scdlResource")).andReturn(null);
        expect(reader.next()).andReturn(END_ELEMENT);

        expect(context.getScdlLocation()).andReturn(base);
        expect(context.getClassLoader()).andReturn(cl);

        expect(registry.load(
                eq(includeURL),
                eq(CompositeComponentType.class),
                isA(LoaderContext.class)))
                .andReturn(null);
        replay(registry, reader, context);

        Include include = loader.load(reader, context);
        assertEquals(name, include.getName());
        assertEquals(includeURL, include.getScdlLocation());
        verify(registry, reader, context);
    }

    public void testWithScdlResource() throws LoaderException, XMLStreamException {
        String name = "foo";
        String resource = "org/fabric3/fabric/loader/test-include.scdl";
        includeURL = cl.getResource(resource);
        assertNotNull(includeURL);

        expect(reader.getName()).andReturn(INCLUDE);
        expect(reader.getAttributeValue(null, "name")).andReturn(name);
        expect(reader.getAttributeValue(null, "scdlLocation")).andReturn(null);
        expect(reader.getAttributeValue(null, "scdlResource")).andReturn(resource);
        expect(reader.next()).andReturn(END_ELEMENT);

        expect(context.getClassLoader()).andReturn(cl);

        expect(registry.load(
                eq(includeURL),
                eq(CompositeComponentType.class),
                isA(LoaderContext.class)))
                .andReturn(null);
        replay(registry, reader, context);

        Include include = loader.load(reader, context);
        assertEquals(name, include.getName());
        assertEquals(includeURL, include.getScdlLocation());
        verify(registry, reader, context);
    }

    protected void setUp() throws Exception {
        super.setUp();
        registry = createMock(LoaderRegistry.class);
        reader = createMock(XMLStreamReader.class);
        context = createMock(LoaderContext.class);
        cl = getClass().getClassLoader();
        base = new URL("http://example.com/test.scdl");
        includeURL = new URL("http://example.com/include.scdl");
        loader = new IncludeLoader(registry);
    }
}
