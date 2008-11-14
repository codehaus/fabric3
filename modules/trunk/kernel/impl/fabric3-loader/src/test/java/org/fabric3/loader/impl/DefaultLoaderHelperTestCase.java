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
package org.fabric3.loader.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.fabric3.spi.introspection.xml.InvalidPrefixException;

/**
 * @version $Rev$ $Date$
 */
public class DefaultLoaderHelperTestCase extends TestCase {

    public static final String XML = "<composite xmlns=\"http://www.osoa.org/xmlns/sca/1.0\" " +
            "xmlns:f3-core=\"urn:fabric3.org:core\"/>";
    private XMLInputFactory xmlFactory;
    private DefaultLoaderHelper helper;

    public void testCreateQName() throws Exception {
        XMLStreamReader reader = createReader(XML);
        QName qName = helper.createQName("f3-core:bar", reader);
        assertEquals("urn:fabric3.org:core", qName.getNamespaceURI());
        assertEquals("bar", qName.getLocalPart());
    }

    public void testCreateQNameContext() throws Exception {
        XMLStreamReader reader = createReader(XML);
        QName qName = helper.createQName("bar", reader);
        assertEquals("http://www.osoa.org/xmlns/sca/1.0", qName.getNamespaceURI());
    }

    public void testCreateQNameInvalidPrefix() throws Exception {
        XMLStreamReader reader = createReader(XML);
        try {
            helper.createQName("bad:bar", reader);
            fail();
        } catch (InvalidPrefixException e) {
            //expected
        }
    }

    public void testComplexProperty() throws XMLStreamException {
        String xml = "<property xmlns:foo='http://foo.com'>"
                + "<foo:a>aValue</foo:a>"
                + "<foo:b>InterestingURI</foo:b>"
                + "</property>";

        XMLStreamReader reader = createReader(xml);
        Document value = helper.loadValue(reader);
        reader.close();

        NodeList childNodes = value.getDocumentElement().getChildNodes();
        assertEquals(2, childNodes.getLength());

        Element e = (Element) childNodes.item(0);
        assertEquals("http://foo.com", e.getNamespaceURI());
        assertEquals("a", e.getLocalName());
        assertEquals("aValue", e.getTextContent());
        e = (Element) childNodes.item(1);
        assertEquals("http://foo.com", e.getNamespaceURI());
        assertEquals("b", e.getLocalName());
        assertEquals("InterestingURI", e.getTextContent());
    }

    protected void setUp() throws Exception {
        super.setUp();
        xmlFactory = XMLInputFactory.newInstance();

        helper = new DefaultLoaderHelper();
    }

    private XMLStreamReader createReader(String xml) throws XMLStreamException {

        InputStream in = new ByteArrayInputStream(xml.getBytes());
        XMLStreamReader reader = xmlFactory.createXMLStreamReader(in);
        reader.nextTag();
        return reader;

    }
}
