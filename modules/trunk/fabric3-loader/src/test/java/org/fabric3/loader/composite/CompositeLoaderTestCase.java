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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.osoa.sca.Constants.SCA_NS;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.scdl.Autowire;
import org.fabric3.scdl.CompositeComponentType;

/**
 * @version $Rev$ $Date$
 */
public class CompositeLoaderTestCase extends TestCase {
    public static final QName COMPOSITE = new QName(SCA_NS, "composite");
    private CompositeLoader loader;
    private QName name;
    private LoaderContext loaderContext;

    public void testLoadNameAndDefaultAutowire() throws Exception {
        XMLStreamReader reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getAttributeValue(null, "name")).andReturn(name.getLocalPart());
        EasyMock.expect(reader.getAttributeValue(null, "targetNamespace")).andReturn(name.getNamespaceURI());
        EasyMock.expect(reader.getAttributeValue(null, "autowire")).andReturn(null);
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(COMPOSITE);
        EasyMock.replay(reader, loaderContext);
        CompositeComponentType type = loader.load(reader, loaderContext);
        assertEquals(name, type.getName());
        assertEquals(Autowire.INHERITED, type.getAutowire());
        EasyMock.verify(reader, loaderContext);
    }

    public void testAutowire() throws Exception {
        XMLStreamReader reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getAttributeValue(null, "name")).andReturn(name.getLocalPart());
        EasyMock.expect(reader.getAttributeValue(null, "targetNamespace")).andReturn(name.getNamespaceURI());
        EasyMock.expect(reader.getAttributeValue(null, "autowire")).andReturn("true");
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(COMPOSITE);
        EasyMock.replay(reader, loaderContext);
        CompositeComponentType type = loader.load(reader, loaderContext);
        assertEquals(Autowire.ON, type.getAutowire());
        EasyMock.verify(reader, loaderContext);
    }

    protected void setUp() throws Exception {
        super.setUp();
        loaderContext = EasyMock.createMock(LoaderContext.class);
        EasyMock.expect(loaderContext.getSourceBase()).andStubReturn(null);
        EasyMock.expect(loaderContext.getTargetClassLoader()).andStubReturn(null);

        loader = new CompositeLoader(null, null, null, null, null, null, null);
        name = new QName("http://example.com", "composite");
    }
}
