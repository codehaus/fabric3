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

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.model.type.ComponentType;
import org.fabric3.spi.model.type.ModelObject;
import org.fabric3.spi.model.type.Property;
import org.fabric3.spi.model.type.ReferenceDefinition;
import org.fabric3.spi.model.type.ServiceDefinition;

/**
 * @version $Rev$ $Date$
 */
public class ComponentTypeElementLoader extends LoaderExtension<ComponentType<?, ?, ?>> {
    public static final QName COMPONENT_TYPE = new QName(SCA_NS, "componentType");

    @Constructor
    public ComponentTypeElementLoader(@Reference LoaderRegistry registry) {
        super(registry);
    }

    public QName getXMLType() {
        return COMPONENT_TYPE;
    }

    @SuppressWarnings("unchecked")
    public ComponentType load(XMLStreamReader reader, LoaderContext context)
            throws XMLStreamException, LoaderException {
        assert COMPONENT_TYPE.equals(reader.getName());
        ComponentType<ServiceDefinition, ReferenceDefinition, Property<?>> componentType;
        componentType = new ComponentType<ServiceDefinition, ReferenceDefinition, Property<?>>();
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                ModelObject o = registry.load(reader, context);
                if (o instanceof ServiceDefinition) {
                    componentType.add((ServiceDefinition) o);
                } else if (o instanceof ReferenceDefinition) {
                    componentType.add((ReferenceDefinition) o);
                } else if (o instanceof Property) {
                    componentType.add((Property<?>) o);
                }
                break;
            case END_ELEMENT:
                return componentType;
            }
        }
    }
}
