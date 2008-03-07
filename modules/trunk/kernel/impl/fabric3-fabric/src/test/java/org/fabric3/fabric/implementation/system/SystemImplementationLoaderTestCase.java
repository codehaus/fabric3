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
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.Constants;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderRegistry;

/**
 * @version $Rev$ $Date$
 */
public class SystemImplementationLoaderTestCase extends TestCase {

    public static final QName SYSTEM_IMPLEMENTATION = new QName(Constants.FABRIC3_SYSTEM_NS, "implementation.system");
    private LoaderRegistry registry;
    private IntrospectionContext context;
    private XMLStreamReader reader;
    private SystemComponentTypeLoader componentTypeLoader;
    private SystemImplementationLoader loader;

    public void testLoad() throws Exception {
        EasyMock.replay(registry);
        componentTypeLoader.load(EasyMock.isA(SystemImplementation.class), EasyMock.eq(context));
        EasyMock.replay(componentTypeLoader);

        EasyMock.expect(reader.getName()).andReturn(SYSTEM_IMPLEMENTATION);
        EasyMock.expect(reader.getAttributeValue(null, "class")).andReturn(getClass().getName());
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.replay(reader);

        SystemImplementation impl = loader.load(reader, context);
        assertEquals(getClass().getName(), impl.getImplementationClass());
        EasyMock.verify(reader);
        EasyMock.verify(context);
        EasyMock.verify(componentTypeLoader);
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        registry = EasyMock.createMock(LoaderRegistry.class);
        componentTypeLoader = EasyMock.createMock(SystemComponentTypeLoader.class);

        context = EasyMock.createMock(IntrospectionContext.class);
        EasyMock.replay(context);

        reader = EasyMock.createMock(XMLStreamReader.class);

        loader = new SystemImplementationLoader(registry, componentTypeLoader);
    }
}
