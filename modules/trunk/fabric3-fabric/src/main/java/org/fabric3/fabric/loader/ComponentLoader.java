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
package org.fabric3.fabric.loader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.spi.loader.InvalidReferenceException;
import org.fabric3.spi.loader.InvalidValueException;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.MissingImplementationException;
import org.fabric3.spi.model.type.Autowire;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.DataType;
import org.fabric3.spi.model.type.Implementation;
import org.fabric3.spi.model.type.ModelObject;
import org.fabric3.spi.model.type.PropertyValue;
import org.fabric3.spi.model.type.ReferenceTarget;
import org.fabric3.spi.model.type.XSDSimpleType;
import org.fabric3.transform.xml.Stream2Element2;

/**
 * Loads a component definition from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class ComponentLoader extends LoaderExtension<ComponentDefinition<?>> {
    private static final QName COMPONENT = new QName(SCA_NS, "component");
    private static final QName PROPERTY = new QName(SCA_NS, "property");
    private static final QName REFERENCE = new QName(SCA_NS, "reference");

    private final Stream2Element2 stream2Element;
    private final DocumentBuilderFactory documentBuilderFactory;

    @Constructor
    public ComponentLoader(@Reference LoaderRegistry registry) {
        super(registry);
        // TODO get the transformers by injection
        stream2Element = new Stream2Element2(PROPERTY);
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
    }

    public QName getXMLType() {
        return COMPONENT;
    }

    public ComponentDefinition<?> load(XMLStreamReader reader, LoaderContext context)
            throws XMLStreamException, LoaderException {
        assert COMPONENT.equals(reader.getName());
        String name = reader.getAttributeValue(null, "name");
        Autowire autowire = loadAutowire(reader);
        URI runtimeId = loadRuntimeId(reader);
        Integer initLevel = loadInitLevel(reader);

        Implementation<?> impl = loadImplementation(reader, context);

        ComponentDefinition<Implementation<?>> componentDefinition =
                new ComponentDefinition<Implementation<?>>(name, impl);
        componentDefinition.setAutowire(autowire);
        componentDefinition.setRuntimeId(runtimeId);
        componentDefinition.setInitLevel(initLevel);

        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                QName qname = reader.getName();
                if (PROPERTY.equals(qname)) {
                    PropertyValue value = loadPropertyValue(reader, context);
                    componentDefinition.add(value);
                    // reader.next();
                } else if (REFERENCE.equals(qname)) {
                    loadReference(reader, componentDefinition, context);
                    reader.next();
                } else {
                    LoaderUtil.skipToEndElement(reader);
                }
                break;
            case END_ELEMENT:
                assert COMPONENT.equals(reader.getName());
                return componentDefinition;
            }
        }
    }

    protected Integer loadInitLevel(XMLStreamReader reader) throws InvalidValueException {
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

    protected Autowire loadAutowire(XMLStreamReader reader) {
        String autowire = reader.getAttributeValue(null, "autowire");
        if (autowire == null) {
            return Autowire.INHERITED;
        } else if (Boolean.parseBoolean(autowire)) {
            return Autowire.ON;
        } else {
            return Autowire.OFF;
        }
    }

    protected URI loadRuntimeId(XMLStreamReader reader) throws InvalidValueException {
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

    protected Implementation<?> loadImplementation(XMLStreamReader reader, LoaderContext context)
            throws XMLStreamException, LoaderException {
        reader.nextTag();
        ModelObject type = registry.load(reader, context);
        if (!(type instanceof Implementation)) {
            throw new MissingImplementationException();
        }
        return (Implementation<?>) type;
    }

    protected PropertyValue loadPropertyValue(XMLStreamReader reader, LoaderContext context)
            throws XMLStreamException, LoaderException {

        String name = reader.getAttributeValue(null, "name");
        if (name == null || name.length() == 0) {
            throw new InvalidNameException(name);
        }

        PropertyValue propertyValue;

        String source = reader.getAttributeValue(null, "source");
        String file = reader.getAttributeValue(null, "file");
        if (source != null) {
            propertyValue = new PropertyValue(name, source);
        } else if (file != null) {
            try {
                URI uri = new URI(file);
                if (!uri.isAbsolute()) {
                    uri = context.getScdlLocation().toURI().resolve(uri);
                }
                propertyValue = new PropertyValue(name, uri);
            } catch (URISyntaxException e) {
                throw new InvalidValueException(file, name, e);
            }
        } else {
            propertyValue = loadInlinePropertyValue(name, reader);
        }
        // LoaderUtil.skipToEndElement(reader);
        return propertyValue;
    }

    protected PropertyValue loadInlinePropertyValue(String name, XMLStreamReader reader)
            throws InvalidValueException, XMLStreamException {
        DataType<QName> dataType;
        String type = reader.getAttributeValue(null, "type");
        String element = reader.getAttributeValue(null, "element");
        if (type != null) {
            if (element != null) {
                throw new InvalidValueException("Cannot supply both type and element for property ", name);
            }
            // TODO support type attribute
            throw new UnsupportedOperationException();
        } else if (element != null) {
            // TODO support element attribute
            throw new UnsupportedOperationException();
        } else {
            dataType = new XSDSimpleType(Element.class, XSDSimpleType.STRING);
        }

        DocumentBuilder docBuilder;
        try {
            docBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError();
        }
        Document value = docBuilder.newDocument();
        Element valueElement = value.createElement("value");
        value.appendChild(valueElement);
        stream2Element.transform(reader, valueElement);
        return new PropertyValue(name, dataType, value);
    }

    protected void loadReference(XMLStreamReader reader,
                                 ComponentDefinition<?> componentDefinition,
                                 LoaderContext context) throws XMLStreamException, LoaderException {
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            throw new InvalidReferenceException("No name specified");
        }
        String target = reader.getAttributeValue(null, "target");
        boolean autowire = Boolean.parseBoolean(reader.getAttributeValue(null, "autowire"));
        URI componentId = URI.create(componentDefinition.getName()); //FIXME we should not need to create a URI here
        List<URI> uris = new ArrayList<URI>();
        if (target != null) {
            StringTokenizer tokenizer = new StringTokenizer(target);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                QualifiedName qName = new QualifiedName(token);
                uris.add(componentId.resolve(qName.getFragment()));
            }
        }

        ReferenceTarget referenceTarget = new ReferenceTarget();
        referenceTarget.setReferenceName(componentId.resolve('#' + name));
        referenceTarget.setAutowire(autowire);
        componentDefinition.add(referenceTarget);
        for (URI uri : uris) {
            referenceTarget.addTarget(uri);
        }
    }

}
