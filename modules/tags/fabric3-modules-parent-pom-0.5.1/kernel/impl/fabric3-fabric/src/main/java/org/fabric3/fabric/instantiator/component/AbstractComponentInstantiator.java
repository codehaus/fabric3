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
package org.fabric3.fabric.instantiator.component;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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

import org.fabric3.fabric.instantiator.LogicalInstantiationException;
import org.fabric3.fabric.services.documentloader.DocumentLoader;
import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.OperationDefinition;
import org.fabric3.scdl.Property;
import org.fabric3.scdl.PropertyValue;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * @version $Revision$ $Date$
 */
public abstract class AbstractComponentInstantiator implements ComponentInstantiator {

    private static final DocumentBuilderFactory DOCUMENT_FACTORY;
    private static final XPathFactory XPATH_FACTORY;

    static {
        DOCUMENT_FACTORY = DocumentBuilderFactory.newInstance();
        DOCUMENT_FACTORY.setNamespaceAware(true);
        XPATH_FACTORY = XPathFactory.newInstance();
    }

    private final DocumentLoader documentLoader;

    protected AbstractComponentInstantiator(DocumentLoader documentLoader) {
        this.documentLoader = documentLoader;
    }

    /**
     * Transfers intents and policy sets declared in the SCDL to the service contract.
     *
     * @param serviceDefinition Service definition from the SCDL.
     */
    protected void addOperationLevelIntentsAndPolicies(LogicalService logicalService, ServiceDefinition serviceDefinition) {
        transferIntentsAndPolicies(logicalService.getDefinition().getServiceContract(), serviceDefinition.getOperations());
    }

    /**
     * Transfers intents and policy sets declared in the SCDL to the service contract.
     *
     * @param referenceDefinition Reference definition from the SCDL.
     */
    protected void addOperationLevelIntentsAndPolicies(LogicalReference logicalReference, ReferenceDefinition referenceDefinition) {
        transferIntentsAndPolicies(logicalReference.getDefinition().getServiceContract(), referenceDefinition.getOperations());
    }

    /**
     * Set the initial actual property values of a component.
     *
     * @param component  the component to initialize
     * @param definition the definition of the component
     * @throws org.fabric3.fabric.instantiator.LogicalInstantiationException
     *          if there was a problem initializing a property value
     */
    protected <I extends Implementation<?>> void initializeProperties(LogicalComponent<I> component,
                                                                      ComponentDefinition<I> definition)
            throws LogicalInstantiationException {

        Map<String, PropertyValue> propertyValues = definition.getPropertyValues();
        AbstractComponentType<?, ?, ?, ?> componentType = definition.getComponentType();

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
                    value = loadValueFromFile(property.getName(), propertyValue.getFile());
                } else if (propertyValue.getSource() != null) {
                    // get the value by evaluating an XPath against the composite properties
                    try {
                        value = deriveValueFromXPath(propertyValue.getSource(), component.getParent());
                    } catch (XPathExpressionException e) {
                        throw new LogicalInstantiationException(e.getMessage(), name, e);
                    }
                } else {
                    // use inline XML file
                    value = propertyValue.getValue();
                }

            }

            component.setPropertyValue(name, value);

        }

    }

    public Document deriveValueFromXPath(String source, final LogicalComponent<?> parent)
            throws XPathExpressionException {

        XPathVariableResolver variableResolver = new XPathVariableResolver() {
            public Object resolveVariable(QName qName) {
                String name = qName.getLocalPart();
                Document value = parent.getPropertyValue(name);
                if (value == null) {
                    return null;
                }
                return value.getDocumentElement();
            }
        };

        XPath xpath = XPATH_FACTORY.newXPath();
        xpath.setXPathVariableResolver(variableResolver);

        DocumentBuilder builder;
        try {
            builder = DOCUMENT_FACTORY.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError();
        }

        Document value = builder.newDocument();
        Element root = value.createElement("value");
        // TODO do we need to copy namespace declarations to this root
        value.appendChild(root);
        try {
            NodeList result = (NodeList) xpath.evaluate(source, root, XPathConstants.NODESET);
            for (int i = 0; i < result.getLength(); i++) {
                Node node = result.item(i);
                value.adoptNode(node);
                root.appendChild(node);
            }
        } catch (XPathExpressionException e) {
            // FIXME rethrow this for now, fix if people find it confusing
            // the Apache and Sun implementations of XPath throw a nested NullPointerException
            // if the xpath contains an unresolvable variable. It might be better to throw
            // a more descriptive cause, but that also might be confusing for people who
            // are used to this behaviour
            throw e;
        }
        return value;

    }

    protected Document loadValueFromFile(String name, URI file) throws InvalidPropertyFileException {
        try {
            return documentLoader.load(file);
        } catch (IOException e) {
            throw new InvalidPropertyFileException(e.getMessage(), name, e, file);
        } catch (SAXException e) {
            throw new InvalidPropertyFileException(e.getMessage(), name, e, file);
        }
    }

    private void transferIntentsAndPolicies(ServiceContract<?> serviceContract, List<OperationDefinition> operationDefinitions) {
        for (OperationDefinition operationDefinition : operationDefinitions) {
            for (Operation<?> operation : serviceContract.getOperations()) {
                if (operationDefinition.getName().equals(operation.getName())) {
                    for (QName intent : operationDefinition.getIntents()) {
                        operation.addIntent(intent);
                    }
                    for (QName policySet : operationDefinition.getPolicySets()) {
                        operation.addPolicySet(policySet);
                    }
                }
            }
        }
    }

}
