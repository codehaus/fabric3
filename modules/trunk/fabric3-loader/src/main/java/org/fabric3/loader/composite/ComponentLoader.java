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
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.loader.InvalidValueException;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.scdl.Autowire;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ComponentReference;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.PropertyValue;

/**
 * Loads a component definition from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class ComponentLoader implements StAXElementLoader<ComponentDefinition<?>> {
    private static final QName COMPONENT = new QName(SCA_NS, "component");
    private static final QName PROPERTY = new QName(SCA_NS, "property");
    private static final QName REFERENCE = new QName(SCA_NS, "reference");

    private final LoaderRegistry registry;
    private final StAXElementLoader<PropertyValue> propertyValueLoader;
    private final StAXElementLoader<ComponentReference> referenceLoader;

    public ComponentLoader(@Reference LoaderRegistry registry,
                           @Reference (name = "propertyValue") StAXElementLoader<PropertyValue> propertyValueLoader,
                           @Reference (name = "reference") StAXElementLoader<ComponentReference> referenceLoader) {
        this.registry = registry;
        this.propertyValueLoader = propertyValueLoader;
        this.referenceLoader = referenceLoader;
    }

    public QName getXMLType() {
        return COMPONENT;
    }

    public ComponentDefinition<?> load(XMLStreamReader reader, LoaderContext context)
            throws XMLStreamException, LoaderException {
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
                    PropertyValue value = propertyValueLoader.load(reader, context);
                    componentDefinition.add(value);
                } else if (REFERENCE.equals(qname)) {
                    ComponentReference reference = referenceLoader.load(reader, context);
                    componentDefinition.add(reference);
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
        return registry.load(reader, Implementation.class, context);
    }

}
