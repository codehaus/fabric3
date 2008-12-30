/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.loader.composite;

import javax.xml.stream.XMLStreamReader;

import org.fabric3.spi.introspection.xml.XmlValidationFailure;
import org.fabric3.model.type.component.ComponentDefinition;

/**
 * @version $Rev$ $Date$
 */
public class DuplicateConfiguredProperty extends XmlValidationFailure {
    private String propertyName;
    private ComponentDefinition definition;

    public DuplicateConfiguredProperty(String propertyName, ComponentDefinition definition, XMLStreamReader reader) {
        super("The property " + propertyName + " on component " + definition.getName() + "is configured more than once", reader);
        this.propertyName = propertyName;
        this.definition = definition;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public ComponentDefinition getComponentDefinition() {
        return definition;
    }
}
