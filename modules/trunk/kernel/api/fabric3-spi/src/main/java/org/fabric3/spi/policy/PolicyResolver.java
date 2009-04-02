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
package org.fabric3.spi.policy;

import java.util.List;

import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;


/**
 * Interface for the policy resolver.
 *
 * @version $Revision$ $Date$
 */
public interface PolicyResolver {

    /**
     * Resolves all the interaction and implementation intents for the wire.
     *
     * @param operations    the operations to resolve policies for
     * @param sourceBinding Source binding.
     * @param targetBinding Target binding.
     * @param source        Source component.
     * @param target        Target component.
     * @return Policy resolution result.
     * @throws PolicyResolutionException If unable to resolve any policies.
     */
    PolicyResult resolvePolicies(List<LogicalOperation> operations,
                                 LogicalBinding<?> sourceBinding,
                                 LogicalBinding<?> targetBinding,
                                 LogicalComponent<?> source,
                                 LogicalComponent<?> target) throws PolicyResolutionException;

}
