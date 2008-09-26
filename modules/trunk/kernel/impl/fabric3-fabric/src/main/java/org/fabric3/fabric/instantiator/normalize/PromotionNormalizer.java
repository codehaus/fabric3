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
package org.fabric3.fabric.instantiator.normalize;

import org.fabric3.fabric.instantiator.LogicalChange;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Merges binding and other metadata on promoted services and references down the to the leaf component they are initially defined on.
 *
 * @version $Rev$ $Date$
 */
public interface PromotionNormalizer {

    /**
     * Performs the normalization operation on services and references defined by the given leaf component. The hierarchy of containing components
     * will be walked to determine the set of promoted services and references.
     *
     * @param component the leaf component
     * @param change    the logical change to update
     */
    void normalize(LogicalComponent<?> component, LogicalChange change);

}
