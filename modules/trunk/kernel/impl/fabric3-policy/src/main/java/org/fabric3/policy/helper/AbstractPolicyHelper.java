/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
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
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalScaArtifact;
import org.fabric3.spi.policy.PolicyResolutionException;
import org.fabric3.spi.services.definitions.DefinitionsRegistry;
import org.fabric3.spi.services.lcm.LogicalComponentManager;

/**
 * Base class for resolving policies.
 *
 * @version $Revision$ $Date$
 */
public class AbstractPolicyHelper {
    protected LogicalComponentManager lcm;
    protected PolicyEvaluator policyEvaluator;
    protected DefinitionsRegistry definitionsRegistry;

    protected AbstractPolicyHelper(DefinitionsRegistry definitionsRegistry, LogicalComponentManager lcm, PolicyEvaluator policyEvaluator) {
        this.definitionsRegistry = definitionsRegistry;
        this.lcm = lcm;
        this.policyEvaluator = policyEvaluator;
    }

    /**
     * Resolves intents to policies.
     *
     * @param requiredIntents the intents to resolve
     * @param target          the target component to resolve against
     * @return the resolved policy sets
     * @throws PolicyResolutionException if there is an error during resolution
     */
    protected Set<PolicySet> resolvePolicies(Set<Intent> requiredIntents, LogicalComponent<?> target) throws PolicyResolutionException {

        Set<PolicySet> policies = new LinkedHashSet<PolicySet>();

        Collection<PolicySet> definitions = definitionsRegistry.getAllDefinitions(PolicySet.class);
        // calculate appliesTo
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

    /**
     * Aggregate intents from ancestors.
     *
     * @param scaArtifact the logical artifact to aggregate intents for
     * @return the agreggated intents
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
