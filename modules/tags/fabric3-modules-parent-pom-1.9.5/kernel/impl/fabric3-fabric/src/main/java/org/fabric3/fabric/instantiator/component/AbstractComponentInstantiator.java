/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
*/
package org.fabric3.fabric.instantiator.component;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.PropertyValue;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalProperty;

/**
 * Contains functionality common to different component instantiators.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractComponentInstantiator {
    private static final DocumentBuilderFactory DOCUMENT_FACTORY;
    private static final XPathFactory XPATH_FACTORY;

    static {
        DOCUMENT_FACTORY = DocumentBuilderFactory.newInstance();
        DOCUMENT_FACTORY.setNamespaceAware(true);
        XPATH_FACTORY = XPathFactory.newInstance();
    }


    /**
     * Set the initial actual property values of a component.
     *
     * @param component  the component to initialize
     * @param definition the definition of the component
     * @param context    the instantiation context
     */
    protected void initializeProperties(LogicalComponent<?> component, ComponentDefinition<?> definition, InstantiationContext context) {

        Map<String, PropertyValue> propertyValues = definition.getPropertyValues();
        ComponentType componentType = definition.getComponentType();
        LogicalCompositeComponent parent = component.getParent();

        for (Property property : componentType.getProperties().values()) {
            String name = property.getName();
            PropertyValue propertyValue = propertyValues.get(name);

            Document value;
            if (propertyValue == null) {
                // use default value from component type
                value = property.getDefaultValue();
            } else {
                // the spec defines the following sequence
                if (propertyValue.getFile() != null) {
                    // load the value from an external resource
                    value = loadValueFromFile(property.getName(), propertyValue.getFile(), component, context);
                } else if (propertyValue.getSource() != null) {
                    // get the value by evaluating an XPath against the composite properties
                    try {
                        NamespaceContext nsContext = propertyValue.getNamespaceContext();
                        value = deriveValueFromXPath(propertyValue, parent, nsContext);
                    } catch (PropertyTypeException e) {
                        InvalidProperty error = new InvalidProperty(name, component, e);
                        context.addError(error);
                        return;
                    }
                } else {
                    // use inline XML file
                    value = propertyValue.getValue();
                }

            }
            if (property.isRequired() && value == null) {
                // The XPath expression returned an empty value. Since the property is required, throw an exception
                PropertySourceNotFound error = new PropertySourceNotFound(name, component);
                context.addError(error);
            } else if (!property.isRequired() && value == null) {
                // The XPath expression returned an empty value. Since the property is optional, ignore it
                continue;
            } else {
                // set the property value
                boolean many = property.isMany();
                LogicalProperty logicalProperty;
                QName type = property.getType();
                if (type == null) {
                    logicalProperty = new LogicalProperty(name, value, many, component);
                } else {
                    logicalProperty = new LogicalProperty(name, value, many, type, component);
                }
                component.setProperties(logicalProperty);
            }

        }

    }

    Document deriveValueFromXPath(final PropertyValue propertyValue, final LogicalComponent<?> parent, NamespaceContext nsContext)
            throws PropertyTypeException {

        XPathVariableResolver variableResolver = new XPathVariableResolver() {
            public Object resolveVariable(QName qName) {
                String name = qName.getLocalPart();
                LogicalProperty property = parent.getProperties(name);
                if (property == null) {
                    return null;
                }
                if (propertyValue.getType() != null && property.getType() != null && !propertyValue.getType().equals(property.getType())) {
                    throw new PropertyTypeException("Property types are incompatible:" + name + " and " + propertyValue.getName());
                }
                Document value = property.getValue();
                if (value == null || value.getDocumentElement().getChildNodes().getLength() == 0) {
                    return null;
                }
                // select the first value
                return value.getDocumentElement();
            }
        };

        XPath xpath = XPATH_FACTORY.newXPath();
        xpath.setXPathVariableResolver(variableResolver);
        xpath.setNamespaceContext(nsContext);

        DocumentBuilder builder;
        try {
            builder = DOCUMENT_FACTORY.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }

        Document document = builder.newDocument();
        Element root = document.createElement("values");
        document.appendChild(root);
        String source = propertyValue.getSource();
        try {
            source = parseSource(source);
            NodeList result = (NodeList) xpath.evaluate(source, document, XPathConstants.NODESET);

            if (result.getLength() == 0) {
                return null;
            }

            for (int i = 0; i < result.getLength(); i++) {
                Node node = result.item(i);
                Element value;
                if (!"value".equals(node.getNodeName())) {
                    value = document.createElement("value");
                    root.appendChild(value);
                } else {
                    // value is already specified as the root of the XPath select, append directly to it
                    value = root;
                }
                // clone the node and copy the namespaces as the original may be accessed multiple times
                Node cloned = node.cloneNode(true);
                NamespaceHelper.copyNamespaces(node, value);
                document.adoptNode(cloned);
                short type = cloned.getNodeType();
                if (Node.ELEMENT_NODE == type || Node.TEXT_NODE == type) {
                    value.appendChild(cloned);
                } else if (Node.ATTRIBUTE_NODE == type) {
                    // convert the attribute to an element in the property DOM
                    Element element = document.createElement(cloned.getNodeName());
                    element.setTextContent(cloned.getNodeValue());
                    value.appendChild(element);
                } else {
                    throw new XPathExpressionException("Unsupported node type: " + type);
                }
            }
        } catch (XPathExpressionException e) {
            if (e.getCause() instanceof TransformerException) {
                String message = e.getCause().getMessage();
                if (message.startsWith("resolveVariable for variable") || message.endsWith("returning null")) {
                    return null;
                }
            }
            throw new PropertyTypeException(e);
        }
        return document;

    }

    public Document loadValueFromFile(String name, URI fileUri, LogicalComponent<?> parent, InstantiationContext context) {
        try {
            DocumentBuilder builder = DOCUMENT_FACTORY.newDocumentBuilder();
            Document document = builder.parse(fileUri.toString());
            Element root = document.getDocumentElement();
            // support documents in various formats: with a root <values>, <value>, or no root element
            if (!"values".equals(root.getNodeName())) {
                if ("value".equals(root.getNodeName())) {
                    Element newRoot = document.createElement("values");
                    document.removeChild(root);
                    document.appendChild(newRoot);
                    newRoot.appendChild(root);
                } else {
                    Element newRoot = document.createElement("values");
                    document.removeChild(root);
                    document.appendChild(newRoot);
                    Element value = document.createElement("value");
                    newRoot.appendChild(value);
                    value.appendChild(root);
                }
            }
            return document;
        } catch (IOException e) {
            InvalidPropertyFile error = new InvalidPropertyFile(name, parent, e, fileUri);
            context.addError(error);
            return null;
        } catch (SAXException e) {
            InvalidPropertyFile error = new InvalidPropertyFile(name, parent, e, fileUri);
            context.addError(error);
            return null;
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }

    private String parseSource(String source) {
        if (source.startsWith("$")) {
            // ASM_5039 complex type with multiple values: ensure all values are selected
            int index = source.indexOf("/");
            if (index > 0 && index < source.length() - 2 && !source.substring(index + 1, index + 2).equals("/")) {
                source = source.substring(0, index) + "/" + source.substring(index);
            }
        }
        return source;
    }


}
