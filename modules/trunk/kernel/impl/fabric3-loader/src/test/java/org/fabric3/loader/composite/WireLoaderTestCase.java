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
package org.fabric3.loader.composite;

import java.net.URI;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamConstants;

import junit.framework.TestCase;

import org.fabric3.scdl.WireDefinition;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.loader.impl.DefaultLoaderHelper;

import org.easymock.EasyMock;

/**
 * @version $Rev$ $Date$
 */
public class WireLoaderTestCase extends TestCase {
    private WireLoader wireLoader;
    private XMLStreamReader reader;

    public void testWithNoServiceName() throws LoaderException, XMLStreamException {
        EasyMock.expect(reader.getAttributeCount()).andReturn(0);
        EasyMock.expect(reader.getAttributeValue(null, "source")).andReturn("source");
        EasyMock.expect(reader.getAttributeValue(null, "target")).andReturn("target");
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.replay(reader);
        WireDefinition definition = wireLoader.load(reader, null);
        EasyMock.verify(reader);
        assertEquals(URI.create("source"), definition.getSource());
        assertEquals(URI.create("target"), definition.getTarget());
    }

    public void testWithServiceName() throws LoaderException, XMLStreamException {
        EasyMock.expect(reader.getAttributeCount()).andReturn(0);
        EasyMock.expect(reader.getAttributeValue(null, "source")).andReturn("source/s");
        EasyMock.expect(reader.getAttributeValue(null, "target")).andReturn("target/t");
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.replay(reader);
        WireDefinition definition = wireLoader.load(reader, null);
        EasyMock.verify(reader);
        assertEquals(URI.create("source#s"), definition.getSource());
        assertEquals(URI.create("target#t"), definition.getTarget());
    }

    protected void setUp() throws Exception {
        super.setUp();
        LoaderHelper helper = new DefaultLoaderHelper();
        wireLoader = new WireLoader(helper);
        reader = EasyMock.createStrictMock(XMLStreamReader.class);
    }
}
