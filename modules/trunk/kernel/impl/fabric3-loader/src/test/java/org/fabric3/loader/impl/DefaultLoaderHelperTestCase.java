/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import org.fabric3.loader.impl.DefaultLoaderHelper;
import org.fabric3.loader.common.InvalidPrefixException;

/**
 * @version $Rev$ $Date$
 */
public class DefaultLoaderHelperTestCase extends TestCase {

    public static final String XML = "<composite xmlns=\"http://www.osoa.org/xmlns/sca/1.0\" " +
            "xmlns:f3=\"http://fabric3.org/xmlns/sca/2.0-alpha\"/>";
    private XMLInputFactory xmlFactory;
    private DefaultLoaderHelper helper;

    public void testCreateQName() throws Exception {
        XMLStreamReader reader = createReader(XML);
        QName qName = helper.createQName("f3:bar", reader);
        assertEquals("http://fabric3.org/xmlns/sca/2.0-alpha", qName.getNamespaceURI());
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
