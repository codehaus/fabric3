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

import java.util.Collections;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.Location;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;

import org.fabric3.loader.common.LoaderContextImpl;
import org.fabric3.scdl.ModelObject;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.loader.UnrecognizedElementException;

/**
 * Verifies the default loader registry
 *
 * @version $Rev$ $Date$
 */
public class StAXLoaderRegistryImplTestCase extends TestCase {
    private LoaderRegistryImpl registry;
    private QName name;
    private LoaderRegistryImpl.Monitor mockMonitor;
    private StAXElementLoader<Object> mockLoader;
    private XMLStreamReader mockReader;
    private LoaderContext loaderContext;
    private ModelObject modelType;

    public void testLoaderRegistration() {
        mockMonitor.registeringLoader(EasyMock.eq(name));
        EasyMock.replay(mockMonitor);
        registry.registerLoader(name, mockLoader);
        EasyMock.verify(mockMonitor);
    }

    public void testLoaderUnregistration() {
        mockMonitor.unregisteringLoader(EasyMock.eq(name));
        EasyMock.replay(mockMonitor);
        registry.unregisterLoader(name);
        EasyMock.verify(mockMonitor);
    }

    public void testSuccessfulDispatch() throws LoaderException, XMLStreamException {
        EasyMock.expect(mockReader.getName()).andReturn(name);
        EasyMock.replay(mockReader);
        mockMonitor.registeringLoader(EasyMock.eq(name));
        mockMonitor.elementLoad(EasyMock.eq(name));
        EasyMock.replay(mockMonitor);
        EasyMock.expect(mockLoader.load(
                EasyMock.eq(mockReader),
                EasyMock.eq(loaderContext))).andReturn(modelType);
        EasyMock.replay(mockLoader);
        registry.registerLoader(name, mockLoader);
        assertSame(modelType, registry.load(mockReader, ModelObject.class, loaderContext));
        EasyMock.verify(mockLoader);
        EasyMock.verify(mockMonitor);
        EasyMock.verify(mockReader);

    }

    public void testUnsuccessfulDispatch() throws LoaderException, XMLStreamException {
        Location location = EasyMock.createNiceMock(Location.class);
        EasyMock.replay(location);
        EasyMock.expect(mockReader.getName()).andReturn(name);
        EasyMock.expect(mockReader.getLocation()).andReturn(location);
        EasyMock.replay(mockReader);
        mockMonitor.elementLoad(EasyMock.eq(name));
        EasyMock.replay(mockMonitor);
        try {
            registry.load(mockReader, ModelObject.class, loaderContext);
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
        mockMonitor.registeringLoader(EasyMock.eq(name));
        mockMonitor.elementLoad(EasyMock.eq(name));
        EasyMock.replay(mockMonitor);
        EasyMock.expect(mockLoader.load(
                EasyMock.eq(mockReader),
                EasyMock.eq(loaderContext))).andReturn(modelType);
        EasyMock.replay(mockLoader);
        registry.registerLoader(name, mockLoader);
        assertSame(modelType, registry.load(mockReader, ModelObject.class, loaderContext));
        EasyMock.verify(mockLoader);
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        name = new QName("http://mock", "test");
        ClassLoader cl = getClass().getClassLoader();
        loaderContext = new LoaderContextImpl(cl, null, null);
        mockMonitor = EasyMock.createMock(LoaderRegistryImpl.Monitor.class);
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        registry = new LoaderRegistryImpl(mockMonitor, xmlInputFactory);
        Map<QName, StAXElementLoader<?>> map = Collections.emptyMap();
        registry.setLoaders(map);
        mockLoader = EasyMock.createMock(StAXElementLoader.class);
        mockReader = EasyMock.createMock(XMLStreamReader.class);
        modelType = new ModelObject() {
        };
    }

}
