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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.fabric3.fabric.policy.infoset.PolicySetEvaluator;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.definitions.BindingType;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.PolicyResolutionException;
import org.fabric3.spi.services.definitions.DefinitionsRegistry;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
public class InteractionPolicyHelperImpl extends AbstractPolicyHelper implements InteractionPolicyHelper {

    public InteractionPolicyHelperImpl(@Reference DefinitionsRegistry definitionsRegistry,
                                       @Reference PolicySetEvaluator policySetEvaluator) {
        super(definitionsRegistry, policySetEvaluator);
    }

    public Set<Intent> getProvidedIntents(LogicalBinding<?> logicalBinding, Operation<?> operation) throws PolicyResolutionException {
        
        QName type = logicalBinding.getBinding().getType();
        BindingType bindingType = definitionsRegistry.getDefinition(type, BindingType.class);
        
        // FIXME This should not happen, all binding types should be registsred
        if (bindingType == null) {
            return Collections.emptySet();
        }

        Set<QName> mayProvidedIntents = bindingType.getMayProvide();
        
        Set<Intent> requiredIntents = getRequestedIntents(logicalBinding, operation);
        
        Set<Intent> intentsToBeProvided = new HashSet<Intent>();
        for(Intent intent : requiredIntents) {
            if(mayProvidedIntents.contains(intent.getName())) {
                intentsToBeProvided.add(intent);
            }
        }
        
        return intentsToBeProvided;
        
    }
    
    public Set<PolicySet> resolveIntents(LogicalBinding<?> logicalBinding, Operation<?> operation, Element target) throws PolicyResolutionException {

        QName type = logicalBinding.getBinding().getType();
        BindingType bindingType = definitionsRegistry.getDefinition(type, BindingType.class);
        
        Set<QName> alwaysProvidedIntents = new HashSet<QName>();
        Set<QName> mayProvidedIntents = new HashSet<QName>();

        // FIXME This should not happen, all binding types should be registsred
        if (bindingType != null) {
            alwaysProvidedIntents = bindingType.getAlwaysProvide();
            mayProvidedIntents = bindingType.getMayProvide();
        }

        Set<Intent> requiredIntents = getRequestedIntents(logicalBinding, operation);
        
        // Remove intents that are provided
        for(Intent intent : requiredIntents) {
            QName intentName = intent.getName();
            if(alwaysProvidedIntents.contains(intentName) || mayProvidedIntents.contains(intentName)) {
                requiredIntents.remove(intent);
            }
        }
        
        Set<PolicySet> policies = resolvePolicies(requiredIntents, target, operation.getName());        
        if(requiredIntents.size() > 0) {
            throw new PolicyResolutionException("Unable to resolve all intents", requiredIntents);
        }
        
        return policies;
        
    }

    private Set<Intent> getRequestedIntents(LogicalBinding<?> logicalBinding, 
                                            Operation<?> operation) throws PolicyResolutionException {
        
        // Aggregate all the intents from the ancestors
        Set<QName> intentNames = new HashSet<QName>();
        intentNames.addAll(operation.getIntents());
        intentNames.addAll(logicalBinding.getBinding().getIntents());
        intentNames.addAll(aggregateIntents(logicalBinding));
        
        // Expand all the profile intents
        Set<Intent> requiredIntents = resolveProfileIntents(intentNames);
        
        // Remove intents not applicable to the artifact
        filterInvalidIntents(Intent.BINDING, requiredIntents);
        
        return requiredIntents;
        
    }

}
