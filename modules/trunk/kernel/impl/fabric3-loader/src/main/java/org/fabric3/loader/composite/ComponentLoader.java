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
package org.fabric3.loader.composite;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.fabric3.scdl.Autowire;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ComponentReference;
import org.fabric3.scdl.ComponentService;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.PropertyValue;
import org.fabric3.spi.Constants;
import org.fabric3.spi.loader.InvalidValueException;
import org.fabric3.spi.loader.Loader;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.PolicyHelper;
import org.fabric3.spi.loader.StAXElementLoader;

/**
 * Loads a component definition from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class ComponentLoader implements StAXElementLoader<ComponentDefinition<?>> {

    private static final QName COMPONENT = new QName(SCA_NS, "component");
    private static final QName PROPERTY = new QName(SCA_NS, "property");
    private static final QName SERVICE = new QName(SCA_NS, "service");
    private static final QName REFERENCE = new QName(SCA_NS, "reference");

    private static final DocumentBuilder documentBuilder;

    static {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            documentBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final Loader loader;
    private final StAXElementLoader<PropertyValue> propertyValueLoader;
    private final StAXElementLoader<ComponentReference> referenceLoader;
    private final StAXElementLoader<ComponentService> serviceLoader;
    private final PolicyHelper policyHelper;

    public ComponentLoader(@Reference Loader loader,
                           @Reference(name = "propertyValue")StAXElementLoader<PropertyValue> propertyValueLoader,
                           @Reference(name = "reference")StAXElementLoader<ComponentReference> referenceLoader,
                           @Reference(name = "service")StAXElementLoader<ComponentService> serviceLoader,
                           @Reference(name = "policyHelper")PolicyHelper policyHelper) {
        this.loader = loader;
        this.propertyValueLoader = propertyValueLoader;
        this.referenceLoader = referenceLoader;
        this.serviceLoader = serviceLoader;
        this.policyHelper = policyHelper;
    }

    public ComponentDefinition<?> load(XMLStreamReader reader, LoaderContext context)
            throws XMLStreamException, LoaderException {

        String name = reader.getAttributeValue(null, "name");
        Autowire autowire = Autowire.fromString(reader.getAttributeValue(null, "autowire"));
        URI runtimeId = loadRuntimeId(reader);
        Integer initLevel = loadInitLevel(reader);
        Document key = loadKey(reader);

        ComponentDefinition<Implementation<?>> componentDefinition = new ComponentDefinition<Implementation<?>>(name);
        componentDefinition.setAutowire(autowire);
        componentDefinition.setRuntimeId(runtimeId);
        componentDefinition.setInitLevel(initLevel);
        componentDefinition.setKey(key);

        policyHelper.loadPolicySetsAndIntents(componentDefinition, reader);

        Implementation<?> impl = loadImplementation(reader, context);
        componentDefinition.setImplementation(impl);

        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                QName qname = reader.getName();
                if (PROPERTY.equals(qname)) {
                    PropertyValue value = propertyValueLoader.load(reader, context);
                    if (impl.getComponentType().getProperties().get(value.getName()) == null) {
                        // ensure the property exists
                        throw new PropertyNotFoundException(value.getName());
                    }
                    componentDefinition.add(value);
                } else if (REFERENCE.equals(qname)) {
                    ComponentReference reference = referenceLoader.load(reader, context);
                    if (impl.getComponentType().getReferences().get(reference.getName()) == null) {
                        // ensure the reference exists
                        throw new ReferenceNotFoundException(reference.getName());
                    }
                    componentDefinition.add(reference);
                } else if (SERVICE.equals(qname)) {
                    ComponentService service = serviceLoader.load(reader, context);
                    if (impl.getComponentType().getServices().get(service.getName()) == null) {
                        // ensure the reference exists
                        throw new ServiceNotFoundException(service.getName());
                    }
                    componentDefinition.add(service);
                } else {
                    // Unknown extension element - ignore
                    LoaderUtil.skipToEndElement(reader);
                }
                break;
            case END_ELEMENT:
                assert COMPONENT.equals(reader.getName());
                return componentDefinition;
            }
        }
    }

    /**
     * Loads the key when the component is wired to a map based reference.
     *
     * @param reader a reader positioned on the element containing the key definition
     * @return a Document containing the key value.
     */
    private Document loadKey(XMLStreamReader reader) {

        String key = reader.getAttributeValue(Constants.FABRIC3_NS, "key");
        if (key == null) {
            return null;
        }

        // create a document with a root element to hold the key value
        Document document = documentBuilder.newDocument();
        Element element = document.createElement("key");
        document.appendChild(element);

        // TODO: we should copy all in-context namespaces to the declaration if we can find what they are
        // in the mean time, see if the value looks like it might contain a prefix
        int index = key.indexOf(':');
        if (index != -1) {
            String prefix = key.substring(0, index);
            String uri = reader.getNamespaceURI(prefix);
            if (uri != null) {
                element.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:" + prefix, uri);
            }
        }
        // set the text value
        element.appendChild(document.createTextNode(key));
        return document;
    }

    /*
     * Loads the init level.
     */
    private Integer loadInitLevel(XMLStreamReader reader) throws InvalidValueException {
        String initLevel = reader.getAttributeValue(null, "initLevel");
        if (initLevel == null || initLevel.length() == 0) {
            return null;
        } else {
            try {
                return Integer.valueOf(initLevel);
            } catch (NumberFormatException e) {
                throw new InvalidValueException(initLevel, "initValue", e);
            }
        }
    }

    /*
     * Loads the runtime id.
     */
    private URI loadRuntimeId(XMLStreamReader reader) throws InvalidValueException {
        String runtimeAttr = reader.getAttributeValue(null, "runtimeId");
        if (runtimeAttr == null) {
            return null;
        } else {
            try {
                return new URI(runtimeAttr);
            } catch (URISyntaxException e) {
                throw new InvalidValueException(runtimeAttr, "runtimeId", e);
            }
        }
    }

    /*
     * Loads the component implementation.
     */
    private Implementation<?> loadImplementation(XMLStreamReader reader, LoaderContext context)
            throws XMLStreamException, LoaderException {
        reader.nextTag();
        return loader.load(reader, Implementation.class, context);
    }

}
