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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.fabric3.fabric.policy.infoset.PolicySetEvaluator;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.model.instance.LogicalScaArtifact;
import org.fabric3.spi.policy.PolicyResolutionException;
import org.fabric3.spi.services.definitions.DefinitionsRegistry;
import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
public class AbstractPolicyHelper {
    
    private final PolicySetEvaluator policySetEvaluator;
    protected final DefinitionsRegistry definitionsRegistry;
    
    protected AbstractPolicyHelper(DefinitionsRegistry definitionsRegistry, PolicySetEvaluator policySetEvaluator) {
        this.definitionsRegistry = definitionsRegistry;
        this.policySetEvaluator = policySetEvaluator;
    }

    /*
     * Resolve the policies.
     */
    protected Set<PolicySet> resolvePolicies(Set<Intent> requiredIntents, Element target, String operation) throws PolicyResolutionException {

        Set<PolicySet> policies = new LinkedHashSet<PolicySet>();
        
        for (PolicySet policySet : definitionsRegistry.getAllDefinitions(PolicySet.class)) {
            Iterator<Intent> iterator = requiredIntents.iterator();
            while(iterator.hasNext()) {
                Intent intent = iterator.next();
                if(policySet.doesProvide(intent.getName())) {
                    String appliesTo = policySet.getAppliesTo();
                    if (appliesTo == null || policySetEvaluator.doesApply(target, appliesTo, operation)) {
                        policies.add(policySet);
                        iterator.remove();
                    }
                }
            }
        }
        
        return policies;

    }

    /*
     * Filter invalid intents.
     */
    protected void filterInvalidIntents(QName type, Set<Intent> requiredIntents) throws PolicyResolutionException {

        for (Iterator<Intent> it = requiredIntents.iterator();it.hasNext();) {
            
            Intent intent = it.next();

            QName intentName = intent.getName();

            if (intent.getIntentType() != null) {
                if (!intent.doesConstrain(type)) {
                    it.remove();
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
                    it.remove();
                }
            }
        }

    }

    /*
     * Aggregate intents from ancestors.
     */
    protected Set<QName> aggregateIntents(final LogicalScaArtifact<?> scaArtifact) {

        LogicalScaArtifact<?> temp = scaArtifact;

        Set<QName> intentNames = new LinkedHashSet<QName>();
        while (temp != null) {
            intentNames.addAll(temp.getIntents());
            temp = temp.getParent();
        }
        return intentNames;

    }

    /*
     * Expand profile intents.
     */
    protected Set<Intent> resolveProfileIntents(Set<QName> intentNames) throws PolicyResolutionException {

        Set<Intent> requiredIntents = new LinkedHashSet<Intent>();

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
