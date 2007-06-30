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

import org.osoa.sca.Constants;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.model.type.ServiceDefinition;

import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 * @version $Rev$ $Date$
 */
public class ServiceLoaderPromoteTestCase extends TestCase {
    private static final QName SERVICE = new QName(Constants.SCA_NS, "service");
    private ServiceLoader loader;
    private XMLStreamReader mockReader;
    private LoaderContext ctx;

    public void testReferenceNoFragment() throws LoaderException, XMLStreamException {
        String name = "serviceDefinition";
        EasyMock.expect(mockReader.getName()).andReturn(SERVICE);
        EasyMock.expect(mockReader.getAttributeValue(null, "name")).andReturn(name);
        EasyMock.expect(mockReader.getAttributeValue(null, "promote")).andReturn("target");
        EasyMock.expect(mockReader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.expect(mockReader.getName()).andReturn(SERVICE);
        EasyMock.replay(mockReader);
        ServiceDefinition serviceDefinition = loader.load(mockReader, ctx);
        assertNotNull(serviceDefinition);
        assertEquals("target", serviceDefinition.getTarget().toString());
    }

    public void testReferenceWithFragment() throws LoaderException, XMLStreamException {
        String name = "serviceDefinition";
        EasyMock.expect(mockReader.getName()).andReturn(SERVICE);
        EasyMock.expect(mockReader.getAttributeValue(null, "name")).andReturn(name);
        EasyMock.expect(mockReader.getAttributeValue(null, "promote")).andReturn("target/fragment");
        EasyMock.expect(mockReader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.expect(mockReader.getName()).andReturn(SERVICE);
        EasyMock.replay(mockReader);
        ServiceDefinition serviceDefinition = loader.load(mockReader, ctx);
        assertNotNull(serviceDefinition);
        assertEquals("target#fragment", serviceDefinition.getTarget().toString());
    }


    protected void setUp() throws Exception {
        super.setUp();
        mockReader = EasyMock.createStrictMock(XMLStreamReader.class);
        LoaderRegistry mockRegistry = EasyMock.createMock(LoaderRegistry.class);
        loader = new ServiceLoader(mockRegistry);

        ctx = EasyMock.createMock(LoaderContext.class);
        EasyMock.replay(ctx);
    }
}
