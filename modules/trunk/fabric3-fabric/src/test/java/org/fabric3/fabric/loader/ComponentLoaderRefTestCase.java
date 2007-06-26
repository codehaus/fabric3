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

import java.net.URI;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.implementation.java.JavaImplementation;
import org.fabric3.spi.component.Component;
import org.fabric3.pojo.processor.JavaMappedReference;
import org.fabric3.pojo.processor.PojoComponentType;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.ReferenceTarget;

/**
 * @version $Rev$ $Date$
 */
public class ComponentLoaderRefTestCase extends TestCase {
    private ComponentLoader loader;
    private final URI componentId = URI.create("sca://localhost/parent/");
    private LoaderContext context;

    public void testLoadReferenceNoFragment() throws LoaderException, XMLStreamException {
        PojoComponentType type =
                new PojoComponentType();
        JavaMappedReference reference = new JavaMappedReference(URI.create("#reference"), null, null);
        type.add(reference);
        JavaImplementation impl = new JavaImplementation();
        impl.setComponentType(type);
        ComponentDefinition<?> definition = new ComponentDefinition<JavaImplementation>("component", impl);
        XMLStreamReader reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getAttributeValue(null, "name")).andReturn("reference");
        EasyMock.expect(reader.getAttributeValue(null, "target")).andReturn("target");
        EasyMock.expect(reader.getAttributeValue(null, "autowire")).andReturn(null);
        EasyMock.replay(reader);
        loader.loadReference(reader, definition, context);
        ReferenceTarget target = definition.getReferenceTargets().get("reference");
        assertEquals(1, target.getTargets().size());
        URI uri = target.getTargets().get(0);
        assertEquals("target", uri.toString());
        assertNull(uri.getFragment());
        EasyMock.verify(reader);
    }

    public void testLoadReferenceWithFragment() throws LoaderException, XMLStreamException {
        PojoComponentType type =
                new PojoComponentType();
        JavaMappedReference reference = new JavaMappedReference(URI.create("#reference"), null, null);
        type.add(reference);
        JavaImplementation impl = new JavaImplementation();
        impl.setComponentType(type);
        ComponentDefinition<?> definition = new ComponentDefinition<JavaImplementation>("component", impl);
        XMLStreamReader reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getAttributeValue(null, "name")).andReturn("reference");
        EasyMock.expect(reader.getAttributeValue(null, "target")).andReturn("target/fragment");
        EasyMock.expect(reader.getAttributeValue(null, "autowire")).andReturn(null);
        EasyMock.replay(reader);
        loader.loadReference(reader, definition, context);
        ReferenceTarget target = definition.getReferenceTargets().get("reference");
        assertEquals(1, target.getTargets().size());
        URI uri = target.getTargets().get(0);
        assertEquals("target#fragment", uri.toString());
        EasyMock.verify(reader);
    }

    public void testLoadReferenceWithMultipleTargetUris() throws LoaderException, XMLStreamException {
        PojoComponentType type =
                new PojoComponentType();
        JavaMappedReference reference = new JavaMappedReference(URI.create("#reference"), null, null);
        type.add(reference);
        JavaImplementation impl = new JavaImplementation();
        impl.setComponentType(type);
        ComponentDefinition<?> definition = new ComponentDefinition<JavaImplementation>("component", impl);
        XMLStreamReader reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getAttributeValue(null, "name")).andReturn("reference");
        EasyMock.expect(reader.getAttributeValue(null, "target")).andReturn("target1/fragment1 target2/fragment2");
        EasyMock.expect(reader.getAttributeValue(null, "autowire")).andReturn(null);
        EasyMock.replay(reader);
        loader.loadReference(reader, definition, context);
        ReferenceTarget target = definition.getReferenceTargets().get("reference");
        assertEquals(2, target.getTargets().size());
        URI uri1 = target.getTargets().get(0);
        assertEquals("target1#fragment1", uri1.toString());
        URI uri2 = target.getTargets().get(1);
        assertEquals("target2#fragment2", uri2.toString());
        EasyMock.verify(reader);
    }

    public void testLoadReferenceAutowire() throws LoaderException, XMLStreamException {
        PojoComponentType type =
                new PojoComponentType();
        JavaMappedReference reference = new JavaMappedReference(URI.create("#reference"), null, null);
        type.add(reference);
        JavaImplementation impl = new JavaImplementation();
        impl.setComponentType(type);
        ComponentDefinition<?> definition = new ComponentDefinition<JavaImplementation>("component", impl);
        XMLStreamReader reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getAttributeValue(null, "name")).andReturn("reference");
        EasyMock.expect(reader.getAttributeValue(null, "target")).andReturn("target/fragment");
        EasyMock.expect(reader.getAttributeValue(null, "autowire")).andReturn("true");
        EasyMock.replay(reader);
        loader.loadReference(reader, definition, context);
        ReferenceTarget target = definition.getReferenceTargets().get("reference");
        assertTrue(target.isAutowire());
        EasyMock.verify(reader);
    }


    protected void setUp() throws Exception {
        super.setUp();
        LoaderRegistry mockRegistry = EasyMock.createMock(LoaderRegistry.class);
        loader = new ComponentLoader(mockRegistry);
        Component parent = EasyMock.createNiceMock(Component.class);
        EasyMock.expect(parent.getUri()).andReturn(componentId).atLeastOnce();
        EasyMock.replay(parent);

        context = EasyMock.createMock(LoaderContext.class);
        EasyMock.replay(context);
    }
}
