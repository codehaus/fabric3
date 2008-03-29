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

import org.fabric3.fabric.policy.helper.ImplementationPolicyHelper;
import org.fabric3.fabric.policy.helper.InteractionPolicyHelper;
import org.fabric3.fabric.policy.infoset.PolicyInfosetBuilder;
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
import org.w3c.dom.Element;

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
    
    private final InteractionPolicyHelper interactionPolicyHelper;
    private final ImplementationPolicyHelper implementationPolicyHelper;
    private final PolicyInfosetBuilder policyInfosetBuilder;
    
    public DefaultPolicyResolver(@Reference InteractionPolicyHelper interactionPolicyHelper,
                                 @Reference ImplementationPolicyHelper implementationPolicyHelper,
                                 @Reference PolicyInfosetBuilder policyInfosetBuilder) {
        this.interactionPolicyHelper = interactionPolicyHelper;
        this.implementationPolicyHelper = implementationPolicyHelper;
        this.policyInfosetBuilder = policyInfosetBuilder;
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
            
            policyResult.addSourceIntents(operation, interactionPolicyHelper.getProvidedIntents(sourceBinding, operation));
            if (source != null) {
                policyResult.addSourceIntents(operation, implementationPolicyHelper.getProvidedIntents(source, operation));
            }
            
            policyResult.addTargetIntents(operation, interactionPolicyHelper.getProvidedIntents(targetBinding, operation));
            if (target != null) {
                policyResult.addSourceIntents(operation, implementationPolicyHelper.getProvidedIntents(target, operation));
            }
                
            Set<PolicySet> policies = null;
            Element policyInfoset = null;
                
            if (source != null) {
                policyInfoset = policyInfosetBuilder.buildInfoSet(source);
                policies = implementationPolicyHelper.resolveIntents(source, operation, policyInfoset);
                policyResult.addSourcePolicySets(operation, CollectionUtils.filter(policies, PROVIDED));
                policyResult.addInterceptedPolicySets(operation, CollectionUtils.filter(policies, INTERCEPTION));
            }

            policyInfoset = policyInfosetBuilder.buildInfoSet(sourceBinding);
            policies = interactionPolicyHelper.resolveIntents(sourceBinding, operation, policyInfoset);
            policyResult.addSourcePolicySets(operation, CollectionUtils.filter(policies, PROVIDED));
            policyResult.addInterceptedPolicySets(operation, CollectionUtils.filter(policies, INTERCEPTION));

            policyInfoset = policyInfosetBuilder.buildInfoSet(targetBinding);
            policies = interactionPolicyHelper.resolveIntents(targetBinding, operation, policyInfoset);
            policyResult.addTargetPolicySets(operation, CollectionUtils.filter(policies, PROVIDED));
            policyResult.addInterceptedPolicySets(operation, CollectionUtils.filter(policies, INTERCEPTION));
                
            if (target != null) {
                policyInfoset = policyInfosetBuilder.buildInfoSet(target);
                policies = implementationPolicyHelper.resolveIntents(target, operation, policyInfoset);
                policyResult.addTargetPolicySets(operation, CollectionUtils.filter(policies, PROVIDED));
                policyResult.addInterceptedPolicySets(operation, CollectionUtils.filter(policies, INTERCEPTION));
            }
                
        }
        
        return policyResult;
        
    }

}
