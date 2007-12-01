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
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.definitions.BindingType;
import org.fabric3.scdl.definitions.ImplementationType;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalScaArtifact;
import org.fabric3.spi.policy.registry.PolicyResolutionException;
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
    public DefaultPolicyResolver(@Reference(required = true) DefinitionsRegistry definitionsRegistry) {
        this.definitionsRegistry = definitionsRegistry;
    }

    /**
     * @see org.fabric3.spi.policy.registry.PolicyResolver#getInteractionIntentsToBeProvided(org.fabric3.spi.model.instance.LogicalBinding)
     */
    public Set<Intent> getInteractionIntentsToBeProvided(LogicalBinding<?> logicalBinding) throws PolicyResolutionException {

        BindingDefinition bindingDefinition = logicalBinding.getBinding();
        QName type = logicalBinding.getBinding().getType();
        BindingType bindingType = definitionsRegistry.getDefinition(type, BindingType.class);
        
        // FIXME This should not happen, all binding types should be registsred
        if(bindingType == null) {
            return Collections.emptySet();
        }

        Set<QName> mayProvidedIntents = bindingType.getMayProvide();
        
        // Aggregate all the intents from the ancestors
        Set<QName> intentNames = new HashSet<QName>();
        intentNames.addAll(bindingDefinition.getIntents());
        intentNames.addAll(aggregateIntents(logicalBinding));

        // Expand all the profile intents
        Set<Intent> requiredIntents = resolveProfileIntents(intentNames);

        // Remove intents not applicable to the artifact
        filterInvalidIntents(Intent.BINDING, requiredIntents);
        
        Set<Intent> intentsToBeProvided = new HashSet<Intent>();
        for(Intent intent : requiredIntents) {
            if(mayProvidedIntents.contains(intent.getName())) {
                intentsToBeProvided.add(intent);
            }
        }
        return intentsToBeProvided;
        
    }
    
    /**
     * @see org.fabric3.spi.policy.registry.PolicyResolver#getImplementationIntentsToBeProvided(org.fabric3.spi.model.instance.LogicalComponent)
     */
    public Set<Intent> getImplementationIntentsToBeProvided(LogicalComponent<?> logicalComponent) throws PolicyResolutionException {
        
        Implementation<?> implementation = logicalComponent.getDefinition().getImplementation();
        QName type = implementation.getType();
        ImplementationType implementationType = definitionsRegistry.getDefinition(type, ImplementationType.class);
        
        // FIXME This should not happen, all implementation types should be registsred
        if(implementationType == null) {
            return Collections.emptySet();
        }
        
        Set<QName> mayProvidedIntents = implementationType.getMayProvide();

        // Aggregate all the intents from the ancestors
        Set<QName> intentNames = new HashSet<QName>();
        intentNames.addAll(implementation.getIntents());
        intentNames.addAll(aggregateIntents(logicalComponent));
        intentNames.removeAll(mayProvidedIntents);

        // Expand all the profile intents
        Set<Intent> requiredIntents = resolveProfileIntents(intentNames);

        // Remove intents not applicable to the artifact
        filterInvalidIntents(Intent.IMPLEMENTATION, requiredIntents);
        
        Set<Intent> intentsToBeProvided = new HashSet<Intent>();
        for(Intent intent : requiredIntents) {
            if(mayProvidedIntents.contains(intent.getName())) {
                intentsToBeProvided.add(intent);
            }
        }
        return intentsToBeProvided;
        
    }
    
    /**
     * @see org.fabric3.spi.policy.registry.PolicyResolver#resolveInteractionIntents(org.fabric3.spi.model.instance.LogicalBinding)
     */
    public Set<PolicySet> resolveInteractionIntents(LogicalBinding<?> logicalBinding, Operation<?> operation) throws PolicyResolutionException {
        
        QName type = logicalBinding.getType();
        BindingType bindingType = definitionsRegistry.getDefinition(type, BindingType.class);
        
        Set<QName> alwaysProvidedIntents = new HashSet<QName>();
        Set<QName> mayProvidedIntents = new HashSet<QName>();

        // FIXME This should not happen, all binding types should be registsred
        if(bindingType != null) {
            alwaysProvidedIntents = bindingType.getAlwaysProvide();
            mayProvidedIntents = bindingType.getMayProvide();
        }

        // Aggregate all the intents from the ancestors
        Set<QName> intentNames = aggregateIntents(logicalBinding);
        intentNames.addAll(operation.getIntents());
        
        // Expand all the profile intents
        Set<Intent> requiredIntents = resolveProfileIntents(intentNames);
        
        // Remove intents not applicable to the artifact
        filterInvalidIntents(Intent.BINDING, requiredIntents);
        
        // Remove intents that are provided
        for(Intent intent : requiredIntents) {
            QName intentName = intent.getName();
            if(alwaysProvidedIntents.contains(intentName) || mayProvidedIntents.contains(intentName)) {
                requiredIntents.remove(intent);
            }
        }
        
        Set<PolicySet> policies = resolvePolicies(requiredIntents);        
        if(requiredIntents.size() > 0) {
            throw new PolicyResolutionException("Unable to resolve all intents", type);
        }
        
        return policies;
        
    }
    
    /**
     * @see org.fabric3.spi.policy.registry.PolicyResolver#resolveImplementationIntents(org.fabric3.spi.model.instance.LogicalComponent)
     */
    public Set<PolicySet> resolveImplementationIntents(LogicalComponent<?> logicalComponent) throws PolicyResolutionException {
        
        Implementation<?> implementation = logicalComponent.getDefinition().getImplementation();
        QName type = implementation.getType();
        ImplementationType implementationType = definitionsRegistry.getDefinition(type, ImplementationType.class);
        
        Set<QName> alwaysProvidedIntents = new HashSet<QName>();
        Set<QName> mayProvidedIntents = new HashSet<QName>();

        // FIXME This should not happen, all implementation types should be registsred
        if(implementationType != null) {
            alwaysProvidedIntents = implementationType.getAlwaysProvide();
            mayProvidedIntents = implementationType.getMayProvide();
        }

        // Aggregate all the intents from the ancestors
        Set<QName> intentNames = new HashSet<QName>();
        intentNames.addAll(logicalComponent.getDefinition().getImplementation().getIntents());
        intentNames.addAll(logicalComponent.getDefinition().getIntents());
        intentNames.addAll(aggregateIntents(logicalComponent));

        // Expand all the profile intents
        Set<Intent> requiredIntents = resolveProfileIntents(intentNames);

        // Remove intents not applicable to the artifact
        filterInvalidIntents(Intent.IMPLEMENTATION, requiredIntents);
        
        // Remove intents that are provided
        for(Intent intent : requiredIntents) {
            QName intentName = intent.getName();
            if(alwaysProvidedIntents.contains(intentName) || mayProvidedIntents.contains(intentName)) {
                requiredIntents.remove(intent);
            }
        }
        
        Set<PolicySet> policies = resolvePolicies(requiredIntents);        
        if(requiredIntents.size() > 0) {
            throw new PolicyResolutionException("Unable to resolve all intents", type);
        }
        
        return policies;
        
    }

    /*
     * Resolve the policies.
     */
    private Set<PolicySet> resolvePolicies(Set<Intent> requiredIntents) throws PolicyResolutionException {

        Set<PolicySet> policies = new HashSet<PolicySet>();
        
        for (PolicySet policySet : definitionsRegistry.getAllDefinitions(PolicySet.class)) {
            for(Intent intent : requiredIntents) {
                if(policySet.doesProvide(intent.getName())) {
                    policies.add(policySet);
                    requiredIntents.remove(intent);
                }
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

}
