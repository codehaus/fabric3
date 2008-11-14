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
import org.fabric3.scdl.ComponentDefinition;

/**
 * A validation failure indicating an attempt to configure a non-existent component reference.
 *
 * @version $Rev$ $Date$
 */
public class ComponentReferenceNotFound extends XmlValidationFailure<ComponentDefinition> {
    private String referenceName;
    private ComponentDefinition definition;

    public ComponentReferenceNotFound(String referenceName, ComponentDefinition definition, XMLStreamReader reader) {
        super("The component " + definition.getName() + " does not have a reference " + referenceName, definition, reader);
        this.referenceName = referenceName;
        this.definition = definition;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public ComponentDefinition getComponentDefinition() {
        return definition;
    }
}