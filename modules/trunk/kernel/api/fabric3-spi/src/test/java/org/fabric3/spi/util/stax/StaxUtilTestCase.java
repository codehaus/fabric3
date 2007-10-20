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
package org.fabric3.spi.util.stax;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;

import junit.framework.TestCase;

/**
 * Test case for StaxHelper
 *
 * @version $Revision$ $Date$
 */
public class StaxUtilTestCase extends TestCase {

    public static final String XML = "<composite xmlns=\"http://www.osoa.org/xmlns/sca/1.0\" " +
            "xmlns:f3=\"http://fabric3.org/xmlns/sca/2.0-alpha\"/>";
    private XMLInputFactory xmlFactory;

    public StaxUtilTestCase(String name) {
        super(name);
    }

    public void testSerialize() throws XMLStreamException {

        InputStream in = getClass().getClassLoader().getResourceAsStream("test.composite");
        XMLStreamReader reader = xmlFactory.createXMLStreamReader(in);
        StaxUtil.serialize(reader);
        // TODO Do assertions
    }

    public void testCreateQName() throws Exception {
        XMLStreamReader reader = createReader(XML);
        reader.nextTag();
        QName qName = StaxUtil.createQName("f3:bar", reader);
        assertEquals("http://fabric3.org/xmlns/sca/2.0-alpha", qName.getNamespaceURI());
        assertEquals("bar", qName.getLocalPart());
    }

    public void testCreateQNameContext() throws Exception {
        XMLStreamReader reader = createReader(XML);
        reader.nextTag();
        QName qName = StaxUtil.createQName("bar", reader);
        assertEquals("http://www.osoa.org/xmlns/sca/1.0", qName.getNamespaceURI());
    }

    public void testCreateQNameInvalidPrefix() throws Exception {
        XMLStreamReader reader = createReader(XML);
        reader.nextTag();
        try {
            StaxUtil.createQName("bad:bar", reader);
            fail();
        } catch (InvalidPrefixException e) {
            //expected
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        xmlFactory = XMLInputFactory.newInstance();
    }

    private XMLStreamReader createReader(String xml) throws XMLStreamException {

        InputStream in = new ByteArrayInputStream(xml.getBytes());
        return xmlFactory.createXMLStreamReader(in);

    }
}
