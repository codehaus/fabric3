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
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import org.easymock.IMocksControl;
import static org.osoa.sca.Constants.SCA_NS;
import org.w3c.dom.Document;

import org.fabric3.fabric.implementation.java.JavaImplementation;
import org.fabric3.spi.implementation.java.PojoComponentType;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.InvalidValueException;
import org.fabric3.spi.model.type.Autowire;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.PropertyValue;

/**
 * @version $Rev$ $Date$
 */
public class ComponentLoaderTestCase extends TestCase {
    private static final QName COMPONENT = new QName(SCA_NS, "component");
    private static final String NAME = "testComponent";
    private JavaImplementation impl;

    private XMLStreamReader reader;
    private LoaderRegistry loaderRegistry;
    private ComponentLoader loader;
    private LoaderContext ctx;
    private IMocksControl control;

    public void _testEmptyComponent() throws LoaderException, XMLStreamException {
        EasyMock.expect(reader.getName()).andReturn(COMPONENT);
        EasyMock.expect(reader.getAttributeValue(null, "name")).andReturn(NAME);
        EasyMock.expect(reader.getAttributeValue(null, "initLevel")).andReturn(null);
        EasyMock.expect(reader.getAttributeValue(null, "autowire")).andReturn(null);
        EasyMock.expect(reader.getAttributeValue(null, "runtimeId")).andReturn(null);
        EasyMock.expect(reader.nextTag()).andReturn(0);
        EasyMock.expect(loaderRegistry.load(null, reader, ctx)).andReturn(impl);
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(COMPONENT);
        control.replay();

        ComponentDefinition component = loader.load(null, reader, ctx);
        assertEquals(NAME, component.getName());
        assertEquals(Autowire.INHERITED, component.getAutowire());
        assertNull(component.getInitLevel());
        control.verify();
    }

    public void _testAutowire() throws LoaderException, XMLStreamException {
        EasyMock.expect(reader.getName()).andReturn(COMPONENT);
        EasyMock.expect(reader.getAttributeValue(null, "name")).andReturn(NAME);
        EasyMock.expect(reader.getAttributeValue(null, "initLevel")).andReturn(null);
        EasyMock.expect(reader.getAttributeValue(null, "autowire")).andReturn("true");
        EasyMock.expect(reader.getAttributeValue(null, "runtimeId")).andReturn(null);
        EasyMock.expect(reader.nextTag()).andReturn(0);
        EasyMock.expect(loaderRegistry.load(null, reader, ctx)).andReturn(impl);
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(COMPONENT);
        control.replay();

        ComponentDefinition component = loader.load(null, reader, ctx);
        assertEquals(NAME, component.getName());
        assertNull(component.getInitLevel());
        assertEquals(Autowire.ON, component.getAutowire());
        control.verify();
    }

    public void _testInitValue20() throws LoaderException, XMLStreamException {
        EasyMock.expect(reader.getName()).andReturn(COMPONENT);
        EasyMock.expect(reader.getAttributeValue(null, "name")).andReturn(NAME);
        EasyMock.expect(reader.getAttributeValue(null, "initLevel")).andReturn("20");
        EasyMock.expect(reader.getAttributeValue(null, "autowire")).andReturn(null);
        EasyMock.expect(reader.getAttributeValue(null, "runtimeId")).andReturn(null);
        EasyMock.expect(reader.nextTag()).andReturn(0);
        EasyMock.expect(loaderRegistry.load(null, reader, ctx)).andReturn(impl);
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(COMPONENT);
        control.replay();

        ComponentDefinition component = loader.load(null, reader, ctx);
        assertEquals(NAME, component.getName());
        assertEquals(Integer.valueOf(20), component.getInitLevel());
        assertEquals(Autowire.INHERITED, component.getAutowire());
        control.verify();
    }

    public void _testLoadPropertyWithSource() throws LoaderException, XMLStreamException {
        expect(reader.getAttributeValue(null, "name")).andReturn("name");
        expect(reader.getAttributeValue(null, "source")).andReturn("$source");
        expect(reader.getAttributeValue(null, "file")).andReturn(null);
        expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        replay(reader);
        PropertyValue value = loader.loadPropertyValue(reader, null);
        assertEquals("$source", value.getSource());
        EasyMock.verify(reader);
    }

    public void _testLoadInline() throws InvalidValueException, XMLStreamException {
        String name = "name";
        String text = "Hello World";
        expect(reader.getAttributeValue(null, "type")).andReturn(null);
        expect(reader.getAttributeValue(null, "element")).andReturn(null);
        expect(reader.next()).andReturn(XMLStreamConstants.CHARACTERS);
        expect(reader.getTextCharacters()).andReturn(text.toCharArray());
        expect(reader.getTextStart()).andReturn(0);
        expect(reader.getTextLength()).andReturn(text.length());
        control.replay();
        PropertyValue propertyValue = loader.loadInlinePropertyValue(name, reader);
        assertEquals(name, propertyValue.getName());
        Document value = propertyValue.getValue();
        assertEquals(text, value.getDocumentElement().getTextContent());
        control.verify();
    }

    public void _testUnrecognizedElement() throws LoaderException, XMLStreamException {
        EasyMock.expect(reader.getName()).andReturn(COMPONENT);
        EasyMock.expect(reader.getAttributeValue(null, "name")).andReturn(NAME);
        EasyMock.expect(reader.getAttributeValue(null, "initLevel")).andReturn(null);
        EasyMock.expect(reader.getAttributeValue(null, "autowire")).andReturn(null);
        EasyMock.expect(reader.getAttributeValue(null, "runtimeId")).andReturn(null);
        EasyMock.expect(reader.nextTag()).andReturn(0);
        EasyMock.expect(loaderRegistry.load(null, reader, ctx)).andReturn(impl);
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.START_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(new QName("foo", "bar"));
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(COMPONENT);
        control.replay();

        ComponentDefinition component = loader.load(null, reader, ctx);
        assertEquals(NAME, component.getName());
        assertEquals(Autowire.INHERITED, component.getAutowire());
        assertNull(component.getInitLevel());
        control.verify();
    }
    
    public void test() {
        
    }

    protected void setUp() throws Exception {
        super.setUp();
        impl = new JavaImplementation();
        impl.setComponentType(new PojoComponentType());
        control = EasyMock.createControl();
        loaderRegistry = control.createMock(LoaderRegistry.class);
        reader = control.createMock(XMLStreamReader.class);
        ctx = control.createMock(LoaderContext.class);

        loader = new ComponentLoader(loaderRegistry);
    }
}
