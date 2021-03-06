/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.spi.model.instance;

import java.net.URI;
import javax.xml.namespace.QName;

import org.fabric3.host.Namespaces;
import org.fabric3.model.type.component.ResourceDefinition;

/**
 * Represents a resource on an instantiated component in the domain.
 *
 * @version $Revision$ $Date$
 */
public class LogicalResource<RD extends ResourceDefinition> extends LogicalAttachPoint {
    private static final long serialVersionUID = -6298167441706672513L;

    private static final QName TYPE = new QName(Namespaces.BINDING, "resource");

    private RD resourceDefinition;
    private URI target;

    /**
     * Initializes the URI and the resource definition.
     *
     * @param uri                URI of the resource.
     * @param resourceDefinition Definition of the resource.
     * @param parent             the parent component
     */
    public LogicalResource(URI uri, RD resourceDefinition, LogicalComponent<?> parent) {
        super(uri, resourceDefinition != null ? resourceDefinition.getServiceContract() : null, parent, TYPE);
        this.resourceDefinition = resourceDefinition;
    }

    /**
     * Gets the definition for this resource.
     *
     * @return Definition for this resource.
     */
    public final RD getResourceDefinition() {
        return resourceDefinition;
    }

    /**
     * Gets the target for the resource.
     *
     * @return Resource target.
     */
    public URI getTarget() {
        return target;
    }

    /**
     * Sets the target for the resource.
     *
     * @param target Resource target.
     */
    public void setTarget(URI target) {
        this.target = target;
    }

}
