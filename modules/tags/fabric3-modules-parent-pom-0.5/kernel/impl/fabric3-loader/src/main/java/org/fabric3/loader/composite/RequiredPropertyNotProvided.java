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
import org.fabric3.scdl.Property;

/**
 * @version $Rev$ $Date$
 */
public class RequiredPropertyNotProvided extends XmlValidationFailure<Property> {
    private String componentName;

    public RequiredPropertyNotProvided(Property property, String componentName, XMLStreamReader reader) {
        super("Component " + componentName + " has a property " + property.getName() + " which requires that a value is supplied", property, reader);
        this.componentName = componentName;
    }

    public String getComponentName() {
        return componentName;
    }
}