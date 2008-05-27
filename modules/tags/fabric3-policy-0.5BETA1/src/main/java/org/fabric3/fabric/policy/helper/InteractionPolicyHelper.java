/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.fabric.policy.helper;

import java.util.Set;

import org.fabric3.scdl.Operation;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.PolicyResolutionException;
import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
public interface InteractionPolicyHelper {

    /**
     * Returns the set of intents that need to be explictly provided by the binding. These are the 
     * intents requested by the use and available in the <code>mayProvide</code> list of intents 
     * declared by the binding type.
     * 
     * @param logicalBinding Logical binding for which intents are to be resolved.
     * @param operation Operation for which the provided intents are to be computed.
     * @return Set of intents that need to be explictly provided by the binding.
     * @throws PolicyResolutionException If there are any unidentified intents.
     */
    Set<Intent> getProvidedIntents(LogicalBinding<?> logicalBinding, Operation<?> operation) throws PolicyResolutionException;
    
    /**
     * Returns the set of policies that will address the intents that are not provided by the binding type.
     * 
     * @param binding Binding for which policies are to be resolved.
     * @param operation Operation for which the intents are to be resolved.
     * @return Set of resolved policies.
     * @throws PolicyResolutionException If all intents cannot be resolved.
     */
    Set<PolicySet> resolveIntents(LogicalBinding<?> binding, Operation<?> operation, Element target) throws PolicyResolutionException;

}
