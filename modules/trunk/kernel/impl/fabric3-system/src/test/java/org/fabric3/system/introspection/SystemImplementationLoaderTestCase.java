/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.system.introspection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.Namespaces;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.ImplementationProcessor;
import org.fabric3.system.scdl.SystemImplementation;

/**
 * @version $Rev$ $Date$
 */
public class SystemImplementationLoaderTestCase extends TestCase {

    public static final QName SYSTEM_IMPLEMENTATION = new QName(Namespaces.IMPLEMENTATION, "implementation.system");
    private IntrospectionContext context;
    private XMLStreamReader reader;
    private ImplementationProcessor<SystemImplementation> implementationProcessor;
    private SystemImplementationLoader loader;

    public void testLoad() throws Exception {
        implementationProcessor.introspect(EasyMock.isA(SystemImplementation.class), EasyMock.eq(context));
        EasyMock.replay(implementationProcessor);

        EasyMock.expect(reader.getAttributeCount()).andReturn(0);
        EasyMock.expect(reader.getName()).andReturn(SYSTEM_IMPLEMENTATION);
        EasyMock.expect(reader.getAttributeValue(null, "class")).andReturn(getClass().getName());
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.replay(reader);

        SystemImplementation impl = loader.load(reader, context);
        assertEquals(getClass().getName(), impl.getImplementationClass());
        EasyMock.verify(reader);
        EasyMock.verify(context);
        EasyMock.verify(implementationProcessor);
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        implementationProcessor = EasyMock.createMock(ImplementationProcessor.class);

        context = EasyMock.createMock(IntrospectionContext.class);
        EasyMock.replay(context);

        reader = EasyMock.createMock(XMLStreamReader.class);

        loader = new SystemImplementationLoader(implementationProcessor);
    }
}
