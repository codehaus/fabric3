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

import org.fabric3.spi.loader.InvalidConfigurationException;
import org.fabric3.spi.loader.InvalidServiceException;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.model.type.Autowire;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.CompositeComponentType;
import org.fabric3.spi.model.type.Include;
import org.fabric3.spi.model.type.ModelObject;
import org.fabric3.spi.model.type.Property;
import org.fabric3.spi.model.type.ReferenceDefinition;
import org.fabric3.spi.model.type.ServiceDefinition;
import org.fabric3.spi.model.type.WireDefinition;
import org.fabric3.spi.util.stax.StaxUtil;

/**
 * Loads a composite component definition from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class CompositeLoader implements StAXElementLoader<CompositeComponentType> {
    private static final QName COMPOSITE = new QName(SCA_NS, "composite");

    private final LoaderRegistry registry;

    public CompositeLoader(@Reference LoaderRegistry registry) {
        this.registry = registry;
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
                ModelObject loadedType = registry.load(reader, ModelObject.class, loaderContext);
                if (loadedType instanceof ServiceDefinition) {
                    type.add((ServiceDefinition) loadedType);
                } else if (loadedType instanceof ReferenceDefinition) {
                    type.add((ReferenceDefinition) loadedType);
                } else if (loadedType instanceof Property<?>) {
                    type.add((Property<?>) loadedType);
                } else if (loadedType instanceof ComponentDefinition<?>) {
                    type.add((ComponentDefinition<?>) loadedType);
                } else if (loadedType instanceof Include) {
                    Include include = (Include) loadedType;
                    QName includeName = include.getName();
                    if (type.getIncludes().containsKey(includeName)) {
                        throw new DuplicateIncludeException("Include already defined with name", includeName.toString());
                    }
                    type.add(include);
                } else if (loadedType instanceof WireDefinition) {
                    type.add((WireDefinition) loadedType);
                } else if (loadedType != null) {
                    throw new InvalidConfigurationException("Invalid element type", loadedType.getClass().getName());
                }
                reader.next();
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
