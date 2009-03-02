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
 * A validation failure indicating an attempt to configure a non-existent component service.
 *
 * @version $Rev$ $Date$
 */
public class ComponentServiceNotFound extends XmlValidationFailure {
    private String serviceName;
    private ComponentDefinition definition;

    public ComponentServiceNotFound(String serviceName, ComponentDefinition definition, XMLStreamReader reader) {
        super("The component " + definition.getName() + " does not have a service " + serviceName, reader);
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