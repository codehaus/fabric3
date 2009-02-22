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
package org.fabric3.fabric.collector;

import javax.xml.namespace.QName;

import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Marks and collects components, wires, and bindings during deployment and undeployment.
 *
 * @version $Revision$ $Date$
 */
public interface Collector {

    /**
     * Marks all components, wires, and bindings that are part of a context as provisioned.
     *
     * @param composite the root composite to traverse
     */
    void markAsProvisioned(LogicalCompositeComponent composite);

    /**
     * Mark components, bindings and wires belonging to the given deployable for collection.
     *
     * @param deployable the deployable being undeployed
     * @param composite  the composite containing components to be undeployed
     */
    void markForCollection(QName deployable, LogicalCompositeComponent composite);

    /**
     * Recursively collects marked components by removing from the given composite.
     *
     * @param composite the composite to collect marked components from
     */
    void collect(LogicalCompositeComponent composite);

}
