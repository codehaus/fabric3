/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.fabric.policy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicyPhase;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.policy.PolicyResolution;
import org.fabric3.spi.policy.PolicyResolutionException;
import org.fabric3.spi.policy.PolicyResolver;
import org.fabric3.spi.policy.PolicyResult;
import org.fabric3.util.closure.Closure;
import org.fabric3.util.closure.CollectionUtils;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class DefaultPolicyResolver implements PolicyResolver {
    
    /** Closure for filtering intercepted policies. */
    private static final Closure<PolicyResult, Boolean> INTERCEPTION = new Closure<PolicyResult, Boolean>() {
        public Boolean execute(PolicyResult policyResult) {
            return policyResult.getPolicyPhase() == PolicyPhase.INTERCEPTION;
        }
    };
    
    /** Closure for filtering provided policies by bindings or implementations. */
    private static final Closure<PolicyResult, Boolean> PROVIDED = new Closure<PolicyResult, Boolean>() {
        public Boolean execute(PolicyResult policyResult) {
            return policyResult.getPolicyPhase() == PolicyPhase.PROVIDED;
        }
    };
    
    private InteractionPolicyHelper interactionPolicyHelper;
    private ImplementationPolicyHelper implementationPolicyHelper;
    
    public DefaultPolicyResolver(@Reference InteractionPolicyHelper interactionPolicyHelper,
                                 @Reference ImplementationPolicyHelper implementationPolicyHelper) {
        this.interactionPolicyHelper = interactionPolicyHelper;
        this.implementationPolicyHelper = implementationPolicyHelper;
    }

    /**
     * Resolves all the interaction and implementation intents for the wire.
     * 
     * @param serviceContract Service contract for the wire.
     * @param sourceBinding Source binding.
     * @param targetBinding Target binding.
     * @param source Source component.
     * @param target Target component.
     * @return Policy resolution result.
     * 
     * @throws PolicyResolutionException If unable to resolve any policies.
     */
    public PolicyResolution resolvePolicies(ServiceContract<?> serviceContract,
                                            LogicalBinding<?> sourceBinding, 
                                            LogicalBinding<?> targetBinding, 
                                            LogicalComponent<?> source, 
                                            LogicalComponent<?> target) throws PolicyResolutionException {

        Set<Intent> intentsProvidedBySource = new HashSet<Intent>();
        Set<Intent> intentsProvidedByTarget = new HashSet<Intent>();
        
        Set<PolicyResult> policySetsProvidedBySource = new HashSet<PolicyResult>();
        Set<PolicyResult> policySetsProvidedByTarget = new HashSet<PolicyResult>();
        
        Map<Operation<?>, Set<PolicyResult>> interceptedPolicies = new HashMap<Operation<?>, Set<PolicyResult>>();
        
        intentsProvidedBySource = interactionPolicyHelper.getInteractionIntentsToBeProvided(sourceBinding);
        if (source != null) {
            intentsProvidedBySource.addAll(implementationPolicyHelper.getImplementationIntentsToBeProvided(source));
        }
            
        intentsProvidedByTarget = interactionPolicyHelper.getInteractionIntentsToBeProvided(targetBinding);
        if (target != null) {
            intentsProvidedByTarget.addAll(implementationPolicyHelper.getImplementationIntentsToBeProvided(target));
        }
            
        for (Operation<?> operation : serviceContract.getOperations()) {
                
            interceptedPolicies.put(operation, new HashSet<PolicyResult>());
                
            Set<PolicyResult> policies = null;
                
            if (source != null) {
                policies = implementationPolicyHelper.resolveImplementationIntents(source);
                policySetsProvidedBySource.addAll(CollectionUtils.filter(policies, PROVIDED));
                interceptedPolicies.get(operation).addAll(CollectionUtils.filter(policies, INTERCEPTION));
            }
                
            if (target != null) {
                policies = implementationPolicyHelper.resolveImplementationIntents(target);
                policySetsProvidedByTarget.addAll(CollectionUtils.filter(policies, PROVIDED));
                interceptedPolicies.get(operation).addAll(CollectionUtils.filter(policies, INTERCEPTION));
            }
                
            policies = interactionPolicyHelper.resolveInteractionIntents(sourceBinding, operation);
            policySetsProvidedBySource.addAll(CollectionUtils.filter(policies, PROVIDED));
            interceptedPolicies.get(operation).addAll(CollectionUtils.filter(policies, INTERCEPTION));
                
            policies = interactionPolicyHelper.resolveInteractionIntents(targetBinding, operation);
            policySetsProvidedByTarget.addAll(CollectionUtils.filter(policies, PROVIDED));
            interceptedPolicies.get(operation).addAll(CollectionUtils.filter(policies, INTERCEPTION));
                
        }
        
        return new PolicyResolution(intentsProvidedBySource, 
                                    intentsProvidedByTarget, 
                                    policySetsProvidedBySource, 
                                    policySetsProvidedByTarget,
                                    interceptedPolicies);
        
    }

}
