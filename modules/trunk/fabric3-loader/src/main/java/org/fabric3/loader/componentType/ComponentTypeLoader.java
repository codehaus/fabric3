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
package org.fabric3.loader.componentType;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.model.type.ComponentType;
import org.fabric3.spi.model.type.ModelObject;
import org.fabric3.spi.model.type.Property;
import org.fabric3.spi.model.type.ReferenceDefinition;
import org.fabric3.spi.model.type.ServiceDefinition;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class ComponentTypeLoader implements StAXElementLoader<ComponentType<ServiceDefinition, ReferenceDefinition, Property<?>>> {
    private static final QName COMPONENT_TYPE = new QName(SCA_NS, "componentType");
    private static final QName PROPERTY = new QName(SCA_NS, "property");
    private static final QName SERVICE = new QName(SCA_NS, "service");
    private static final QName REFERENCE = new QName(SCA_NS, "reference");

    private final LoaderRegistry registry;
    private final StAXElementLoader<Property<?>> propertyLoader;
    private final StAXElementLoader<ServiceDefinition> serviceLoader;
    private final StAXElementLoader<ReferenceDefinition> referenceLoader;

    public ComponentTypeLoader(@Reference LoaderRegistry registry,
                               @Reference(name = "property")StAXElementLoader<Property<?>> propertyLoader,
                               @Reference(name = "service")StAXElementLoader<ServiceDefinition> serviceLoader,
                               @Reference(name = "reference")StAXElementLoader<ReferenceDefinition> referenceLoader
    ) {
        this.registry = registry;
        this.propertyLoader = propertyLoader;
        this.serviceLoader = serviceLoader;
        this.referenceLoader = referenceLoader;
    }

    @Init
    public void init() {
        registry.registerLoader(COMPONENT_TYPE, this);
    }

    @Destroy
    public void destroy() {
        registry.unregisterLoader(COMPONENT_TYPE);
    }

    public QName getXMLType() {
        return COMPONENT_TYPE;
    }

    public ComponentType<ServiceDefinition, ReferenceDefinition, Property<?>> load(XMLStreamReader reader,
                                                                                   LoaderContext loaderContext)
            throws XMLStreamException, LoaderException {
        String constrainingAttr = reader.getAttributeValue(null, "constrainingType");
        QName constrainingType = LoaderUtil.getQName(constrainingAttr,
                                                     loaderContext.getTargetNamespace(),
                                                     reader.getNamespaceContext());

        ComponentType<ServiceDefinition, ReferenceDefinition, Property<?>> type =
                new ComponentType<ServiceDefinition, ReferenceDefinition, Property<?>>();
        type.setConstrainingType(constrainingType);
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                QName qname = reader.getName();
                if (PROPERTY.equals(qname)) {
                    Property<?> property = propertyLoader.load(reader, loaderContext);
                    type.add(property);
                } else if (SERVICE.equals(qname)) {
                    ServiceDefinition service = serviceLoader.load(reader, loaderContext);
                    type.add(service);
                } else if (REFERENCE.equals(qname)) {
                    ReferenceDefinition reference = referenceLoader.load(reader, loaderContext);
                    type.add(reference);
                } else {
                    // Extension element - for now try to load and see if we can handle it
                    ModelObject modelObject = registry.load(reader, ModelObject.class, loaderContext);
                    if (modelObject instanceof Property) {
                        type.add((Property<?>) modelObject);
                    } else if (modelObject instanceof ServiceDefinition) {
                        type.add((ServiceDefinition) modelObject);
                    } else if (modelObject instanceof ReferenceDefinition) {
                        type.add((ReferenceDefinition) modelObject);
                    } else {
                        // Unknown extension element, ignore
                    }
                }
                break;
            case END_ELEMENT:
                return type;
            }
        }
    }
}
