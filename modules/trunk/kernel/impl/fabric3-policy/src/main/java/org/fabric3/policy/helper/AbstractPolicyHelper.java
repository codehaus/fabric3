/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.policy.helper;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.policy.infoset.PolicyEvaluator;
import org.fabric3.spi.model.instance.LogicalScaArtifact;
import org.fabric3.spi.policy.PolicyRegistry;
import org.fabric3.spi.policy.PolicyResolutionException;
import org.fabric3.spi.lcm.LogicalComponentManager;

/**
 * Base class for resolving policies.
 *
 * @version $Rev$ $Date$
 */
public class AbstractPolicyHelper {
    protected LogicalComponentManager lcm;
    protected PolicyEvaluator policyEvaluator;
    protected PolicyRegistry policyRegistry;

    protected AbstractPolicyHelper(PolicyRegistry policyRegistry, LogicalComponentManager lcm, PolicyEvaluator policyEvaluator) {
        this.policyRegistry = policyRegistry;
        this.lcm = lcm;
        this.policyEvaluator = policyEvaluator;
    }

    /**
     * Resolves intents to policies.
     *
     * @param requiredIntents the intents to resolve
     * @param target          the logical artifact to resolve against
     * @return the resolved policy sets
     * @throws PolicyResolutionException if there is an error during resolution
     */
    protected Set<PolicySet> resolvePolicies(Set<Intent> requiredIntents, LogicalScaArtifact<?> target) throws PolicyResolutionException {

        Set<PolicySet> policies = new LinkedHashSet<PolicySet>();

        Collection<PolicySet> definitions = policyRegistry.getAllDefinitions(PolicySet.class);
        // Calculate appliesTo by first determining if the policy set provides the intent and then matching its appliesTo expression
        // against the logical artifact given as a target
        for (PolicySet policySet : definitions) {
            Iterator<Intent> iterator = requiredIntents.iterator();
            while (iterator.hasNext()) {
                Intent intent = iterator.next();
                if (policySet.doesProvide(intent.getName())) {
                    String appliesTo = policySet.getAppliesTo();
                    String attachTo = policySet.getAttachTo();
                    if ((appliesTo == null && attachTo == null) || (attachTo == null && policyEvaluator.doesApply(appliesTo, target))) {
                        policies.add(policySet);
                        iterator.remove();
                    }
                }
            }
        }
        return policies;
    }

    /**
     * Filter invalid intents.
     *
     * @param type            the type to filter on
     * @param requiredIntents the intents to filter
     * @throws PolicyResolutionException if an error is encountered filtering
     */
    protected void filterInvalidIntents(QName type, Set<Intent> requiredIntents) throws PolicyResolutionException {

        for (Iterator<Intent> it = requiredIntents.iterator(); it.hasNext();) {
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
                Intent qualifiableIntent = policyRegistry.getDefinition(intent.getQualifiable(), Intent.class);
                if (qualifiableIntent == null) {
                    throw new PolicyResolutionException("Unknown intent", intent.getQualifiable());
                }
                if (!qualifiableIntent.doesConstrain(type)) {
                    it.remove();
                }
            }
        }

    }

    /**
     * Aggregate intents from ancestors.
     *
     * @param scaArtifact the logical artifact to aggregate intents for
     * @return the agreggated intents
     */
    protected Set<QName> aggregateIntents(LogicalScaArtifact<?> scaArtifact) {
        LogicalScaArtifact<?> temp = scaArtifact;
        Set<QName> intentNames = new LinkedHashSet<QName>();
        while (temp != null) {
            intentNames.addAll(temp.getIntents());
            temp = temp.getParent();
        }
        return intentNames;
    }

    /**
     * Resolves profile intents.
     *
     * @param intentNames the intent names to resolve
     * @return the expanded intents
     * @throws PolicyResolutionException if an exception is encountered resolving the intents
     */
    protected Set<Intent> resolveProfileIntents(Set<QName> intentNames) throws PolicyResolutionException {
        Set<Intent> requiredIntents = new LinkedHashSet<Intent>();
        for (QName intentName : intentNames) {
            Intent intent = policyRegistry.getDefinition(intentName, Intent.class);
            if (intent == null) {
                throw new PolicyResolutionException("Unknown intent", intentName);
            }
            if (intent.isProfile()) {
                for (QName requiredInentName : intent.getRequires()) {
                    Intent requiredIntent = policyRegistry.getDefinition(requiredInentName, Intent.class);
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
