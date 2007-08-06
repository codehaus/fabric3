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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.fabric3.spi.model.instance.LogicalScaArtifact;
import org.fabric3.scdl.ModelObject;
import org.fabric3.scdl.definitions.BindingType;
import org.fabric3.scdl.definitions.ImplementationType;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.scdl.definitions.PolicySetExtension;
import org.fabric3.spi.policy.registry.PolicyRegistry;
import org.fabric3.spi.policy.registry.PolicyResolutionException;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.osoa.sca.annotations.Reference;

/**
 *
 * @version $Revision$ $Date$
 */
public class DefaultPolicyRegistry implements PolicyRegistry {

    private Set<PolicySet> policySets = new HashSet<PolicySet>();
    private Map<QName, Intent> intents = new HashMap<QName, Intent>();
    private Map<QName, BindingType> bindingTypes = new HashMap<QName, BindingType>();
    private Map<QName, ImplementationType> implementationTypes = new HashMap<QName, ImplementationType>();
    
    private MetaDataStore metaDataStore;
    
    /**
     * Injects the metadata store.
     * 
     * @param metaDataStore Metadata strore.
     */
    @Reference
    public void setMetaDataStore(MetaDataStore metaDataStore) {
        this.metaDataStore = metaDataStore;
    }
    
    /**
     * @see org.fabric3.spi.policy.registry.PolicyRegistry#getInterceptors(org.fabric3.spi.model.instance.LogicalScaArtifact)
     */
    public Set<PolicySetExtension> getPolicy(final LogicalScaArtifact<?> scaArtifact) throws PolicyResolutionException {
        
        // Aggregate all the intents from the ancestors
        Set<QName> intentNames = aggregateIntents(scaArtifact);
        
        // Expand all the profile intents
        Set<Intent> requiredIntents = resolveProfileIntents(intentNames);
        
        // Remove intents not applicable to the artifact
        // TODO for default bindings these may need to change
        filterInvalidIntents(scaArtifact.getType(), requiredIntents);

        // Resolve the policies
        return resolvePolicies(intentNames);
        
    }

    /*
     * Resolve the policies.
     */
    private Set<PolicySetExtension> resolvePolicies(Set<QName> intentNames) {
        
        Set<PolicySetExtension> policies = new HashSet<PolicySetExtension>();
        for(PolicySet policySet : policySets) {
            if(policySet.doesProvide(intentNames)) {
                policies.add(policySet.getExtension());
            }
        }
        return policies;
        
    }

    /**
     * @see org.fabric3.spi.policy.registry.PolicyRegistry#registerIntent(org.fabric3.spi.policy.model.Intent)
     */
    public void registerIntent(Intent intent) {
        intents.put(intent.getName(), intent);
    }

    /**
     * @see org.fabric3.spi.policy.registry.PolicyRegistry#registerPolicySet(org.fabric3.spi.policy.model.PolicySet)
     */
    public void registerPolicySet(PolicySet policySet) {
        policySets.add(policySet);
    }

    /**
     * @see org.fabric3.spi.policy.registry.PolicyRegistry#registerBindingType(org.fabric3.scdl.definitions.BindingType)
     */
    public void registerBindingType(BindingType bindingType) {
        bindingTypes.put(bindingType.getName(), bindingType);
    }

    /**
     * @see org.fabric3.spi.policy.registry.PolicyRegistry#registerImplementationType(org.fabric3.scdl.definitions.ImplementationType)
     */
    public void registerImplementationType(ImplementationType implementationType) {
        implementationTypes.put(implementationType.getName(), implementationType);
    }

    /**
     * @see org.fabric3.spi.policy.registry.PolicyRegistry#deploy(javax.xml.namespace.QName)
     */
    public void deploy(QName definitionArtifact) {
        
        ModelObject modelObject = metaDataStore.resolve(definitionArtifact);
        if(modelObject instanceof Intent) {
            registerIntent((Intent) modelObject);
        } else if(modelObject instanceof PolicySet) {
            registerPolicySet((PolicySet) modelObject);
        } else if(modelObject instanceof BindingType) {
            registerBindingType((BindingType) modelObject);
        } else if(modelObject instanceof ImplementationType) {
            registerImplementationType((ImplementationType) modelObject);
        }
        
    }

    /*
     * Filter invalid intents.
     */
    private void filterInvalidIntents(QName type, Set<Intent> requiredIntents) throws PolicyResolutionException {
        
        for(Intent intent : requiredIntents) {
            
            QName intentName = intent.getName();
            
            if(intent.getIntentType() != null) {
                if(!intent.doesConstrain(type)) {
                    requiredIntents.remove(intent);
                }
            } else {
                if(!intent.isQualified()) {
                    throw new PolicyResolutionException("Unqualified intent without constrained artifact", intentName);
                }
                Intent qualifiableIntent = intents.get(intent.getQualifiable());
                if(qualifiableIntent == null) {
                    throw new PolicyResolutionException("Unknown intent", intent.getQualifiable());
                }
                if(!qualifiableIntent.doesConstrain(type)) {
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
        while(temp != null) {
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
        
        for(QName intentName : intentNames) {
            
            Intent intent = intents.get(intentName);
            if(intent == null) {
                throw new PolicyResolutionException("Unknown intent", intentName);
            }
            
            if(intent.isProfile()) {
                for(QName requiredInentName : intent.getRequires()) {
                    Intent requiredIntent = intents.get(requiredInentName);
                    if(requiredIntent == null) {
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
