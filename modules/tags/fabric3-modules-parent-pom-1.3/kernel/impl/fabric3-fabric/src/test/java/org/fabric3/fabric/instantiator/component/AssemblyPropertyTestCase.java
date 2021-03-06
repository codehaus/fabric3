/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
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

import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.Implementation;
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
                                                                                 InstantiationContext context) {
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
