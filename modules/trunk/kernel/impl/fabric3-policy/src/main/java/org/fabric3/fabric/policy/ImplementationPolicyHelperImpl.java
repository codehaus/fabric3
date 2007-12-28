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
package org.fabric3.fabric.policy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.definitions.ImplementationType;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicyPhase;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.policy.PolicyResolutionException;
import org.fabric3.spi.policy.PolicyResult;
import org.fabric3.spi.services.definitions.DefinitionsRegistry;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class ImplementationPolicyHelperImpl extends AbstractPolicyHelper implements ImplementationPolicyHelper {

    public ImplementationPolicyHelperImpl(@Reference DefinitionsRegistry definitionsRegistry) {
        super(definitionsRegistry);
    }
    
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
    
    public Set<PolicyResult> resolveImplementationIntents(LogicalComponent<?> logicalComponent) throws PolicyResolutionException {
        
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
        
        Set<PolicySet> policies = resolvePolicies(requiredIntents, logicalComponent);        
        if(requiredIntents.size() > 0) {
            throw new PolicyResolutionException("Unable to resolve all intents", requiredIntents);
        }
        
        return createResults(policies, PolicyPhase.PROVIDED);
        
    }

}
