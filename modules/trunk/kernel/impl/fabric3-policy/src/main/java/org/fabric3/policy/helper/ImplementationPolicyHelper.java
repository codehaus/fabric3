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
package org.fabric3.policy.helper;

import java.util.Set;

import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.policy.PolicyResolutionException;

/**
 * @version $Revision$ $Date$
 */
public interface ImplementationPolicyHelper {

    /**
     * Returns the set of intents that need to be explictly provided by the implementation. These are the intents requested by the user and available
     * in the <code>mayProvide</code> list of intents declared by the implementation type.
     *
     * @param component the logical component for which intents are to be resolved.
     * @param operation the operation for which the provided intents are to be computed.
     * @return Set of intents that need to be explictly provided by the implementation.
     * @throws PolicyResolutionException If there are any unidentified intents.
     */
    Set<Intent> getProvidedIntents(LogicalComponent<?> component, LogicalOperation operation) throws PolicyResolutionException;

    /**
     * Returns the set of policies explicitly declared for the operation and those that satisfy the intents not provided by the implementation type.
     *
     * @param component the logical component for which policies are to be resolved.
     * @param operation the oeration for which the provided intents are to be computed.
     * @return Set of resolved policies.
     * @throws PolicyResolutionException If all intents cannot be resolved.
     */
    Set<PolicySet> resolve(LogicalComponent<?> component, LogicalOperation operation) throws PolicyResolutionException;


}
