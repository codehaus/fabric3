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
package org.fabric3.fabric.policy.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.scdl.definitions.PolicySetExtension;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalScaArtifact;
import org.fabric3.spi.policy.registry.PolicyResolutionException;
import org.fabric3.spi.policy.registry.PolicyResolutionResult;
import org.fabric3.spi.policy.registry.PolicyResolver;
import org.fabric3.spi.services.definitions.DefinitionsRegistry;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class DefaultPolicyResolver implements PolicyResolver {

    // Definitions registry
    private DefinitionsRegistry definitionsRegistry;
    
    /**
     * Injects the definitions registry.
     * 
     * @param definitionsRegistry Definitions registry.
     */
    public DefaultPolicyResolver(@Reference DefinitionsRegistry definitionsRegistry) {
        this.definitionsRegistry = definitionsRegistry;
    }

    /**
     * @see org.fabric3.spi.policy.registry.PolicyResolver#resolveIntents(org.fabric3.spi.model.instance.LogicalBinding)
     */
    public PolicyResolutionResult resolveIntents(LogicalBinding<?> logicalBinding) throws PolicyResolutionException {

        // Aggregate all the intents from the ancestors
        Set<QName> intentNames = aggregateIntents(logicalBinding);

        // Expand all the profile intents
        Set<Intent> requiredIntents = resolveProfileIntents(intentNames);

        // Remove intents not applicable to the artifact
        // TODO for default bindings these may need to change
        filterInvalidIntents(Intent.BINDING, requiredIntents);
        
        // TODO This is not the correct implementatopn based on the chat with Jeremy & Michael
        // We need to go to the binding type to get provided intents
        
        Map<PolicySetExtension, Boolean> resolvedPolicies = new HashMap<PolicySetExtension, Boolean>();
        Map<Intent, Boolean> providedIntents = new HashMap<Intent, Boolean>();
        
        for(PolicySetExtension extension : resolvePolicies(intentNames)) {
            resolvedPolicies.put(extension, Boolean.TRUE);
        }
        
        return new DefaultPolicyResolutionResult(providedIntents, resolvedPolicies);
        
    }

    /**
     * @see org.fabric3.spi.policy.registry.PolicyResolver#resolveIntents(org.fabric3.spi.model.instance.LogicalComponent)
     */
    public PolicyResolutionResult resolveIntents(LogicalComponent<?> logicalComponent) throws PolicyResolutionException {

        // Aggregate all the intents from the ancestors
        Set<QName> intentNames = new HashSet<QName>();
        intentNames.addAll(logicalComponent.getDefinition().getImplementation().getIntents());
        intentNames.addAll(aggregateIntents(logicalComponent));

        // Expand all the profile intents
        Set<Intent> requiredIntents = resolveProfileIntents(intentNames);

        // Remove intents not applicable to the artifact
        filterInvalidIntents(Intent.IMPLEMENTATION, requiredIntents);
        
        // TODO This is not the correct implementatopn based on the chat with Jeremy & Michael
        // We need to go to the implementation type to get provided intents
        
        Map<PolicySetExtension, Boolean> resolvedPolicies = new HashMap<PolicySetExtension, Boolean>();
        Map<Intent, Boolean> providedIntents = new HashMap<Intent, Boolean>();
        
        for(PolicySetExtension extension : resolvePolicies(intentNames)) {
            resolvedPolicies.put(extension, Boolean.TRUE);
        }
        
        return new DefaultPolicyResolutionResult(providedIntents, resolvedPolicies);
        
    }

    /*
     * Resolve the policies.
     */
    private Set<PolicySetExtension> resolvePolicies(Set<QName> intentNames) {

        Set<PolicySetExtension> policies = new HashSet<PolicySetExtension>();
        for (PolicySet policySet : definitionsRegistry.getAllDefinitions(PolicySet.class)) {
            if (policySet.doesProvide(intentNames)) {
                policies.add(policySet.getExtension());
            }
        }
        return policies;

    }

    /*
     * Filter invalid intents.
     */
    private void filterInvalidIntents(QName type, Set<Intent> requiredIntents) throws PolicyResolutionException {

        for (Intent intent : requiredIntents) {

            QName intentName = intent.getName();

            if (intent.getIntentType() != null) {
                if (!intent.doesConstrain(type)) {
                    requiredIntents.remove(intent);
                }
            } else {
                if (!intent.isQualified()) {
                    throw new PolicyResolutionException("Unqualified intent without constrained artifact", intentName);
                }
                Intent qualifiableIntent = definitionsRegistry.getDefinition(intent.getQualifiable(), Intent.class);
                if (qualifiableIntent == null) {
                    throw new PolicyResolutionException("Unknown intent", intent.getQualifiable());
                }
                if (!qualifiableIntent.doesConstrain(type)) {
                    requiredIntents.remove(intent);
                }
            }
        }

    }

    /*
     * Aggregate intents from ancestors.
     */
    private Set<QName> aggregateIntents(final LogicalScaArtifact<?> scaArtifact) {

        LogicalScaArtifact<?> temp = scaArtifact;

        Set<QName> intentNames = new HashSet<QName>();
        while (temp != null) {
            intentNames.addAll(scaArtifact.getIntents());
            temp = temp.getParent();
        }
        return intentNames;

    }

    /*
     * Expand profile intents.
     */
    private Set<Intent> resolveProfileIntents(Set<QName> intentNames) throws PolicyResolutionException {

        Set<Intent> requiredIntents = new HashSet<Intent>();

        for (QName intentName : intentNames) {

            Intent intent = definitionsRegistry.getDefinition(intentName, Intent.class);
            if (intent == null) {
                throw new PolicyResolutionException("Unknown intent", intentName);
            }

            if (intent.isProfile()) {
                for (QName requiredInentName : intent.getRequires()) {
                    Intent requiredIntent = definitionsRegistry.getDefinition(requiredInentName, Intent.class);
                    if (requiredIntent == null) {
                        throw new PolicyResolutionException("Unknown intent", requiredInentName);
                    }
                    requiredIntents.add(requiredIntent);

                }
            } else {
                requiredIntents.add(intent);
            }

        }
        return requiredIntents;

    }
    
    /*
     * Implementation of policy resolution result.
     */
    private class DefaultPolicyResolutionResult implements PolicyResolutionResult {
        
        private final Map<Intent, Boolean> providedIntents;
        private final Map<PolicySetExtension, Boolean> resolvedPolicies;
        
        public DefaultPolicyResolutionResult(Map<Intent, Boolean> providedIntents, Map<PolicySetExtension, Boolean> resolvedPolicies) {
            this.providedIntents = providedIntents;
            this.resolvedPolicies = resolvedPolicies;
        }

        public Map<Intent, Boolean> getProvidedIntents() {
            return Collections.unmodifiableMap(providedIntents);
        }

        public Map<PolicySetExtension, Boolean> getResolvedPolicies() {
            return Collections.unmodifiableMap(resolvedPolicies);
        }
        
    }

}
