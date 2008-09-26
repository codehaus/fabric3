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
package org.fabric3.fabric.instantiator;

import org.fabric3.scdl.Composite;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Implementations instantiate logical components within a domain.
 *
 * @version $Revision$ $Date$
 */
public interface LogicalModelInstantiator {

    /**
     * Creates a LogicalChange for including a composite in another composite.
     *
     * @param targetComposite the target composite in which the composite is to be included.
     * @param composite       the composite to be included.
     * @return the change that would result from this include operation
     */
    LogicalChange include(LogicalCompositeComponent targetComposite, Composite composite);


    /**
     * Creates a LogicalChange for removing the composite from the target composite.
     *
     * @param targetComposite the target composite from which the composite is to be removed.
     * @param composite       Composite to be removed.
     * @return the change that would result from this remove operation
     */
    LogicalChange remove(LogicalCompositeComponent targetComposite, Composite composite);
}
