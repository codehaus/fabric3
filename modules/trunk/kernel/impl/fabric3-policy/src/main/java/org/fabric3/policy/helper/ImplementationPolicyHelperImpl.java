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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.definitions.ImplementationType;
import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.policy.infoset.PolicyEvaluator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.policy.PolicyResolutionException;
import org.fabric3.spi.policy.PolicyRegistry;
import org.fabric3.spi.services.lcm.LogicalComponentManager;

/**
 * @version $Revision$ $Date$
 */
public class ImplementationPolicyHelperImpl extends AbstractPolicyHelper implements ImplementationPolicyHelper {

    public ImplementationPolicyHelperImpl(@Reference PolicyRegistry policyRegistry,
                                          @Reference LogicalComponentManager lcm,
                                          @Reference PolicyEvaluator policyEvaluator) {
        super(policyRegistry, lcm, policyEvaluator);
    }

    public Set<Intent> getProvidedIntents(LogicalComponent<?> logicalComponent, LogicalOperation operation) throws PolicyResolutionException {
        Implementation<?> implementation = logicalComponent.getDefinition().getImplementation();
        QName type = implementation.getType();
        ImplementationType implementationType = policyRegistry.getDefinition(type, ImplementationType.class);

        // FIXME This should not happen, all implementation types should be registsred
        if (implementationType == null) {
            return Collections.emptySet();
        }

        Set<QName> mayProvidedIntents = implementationType.getMayProvide();

        Set<Intent> requiredIntents = getRequestedIntents(logicalComponent, operation);

        Set<Intent> intentsToBeProvided = new LinkedHashSet<Intent>();
        for (Intent intent : requiredIntents) {
            if (mayProvidedIntents.contains(intent.getName())) {
                intentsToBeProvided.add(intent);
            }
        }
        return intentsToBeProvided;

    }

    public Set<PolicySet> resolveIntents(LogicalComponent<?> logicalComponent, LogicalOperation operation) throws PolicyResolutionException {

        Implementation<?> implementation = logicalComponent.getDefinition().getImplementation();
        QName type = implementation.getType();
        ImplementationType implementationType = policyRegistry.getDefinition(type, ImplementationType.class);

        Set<QName> alwaysProvidedIntents = new LinkedHashSet<QName>();
        Set<QName> mayProvidedIntents = new LinkedHashSet<QName>();

        // FIXME This should not happen, all implementation types should be registsred
        if (implementationType != null) {
            alwaysProvidedIntents = implementationType.getAlwaysProvide();
            mayProvidedIntents = implementationType.getMayProvide();
        }

        Set<Intent> requiredIntents = getRequestedIntents(logicalComponent, operation);
        Set<Intent> requiredIntentsCopy = new HashSet<Intent>(requiredIntents);

        // Remove intents that are provided
        for (Intent intent : requiredIntentsCopy) {
            QName intentName = intent.getName();
            if (alwaysProvidedIntents.contains(intentName) || mayProvidedIntents.contains(intentName)) {
                requiredIntents.remove(intent);
            }
        }
        if (requiredIntents.isEmpty()) {
            // short-circuit intent resolution
            return Collections.emptySet();
        }
        Set<PolicySet> policies = resolvePolicies(requiredIntents, logicalComponent);
        if (!requiredIntents.isEmpty()) {
            throw new PolicyResolutionException("Unable to resolve all intents", requiredIntents);
        }

        return policies;

    }

    private Set<Intent> getRequestedIntents(LogicalComponent<?> logicalComponent, LogicalOperation operation) throws PolicyResolutionException {

        // Aggregate all the intents from the ancestors
        Set<QName> intentNames = new LinkedHashSet<QName>();
        intentNames.addAll(operation.getIntents());
        intentNames.addAll(logicalComponent.getDefinition().getImplementation().getIntents());
        intentNames.addAll(aggregateIntents(logicalComponent));

        // Expand all the profile intents
        Set<Intent> requiredIntents = resolveProfileIntents(intentNames);

        // Remove intents not applicable to the artifact
        filterInvalidIntents(Intent.IMPLEMENTATION, requiredIntents);

        return requiredIntents;

    }

}
