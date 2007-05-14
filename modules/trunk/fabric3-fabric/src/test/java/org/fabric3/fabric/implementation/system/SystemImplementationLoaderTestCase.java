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
package org.fabric3.fabric.implementation.system;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.Constants;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.UnrecognizedElementException;

/**
 * @version $Rev$ $Date$
 */
public class SystemImplementationLoaderTestCase extends TestCase {

    public static final QName SYSTEM_IMPLEMENTATION = new QName(Constants.FABRIC3_SYSTEM_NS, "implementation.system");

    public void testLoad() throws Exception {
        LoaderRegistry registry = EasyMock.createNiceMock(LoaderRegistry.class);
        EasyMock.replay(registry);
        LoaderContext context = EasyMock.createMock(LoaderContext.class);
        EasyMock.expect(context.getClassLoader()).andReturn(getClass().getClassLoader());
        EasyMock.replay(context);
        XMLStreamReader reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getName()).andReturn(SYSTEM_IMPLEMENTATION);
        EasyMock.expect(reader.getAttributeValue((String) EasyMock.isNull(), EasyMock.eq("class")))
                .andReturn(getClass().getName());
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.replay(reader);
        SystemImplementationLoader loader = new SystemImplementationLoader(registry);
        SystemImplementation impl = loader.load(null, reader, context);
        assertEquals(getClass(), impl.getImplementationClass());
        EasyMock.verify(reader);
        EasyMock.verify(context);
    }

    public void testUnrecognizedElement() throws Exception {
        LoaderRegistry registry = EasyMock.createNiceMock(LoaderRegistry.class);
        EasyMock.replay(registry);
        LoaderContext context = EasyMock.createMock(LoaderContext.class);
        EasyMock.expect(context.getClassLoader()).andReturn(getClass().getClassLoader());
        EasyMock.replay(context);
        XMLStreamReader reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getName()).andReturn(SYSTEM_IMPLEMENTATION).atLeastOnce();
        EasyMock.expect(reader.getAttributeValue((String) EasyMock.isNull(), EasyMock.eq("class")))
                .andReturn(getClass().getName());
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.START_ELEMENT);
        EasyMock.expect(reader.getLocation()).andReturn(new MockLocation());
        EasyMock.replay(reader);
        SystemImplementationLoader loader = new SystemImplementationLoader(registry);
        try {
            loader.load(null, reader, context);
            fail();
        } catch (UnrecognizedElementException e) {
            // expected
        }
    }

    private class MockLocation implements Location {

        public int getLineNumber() {
            return 0;
        }

        public int getColumnNumber() {
            return 0;
        }

        public int getCharacterOffset() {
            return 0;
        }

        public String getPublicId() {
            return null;
        }

        public String getSystemId() {
            return null;
        }
    }

}
