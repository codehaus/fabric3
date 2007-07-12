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
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

/**
 * Test case for StaxHelper
 *
 * @version $Revision$ $Date$
 */
public class StaxUtilTestCase extends TestCase {

    public static final String XML = "<composite xmlns=\"http://www.osoa.org/xmlns/sca/1.0\" " +
            "xmlns:f3=\"http://fabric3.org/xmlns/sca/2.0-alpha\"/>";

    public StaxUtilTestCase(String name) {
        super(name);
    }

    public void testSerialize() throws XMLStreamException {

        InputStream in = getClass().getClassLoader().getResourceAsStream("test.composite");
        XMLStreamReader reader = StaxUtil.createReader(in);
        StaxUtil.serialize(reader);
        // TODO Do assertions
    }

    public void testGetDocumentElementQName() throws XMLStreamException {
        InputStream in = getClass().getClassLoader().getResourceAsStream("test.composite");
        XMLStreamReader reader = StaxUtil.createReader(in);
        String xml = StaxUtil.serialize(reader);
        QName qname = StaxUtil.getDocumentElementQName(xml);
        assertEquals("http://www.osoa.org/xmlns/sca/1.0", qname.getNamespaceURI());
        assertEquals("composite", qname.getLocalPart());
    }

    public void testCreateQName() throws Exception {
        XMLStreamReader reader = StaxUtil.createReader(XML);
        reader.nextTag();
        QName qName = StaxUtil.createQName("f3:bar", reader);
        assertEquals("http://fabric3.org/xmlns/sca/2.0-alpha", qName.getNamespaceURI());
        assertEquals("bar", qName.getLocalPart());
    }

    public void testCreateQNameContext() throws Exception {
        XMLStreamReader reader = StaxUtil.createReader(XML);
        reader.nextTag();
        QName qName = StaxUtil.createQName("bar", reader);
        assertEquals("http://www.osoa.org/xmlns/sca/1.0", qName.getNamespaceURI());
    }

    public void testCreateQNameInvalidPrefix() throws Exception {
        XMLStreamReader reader = StaxUtil.createReader(XML);
        reader.nextTag();
        try {
            StaxUtil.createQName("bad:bar", reader);
            fail();
        } catch (InvalidPrefixException e) {
            //expected
        }
    }

}
