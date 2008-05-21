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
package org.fabric3.loader.composite;

import javax.xml.stream.XMLStreamReader;

import org.fabric3.introspection.xml.XmlValidationFailure;
import org.fabric3.scdl.ComponentDefinition;

/**
 * A validation failure indicating an attempt to configure a non-existent component property.
 *
 * @version $Rev$ $Date$
 */
public class ComponentPropertyNotFound extends XmlValidationFailure<ComponentDefinition> {
    private String propertyName;
    private ComponentDefinition definition;

    public ComponentPropertyNotFound(String propertyName, ComponentDefinition definition, XMLStreamReader reader) {
        super("The component " + definition.getName() + " does not have a property " + propertyName, definition, reader);
        this.propertyName = propertyName;
        this.definition = definition;
    }

    public ComponentDefinition getComponentDefinition() {
        return definition;
    }

    public String getPropertyName() {
        return propertyName;
    }
}