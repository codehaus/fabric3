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

import java.util.Set;

import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.definitions.PolicyPhase;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
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
    private static final Closure<PolicySet, Boolean> INTERCEPTION = new Closure<PolicySet, Boolean>() {
        public Boolean execute(PolicySet policySet) {
            return policySet.getPhase() == PolicyPhase.INTERCEPTION;
        }
    };
    
    /** Closure for filtering provided policies by bindings or implementations. */
    private static final Closure<PolicySet, Boolean> PROVIDED = new Closure<PolicySet, Boolean>() {
        public Boolean execute(PolicySet policySet) {
            return policySet.getPhase() == PolicyPhase.PROVIDED;
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
    public PolicyResult resolvePolicies(ServiceContract<?> serviceContract,
                                            LogicalBinding<?> sourceBinding, 
                                            LogicalBinding<?> targetBinding, 
                                            LogicalComponent<?> source, 
                                            LogicalComponent<?> target) throws PolicyResolutionException {
        
        PolicyResultImpl policyResult = new PolicyResultImpl();
            
        for (Operation<?> operation : serviceContract.getOperations()) {
            
            policyResult.addSourceIntent(operation, interactionPolicyHelper.getProvidedIntents(sourceBinding, operation));
            if (source != null) {
                policyResult.addSourceIntent(operation, implementationPolicyHelper.getProvidedIntents(source, operation));
            }
            
            policyResult.addTargetIntent(operation, interactionPolicyHelper.getProvidedIntents(targetBinding, operation));
            if (target != null) {
                policyResult.addSourceIntent(operation, implementationPolicyHelper.getProvidedIntents(target, operation));
            }
                
            Set<PolicySet> policies = null;
                
            if (source != null) {
                policies = implementationPolicyHelper.resolveIntents(source, operation);
                policyResult.addSourcePolicySet(operation, CollectionUtils.filter(policies, PROVIDED));
                policyResult.addInterceptedPolicySet(operation, CollectionUtils.filter(policies, INTERCEPTION));
            }
                
            if (target != null) {
                policies = implementationPolicyHelper.resolveIntents(target, operation);
                policyResult.addTargetPolicySet(operation, CollectionUtils.filter(policies, PROVIDED));
                policyResult.addInterceptedPolicySet(operation, CollectionUtils.filter(policies, INTERCEPTION));
            }
                
            policies = interactionPolicyHelper.resolveIntents(sourceBinding, operation);
            policyResult.addSourcePolicySet(operation, CollectionUtils.filter(policies, PROVIDED));
            policyResult.addInterceptedPolicySet(operation, CollectionUtils.filter(policies, INTERCEPTION));
                
            policies = interactionPolicyHelper.resolveIntents(targetBinding, operation);
            policyResult.addTargetPolicySet(operation, CollectionUtils.filter(policies, PROVIDED));
            policyResult.addInterceptedPolicySet(operation, CollectionUtils.filter(policies, INTERCEPTION));
                
        }
        
        return policyResult;
        
    }

}
