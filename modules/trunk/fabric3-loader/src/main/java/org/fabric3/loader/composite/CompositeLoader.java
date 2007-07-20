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

import org.fabric3.spi.loader.InvalidServiceException;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.model.type.Autowire;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.CompositeComponentType;
import org.fabric3.spi.model.type.Include;
import org.fabric3.spi.model.type.Property;
import org.fabric3.spi.model.type.ReferenceDefinition;
import org.fabric3.spi.model.type.ServiceDefinition;
import org.fabric3.spi.model.type.ModelObject;
import org.fabric3.spi.util.stax.StaxUtil;

/**
 * Loads a composite component definition from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class CompositeLoader implements StAXElementLoader<CompositeComponentType> {
    private static final QName COMPOSITE = new QName(SCA_NS, "composite");
    private static final QName INCLUDE = new QName(SCA_NS, "include");
    private static final QName PROPERTY = new QName(SCA_NS, "property");
    private static final QName SERVICE = new QName(SCA_NS, "service");
    private static final QName REFERENCE = new QName(SCA_NS, "reference");
    private static final QName COMPONENT = new QName(SCA_NS, "component");

    private final LoaderRegistry registry;
    private final StAXElementLoader<Include> includeLoader;
    private final StAXElementLoader<Property<?>> propertyLoader;
    private final StAXElementLoader<ServiceDefinition> serviceLoader;
    private final StAXElementLoader<ReferenceDefinition> referenceLoader;
    private final StAXElementLoader<ComponentDefinition<?>> componentLoader;

    public CompositeLoader(@Reference LoaderRegistry registry,
                           @Reference(name="include") StAXElementLoader<Include> includeLoader,
                           @Reference(name="property") StAXElementLoader<Property<?>> propertyLoader,
                           @Reference(name="service") StAXElementLoader<ServiceDefinition> serviceLoader,
                           @Reference(name="reference") StAXElementLoader<ReferenceDefinition> referenceLoader,
                           @Reference(name="component") StAXElementLoader<ComponentDefinition<?>> componentLoader
                           ) {
        this.registry = registry;
        this.includeLoader = includeLoader;
        this.propertyLoader = propertyLoader;
        this.serviceLoader = serviceLoader;
        this.referenceLoader = referenceLoader;
        this.componentLoader = componentLoader;
    }

    public QName getXMLType() {
        return COMPOSITE;
    }

    @Init
    public void init() {
        registry.registerLoader(COMPOSITE, this);
    }

    @Destroy
    public void destroy() {
        registry.unregisterLoader(COMPOSITE);
    }

    public CompositeComponentType load(XMLStreamReader reader, LoaderContext loaderContext)
            throws XMLStreamException, LoaderException {
        String name = reader.getAttributeValue(null, "name");
        String autowire = reader.getAttributeValue(null, "autowire");
        QName compositeName = StaxUtil.createQName(name, reader);
        CompositeComponentType type = new CompositeComponentType(compositeName);

        if ("true".equalsIgnoreCase(autowire)) {
            type.setAutowire(Autowire.ON);
        } else if (autowire == null) {
            type.setAutowire(Autowire.INHERITED);
        } else {
            type.setAutowire(Autowire.OFF);
        }
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                QName qname = reader.getName();
                if (INCLUDE.equals(qname)) {
                    Include include = includeLoader.load(reader, loaderContext);
                    QName includeName = include.getName();
                    if (type.getIncludes().containsKey(includeName)) {
                        throw new DuplicateIncludeException("Include already defined with name", includeName.toString());
                    }
                    type.add(include);
                } else if (PROPERTY.equals(qname)) {
                    Property<?> property = propertyLoader.load(reader, loaderContext);
                    type.add(property);
                } else if (SERVICE.equals(qname)) {
                    ServiceDefinition service = serviceLoader.load(reader, loaderContext);
                    type.add(service);
                } else if (REFERENCE.equals(qname)) {
                    ReferenceDefinition reference = referenceLoader.load(reader, loaderContext);
                    type.add(reference);
                } else if (COMPONENT.equals(qname)) {
                    ComponentDefinition<?> componentDefinition = componentLoader.load(reader, loaderContext);
                    type.add(componentDefinition);
                } else {
                    // Extension element - for now try to load and see if we can handle it
                    ModelObject modelObject = registry.load(reader, ModelObject.class, loaderContext);
                    if (modelObject instanceof Property) {
                        type.add((Property<?>) modelObject);
                    } else if (modelObject instanceof ServiceDefinition) {
                        type.add((ServiceDefinition) modelObject);
                    } else if (modelObject instanceof ReferenceDefinition) {
                        type.add((ReferenceDefinition) modelObject);
                    } else if (modelObject instanceof ComponentDefinition) {
                        type.add((ComponentDefinition<?>) modelObject);
                    } else {
                        // Unknown extension element, ignore
                    }
                }
                break;
            case END_ELEMENT:
                assert COMPOSITE.equals(reader.getName());
                verifyCompositeCompleteness(type);
                return type;
            }
        }
    }

    protected void verifyCompositeCompleteness(CompositeComponentType composite) throws InvalidServiceException {
        // check if all of the composite services have been wired
        for (ServiceDefinition svcDefn : composite.getDeclaredServices().values()) {
            if (svcDefn.getTarget() == null) {
                String identifier = svcDefn.getUri().toString();
                throw new InvalidServiceException("Composite service not wired to a target", identifier);
            }
        }
    }

}
