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
package org.fabric3.fabric.assembly;

import java.net.URI;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.model.instance.LogicalComponent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version $Rev$ $Date$
 */
public class AssemblyPropertyTestCase extends TestCase {
    private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();
    private AbstractAssembly assembly;
    private LogicalComponent<CompositeImplementation> domain;
    private Element root;

    public void testSimpleProperty() throws Exception {
        root.setTextContent("Hello World");
        Document value = assembly.deriveValueFromXPath("$domain", domain);
        Node child = value.getDocumentElement().getFirstChild();
        assertEquals(Node.TEXT_NODE, child.getNodeType());
        assertEquals("Hello World", child.getTextContent());
    }

    public void testComplexPropertyEvaluatingToElement() throws Exception {
        Document property = root.getOwnerDocument();
        Element http = property.createElement("http");
        root.appendChild(http);
        Element port = property.createElement("port");
        http.appendChild(port);
        port.setTextContent("8080");
        Document value = assembly.deriveValueFromXPath("$domain/http", domain);
        Node child = value.getDocumentElement().getFirstChild();
        assertEquals(Node.ELEMENT_NODE, child.getNodeType());
        assertEquals("port", child.getNodeName());
        assertEquals("8080", child.getTextContent());
    }

    public void testComplexPropertyEvaluatingToText() throws Exception {
        Document property = root.getOwnerDocument();
        Element http = property.createElement("http");
        root.appendChild(http);
        Element port = property.createElement("port");
        http.appendChild(port);
        port.setTextContent("8080");
        Document value = assembly.deriveValueFromXPath("$domain/http/port", domain);
        Node child = value.getDocumentElement().getFirstChild();
        assertEquals(Node.TEXT_NODE, child.getNodeType());
        assertEquals("8080", child.getTextContent());
    }

    public void testUnknownVariable() {
        try {
            assembly.deriveValueFromXPath("$foo", domain);
            fail();
        } catch (XPathExpressionException e) {
            // this is ok?
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        assembly = new MockAssembly();
        domain = new LogicalComponent<CompositeImplementation>(assembly.domainUri, assembly.domainUri, null, null);

        Document value = FACTORY.newDocumentBuilder().newDocument();
        root = value.createElement("value");
        value.appendChild(root);
        domain.setPropertyValue("domain", value);
    }

    private static class MockAssembly extends AbstractAssembly {
        public MockAssembly() {
            super(URI.create("sca://./domain"), null, null, null, null, null, null, null);

        }
    }
}
