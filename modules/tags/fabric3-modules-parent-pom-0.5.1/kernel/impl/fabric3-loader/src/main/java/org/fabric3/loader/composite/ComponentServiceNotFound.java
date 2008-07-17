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

import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.XmlValidationFailure;
import org.fabric3.scdl.ComponentDefinition;

/**
 * A validation failure indicating an attempt to configure a non-existent component service.
 *
 * @version $Rev$ $Date$
 */
public class ComponentServiceNotFound extends XmlValidationFailure<ComponentDefinition> {
    private String serviceName;
    private ComponentDefinition definition;

    public ComponentServiceNotFound(String serviceName, ComponentDefinition definition, XMLStreamReader reader) {
        super("The component " + definition.getName() + " does not have a service " + serviceName, definition, reader);
        this.serviceName = serviceName;
        this.definition = definition;
    }

    public String getServiceName() {
        return serviceName;
    }

    public ComponentDefinition getComponentDefinition() {
        return definition;
    }
}