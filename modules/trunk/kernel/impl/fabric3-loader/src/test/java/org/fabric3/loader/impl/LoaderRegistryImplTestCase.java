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
package org.fabric3.loader.impl;

import java.util.Collections;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.loader.common.IntrospectionContextImpl;
import org.fabric3.scdl.ModelObject;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.introspection.xml.UnrecognizedElementException;
import org.fabric3.spi.services.factories.xml.XMLFactory;

/**
 * Verifies the default loader registry
 *
 * @version $Rev$ $Date$
 */
public class LoaderRegistryImplTestCase extends TestCase {
    private LoaderRegistryImpl registry;
    private QName name;
    private LoaderRegistryImpl.Monitor mockMonitor;
    private TypeLoader<Object> mockLoader;
    private XMLStreamReader mockReader;
    private IntrospectionContext introspectionContext;
    private ModelObject modelType;

    public void testLoaderRegistration() {
        mockMonitor.registeringLoader(name);
        EasyMock.replay(mockMonitor);
        registry.registerLoader(name, mockLoader);
        EasyMock.verify(mockMonitor);
    }

    public void testLoaderUnregistration() {
        mockMonitor.unregisteringLoader(name);
        EasyMock.replay(mockMonitor);
        registry.unregisterLoader(name);
        EasyMock.verify(mockMonitor);
    }

    public void testSuccessfulDispatch() throws LoaderException, XMLStreamException {
        EasyMock.expect(mockReader.getName()).andReturn(name);
        EasyMock.replay(mockReader);
        mockMonitor.registeringLoader(name);
        mockMonitor.elementLoad(name);
        EasyMock.replay(mockMonitor);
        EasyMock.expect(mockLoader.load(mockReader, introspectionContext)).andReturn(modelType);
        EasyMock.replay(mockLoader);
        registry.registerLoader(name, mockLoader);
        assertSame(modelType, registry.load(mockReader, ModelObject.class, introspectionContext));
        EasyMock.verify(mockLoader);
        EasyMock.verify(mockMonitor);
        EasyMock.verify(mockReader);

    }

    public void testUnsuccessfulDispatch() throws LoaderException, XMLStreamException {
        EasyMock.expect(mockReader.getName()).andReturn(name);
        EasyMock.replay(mockReader);
        mockMonitor.elementLoad(name);
        EasyMock.replay(mockMonitor);
        try {
            registry.load(mockReader, ModelObject.class, introspectionContext);
            fail();
        } catch (UnrecognizedElementException e) {
            assertSame(name, e.getElement());
        }
        EasyMock.verify(mockReader);
        EasyMock.verify(mockMonitor);
    }

    public void testPregivenModelObject() throws Exception {
        EasyMock.expect(mockReader.getName()).andReturn(name);
        EasyMock.replay(mockReader);
        mockMonitor.registeringLoader(name);
        mockMonitor.elementLoad(name);
        EasyMock.replay(mockMonitor);
        EasyMock.expect(mockLoader.load(mockReader, introspectionContext)).andReturn(modelType);
        EasyMock.replay(mockLoader);
        registry.registerLoader(name, mockLoader);
        assertSame(modelType, registry.load(mockReader, ModelObject.class, introspectionContext));
        EasyMock.verify(mockLoader);
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        name = new QName("http://mock", "test");
        ClassLoader cl = getClass().getClassLoader();
        introspectionContext = new IntrospectionContextImpl(cl, null, null);
        mockMonitor = EasyMock.createMock(LoaderRegistryImpl.Monitor.class);
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        XMLFactory xmlFactory = EasyMock.createMock(XMLFactory.class);
        EasyMock.expect(xmlFactory.newInputFactoryInstance()).andStubReturn(xmlInputFactory);
        EasyMock.replay(xmlFactory);
        registry = new LoaderRegistryImpl(mockMonitor, xmlFactory);
        Map<QName, TypeLoader<?>> map = Collections.emptyMap();
        registry.setLoaders(map);
        mockLoader = EasyMock.createMock(TypeLoader.class);
        mockReader = EasyMock.createMock(XMLStreamReader.class);
        modelType = new ModelObject() {
        };
    }

}
