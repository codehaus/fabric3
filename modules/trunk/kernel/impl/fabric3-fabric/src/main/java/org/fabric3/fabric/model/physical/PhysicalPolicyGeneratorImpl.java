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
package org.fabric3.fabric.model.physical;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fabric3.fabric.generator.PolicyException;
import org.fabric3.fabric.util.closure.Closure;
import org.fabric3.fabric.util.closure.CollectionUtils;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicyPhase;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.policy.registry.PolicyResolutionException;
import org.fabric3.spi.policy.registry.PolicyResolver;
import org.fabric3.spi.policy.registry.PolicyResult;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class PhysicalPolicyGeneratorImpl implements PhysicalPolicyGenerator {
    
    /** Closure for filtering intercepted policies. */
    private static final Closure<PolicyResult, Boolean> INTERCEPTION = new Closure<PolicyResult, Boolean>() {
        public Boolean execute(PolicyResult policyResult) {
            return policyResult.getPolicyPhase() == PolicyPhase.INTERCEPTION;
        }
    };
    
    /** Closure for filtering provided policies by bindings or implementations. */
    private static final Closure<PolicyResult, Boolean> WIRE_GENERATION = new Closure<PolicyResult, Boolean>() {
        public Boolean execute(PolicyResult policyResult) {
            return policyResult.getPolicyPhase() == PolicyPhase.WIRE_GENERATION;
        }
    };
    
    private final PolicyResolver policyResolver;
    private final PhysicalOperationHelper physicalOperationHelper;
    private final GeneratorRegistry generatorRegistry;
    
    public PhysicalPolicyGeneratorImpl(@Reference PolicyResolver policyResolver,
                                       @Reference PhysicalOperationHelper physicalOperationHelper,
                                       @Reference GeneratorRegistry generatorRegistry) {
        this.policyResolver = policyResolver;
        this.physicalOperationHelper = physicalOperationHelper;
        this.generatorRegistry = generatorRegistry;
    }
    
    @SuppressWarnings("unchecked")
    public PhysicalPolicyResult generatePhysicalPolicies(ServiceContract serviceContract,
                                                         LogicalBinding<?> sourceBinding,
                                                         LogicalBinding<?> targetBinding,
                                                         LogicalComponent<?> source,
                                                         LogicalComponent<?> target) throws GenerationException {

        // Resolve the policies that map to interaction and implementation intents
        PhysicalWireDefinition wireDefinition = new PhysicalWireDefinition();

        Set<Intent> intentsProvidedBySource = new HashSet<Intent>();
        Set<Intent> intentsProvidedByTarget = new HashSet<Intent>();
        
        Set<PolicyResult> policySetsProvidedBySource = new HashSet<PolicyResult>();
        Set<PolicyResult> policySetsProvidedByTarget = new HashSet<PolicyResult>();
        
        try {
            
            intentsProvidedBySource = policyResolver.getInteractionIntentsToBeProvided(sourceBinding);
            if (source != null) {
                intentsProvidedBySource.addAll(policyResolver.getImplementationIntentsToBeProvided(source));
            }
            
            intentsProvidedByTarget = policyResolver.getInteractionIntentsToBeProvided(targetBinding);
            if (target != null) {
                intentsProvidedByTarget.addAll(policyResolver.getImplementationIntentsToBeProvided(target));
            }
            
            List<Operation<?>> operations = serviceContract.getOperations();
            
            for (Operation operation : operations) {
                
                Set<PolicyResult> interceptedPolicies = new HashSet<PolicyResult>();
                
                Set<PolicyResult> policies = null;
                
                if (source != null) {
                    policies = policyResolver.resolveImplementationIntents(source);
                    policySetsProvidedBySource.addAll(CollectionUtils.filter(policies, WIRE_GENERATION));
                    interceptedPolicies.addAll(CollectionUtils.filter(policies, INTERCEPTION));
                }
                
                if (target != null) {
                    policies = policyResolver.resolveImplementationIntents(target);
                    policySetsProvidedByTarget.addAll(CollectionUtils.filter(policies, WIRE_GENERATION));
                    interceptedPolicies.addAll(CollectionUtils.filter(policies, INTERCEPTION));
                }
                
                policies = policyResolver.resolveInteractionIntents(sourceBinding, operation);
                policySetsProvidedBySource.addAll(CollectionUtils.filter(policies, WIRE_GENERATION));
                interceptedPolicies.addAll(CollectionUtils.filter(policies, INTERCEPTION));
                
                policies = policyResolver.resolveInteractionIntents(targetBinding, operation);
                policySetsProvidedByTarget.addAll(CollectionUtils.filter(policies, WIRE_GENERATION));
                interceptedPolicies.addAll(CollectionUtils.filter(policies, INTERCEPTION));
                
                setOperationDefinition(operation, wireDefinition, interceptedPolicies);
                
            }
            
        } catch (PolicyResolutionException e) {
            throw new PolicyException(e);
        }

        setCallbackOperationDefinitions(serviceContract, wireDefinition);
        
        return new PhysicalPolicyResult(intentsProvidedBySource, 
                                        intentsProvidedByTarget, 
                                        policySetsProvidedBySource, 
                                        policySetsProvidedByTarget, 
                                        wireDefinition);
        
    }

    private void setOperationDefinition(Operation<?> operation, PhysicalWireDefinition wireDefinition, Set<PolicyResult> policies)
            throws GenerationException {

        PhysicalOperationDefinition physicalOperation = physicalOperationHelper.mapOperation(operation);
        wireDefinition.addOperation(physicalOperation);
        for (PhysicalInterceptorDefinition interceptorDefinition : generateInterceptorDefinitions(policies)) {
            physicalOperation.addInterceptor(interceptorDefinition);
        }

    }

    @SuppressWarnings({"unchecked"})
    private void setCallbackOperationDefinitions(ServiceContract<?> contract, PhysicalWireDefinition wireDefinition) {

        for (Operation o : contract.getCallbackOperations()) {
            PhysicalOperationDefinition physicalOperation = physicalOperationHelper.mapOperation(o);
            physicalOperation.setCallback(true);
            wireDefinition.addOperation(physicalOperation);
        }

    }

    @SuppressWarnings("unchecked")
    private Set<PhysicalInterceptorDefinition> generateInterceptorDefinitions(Set<PolicyResult> policies) throws GenerationException {

        if (policies == null) {
            return Collections.EMPTY_SET;
        }

        Set<PhysicalInterceptorDefinition> interceptors = new HashSet<PhysicalInterceptorDefinition>();
        for (PolicyResult policy : policies) {
            InterceptorDefinitionGenerator interceptorDefinitionGenerator = 
                generatorRegistry.getInterceptorDefinitionGenerator(policy.getQualifiedName());
            interceptors.add(interceptorDefinitionGenerator.generate(policy.getPolicyDefinition(), null));
        }
        return interceptors;

    }

}
