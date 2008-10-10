/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
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
package org.fabric3.fabric.instantiator.component;

import java.net.URI;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.fabric3.fabric.instantiator.LogicalChange;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.Implementation;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * @version $Rev$ $Date$
 */
public class AssemblyPropertyTestCase extends TestCase {
    private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();
    private AbstractComponentInstantiator componentInstantiator;
    private LogicalComponent<CompositeImplementation> domain;
    private Element root;
    private Document property;

    public void testSimpleProperty() throws Exception {
        root.setTextContent("Hello World");
        Document value = componentInstantiator.deriveValueFromXPath("$domain", domain);
        Node child = value.getDocumentElement().getFirstChild();
        assertEquals(Node.TEXT_NODE, child.getNodeType());
        assertEquals("Hello World", child.getTextContent());
    }

    public void testComplexProperty() throws Exception {
        Element http = property.createElement("http");
        root.appendChild(http);
        Element port = property.createElement("port");
        http.appendChild(port);
        port.setTextContent("8080");
        Document value = componentInstantiator.deriveValueFromXPath("$domain/http/port", domain);
        Node child = value.getDocumentElement().getFirstChild();
        assertEquals(Node.ELEMENT_NODE, child.getNodeType());
        assertEquals("port", child.getNodeName());
        assertEquals("8080", child.getTextContent());
    }

    public void testAttributeProperty() throws Exception {
        Element http = property.createElement("http");
        http.setAttribute("port", "8080");
        root.appendChild(http);
        Document value = componentInstantiator.deriveValueFromXPath("$domain/http/@port", domain);
        Node child = value.getDocumentElement().getFirstChild();
        assertEquals(Node.ELEMENT_NODE, child.getNodeType());
        assertEquals("port", child.getNodeName());
        assertEquals("8080", child.getTextContent());
    }

    public void testComplexPropertyWithMultipleValues() throws Exception {
        Element http1 = property.createElement("http");
        root.appendChild(http1);
        http1.setAttribute("index", "1");
        Element http2 = property.createElement("http");
        root.appendChild(http2);
        http2.setAttribute("index", "2");
        Document value = componentInstantiator.deriveValueFromXPath("$domain/http", domain);
        Node child = value.getDocumentElement();
        NodeList list = child.getChildNodes();
        assertEquals(2, list.getLength());
        assertEquals("http", list.item(0).getNodeName());
        assertEquals("1", ((Element) list.item(0)).getAttribute("index"));
        assertEquals("http", list.item(1).getNodeName());
        assertEquals("2", ((Element) list.item(1)).getAttribute("index"));
    }

    public void testUnknownVariable() {
        try {
            componentInstantiator.deriveValueFromXPath("$foo", domain);
            fail();
        } catch (XPathExpressionException e) {
            // this is ok?
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        componentInstantiator = new AbstractComponentInstantiator(null) {

            public <I extends Implementation<?>> LogicalComponent<I> instantiate(LogicalCompositeComponent parent,
                                                                                 Map<String, Document> properties,
                                                                                 ComponentDefinition<I> definition,
                                                                                 LogicalChange change) {
                return null;
            }
        };
        domain = new LogicalComponent<CompositeImplementation>(URI.create("fabric3://domain"), null, null);

        property = FACTORY.newDocumentBuilder().newDocument();
        root = property.createElement("value");
        property.appendChild(root);
        domain.setPropertyValue("domain", property);
    }

}
