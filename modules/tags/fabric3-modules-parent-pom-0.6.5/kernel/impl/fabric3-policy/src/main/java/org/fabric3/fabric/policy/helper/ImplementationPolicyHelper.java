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
package org.fabric3.fabric.policy.helper;

import java.util.Set;

import org.fabric3.scdl.Operation;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.policy.PolicyResolutionException;
import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
public interface ImplementationPolicyHelper {
    
    /**
     * Returns the set of intents that need to be explictly provided by the implementation. These 
     * are the intents requested by the use and available in the <code>mayProvide</code> list of intents 
     * declared by the implementation type.
     * 
     * @param logicalComponent Logical component for which intents are to be resolved.
     * @param operation Operation for which the provided intents are to be computed.
     * @return Set of intents that need to be explictly provided by the implementation.
     * @throws PolicyResolutionException If there are any unidentified intents.
     */
    Set<Intent> getProvidedIntents(LogicalComponent<?> logicalComponent, Operation<?> operation) 
    throws PolicyResolutionException;
    
    /**
     * Returns the set of policies that will address the intents that are not provided by the implementation type.
     * 
     * @param logicalComponent Logical component for which policies are to be resolved.
     * @param operation Operation for which the provided intents are to be computed.
     * @return Set of resolved policies.
     * @throws PolicyResolutionException If all intents cannot be resolved.
     */
    Set<PolicySet> resolveIntents(LogicalComponent<?> logicalComponent, Operation<?> operation, Element target) throws PolicyResolutionException;

}
