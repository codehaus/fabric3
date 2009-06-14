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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.definitions.BindingType;
import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.policy.infoset.PolicyEvaluator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalScaArtifact;
import org.fabric3.spi.policy.PolicyRegistry;
import org.fabric3.spi.policy.PolicyResolutionException;
import org.fabric3.spi.services.lcm.LogicalComponentManager;

/**
 * @version $Revision$ $Date$
 */
public class InteractionPolicyHelperImpl extends AbstractPolicyHelper implements InteractionPolicyHelper {

    public InteractionPolicyHelperImpl(@Reference PolicyRegistry policyRegistry,
                                       @Reference LogicalComponentManager lcm,
                                       @Reference PolicyEvaluator policyEvaluator) {
        super(policyRegistry, lcm, policyEvaluator);
    }

    public Set<Intent> getProvidedIntents(LogicalBinding<?> binding, LogicalOperation operation) throws PolicyResolutionException {

        QName type = binding.getDefinition().getType();
        BindingType bindingType = policyRegistry.getDefinition(type, BindingType.class);

        // FIXME This should not happen, all binding types should be registsred
        if (bindingType == null) {
            return Collections.emptySet();
        }

        Set<QName> mayProvidedIntents = bindingType.getMayProvide();

        Set<Intent> requiredIntents = getRequestedIntents(binding, operation);

        Set<Intent> intentsToBeProvided = new LinkedHashSet<Intent>();
        for (Intent intent : requiredIntents) {
            if (mayProvidedIntents.contains(intent.getName())) {
                intentsToBeProvided.add(intent);
            }
        }

        return intentsToBeProvided;

    }


    public Set<PolicySet> resolve(LogicalBinding<?> binding, LogicalOperation operation) throws PolicyResolutionException {

        QName type = binding.getDefinition().getType();
        BindingType bindingType = policyRegistry.getDefinition(type, BindingType.class);

        Set<QName> alwaysProvidedIntents = new LinkedHashSet<QName>();
        Set<QName> mayProvidedIntents = new LinkedHashSet<QName>();

        // FIXME This should not happen, all binding types should be registsred
        if (bindingType != null) {
            alwaysProvidedIntents = bindingType.getAlwaysProvide();
            mayProvidedIntents = bindingType.getMayProvide();
        }

        Set<Intent> requiredIntents = getRequestedIntents(binding, operation);
        Set<Intent> requiredIntentsCopy = new HashSet<Intent>(requiredIntents);

        // Remove intents that are provided
        for (Intent intent : requiredIntentsCopy) {
            QName intentName = intent.getName();
            if (alwaysProvidedIntents.contains(intentName) || mayProvidedIntents.contains(intentName)) {
                requiredIntents.remove(intent);
            }
        }
        Set<QName> policySets = aggregatePolicySets(binding, operation);
        if (requiredIntents.isEmpty() && policySets.isEmpty()) {
            // short-circuit intent resolution
            return Collections.emptySet();
        }

        LogicalComponent<?> target = binding.getParent().getParent();
        Set<PolicySet> policies = resolvePolicies(requiredIntents, target);
        if (!requiredIntents.isEmpty()) {
            throw new PolicyResolutionException("Unable to resolve all intents", requiredIntents);
        }

        for (QName name : policySets) {
            PolicySet policySet = policyRegistry.getDefinition(name, PolicySet.class);
            policies.add(policySet);
        }

        return policies;

    }

    private Set<Intent> getRequestedIntents(LogicalBinding<?> logicalBinding, LogicalOperation operation) throws PolicyResolutionException {

        // Aggregate all the intents from the ancestors
        Set<QName> intentNames = new LinkedHashSet<QName>();
        intentNames.addAll(operation.getIntents());
        intentNames.addAll(logicalBinding.getDefinition().getIntents());
        intentNames.addAll(aggregateIntents(logicalBinding));

        // Expand all the profile intents
        Set<Intent> requiredIntents = resolveProfileIntents(intentNames);

        // Remove intents not applicable to the artifact
        filterInvalidIntents(Intent.BINDING, requiredIntents);

        return requiredIntents;

    }

    /**
     * Aggregates policy sets from ancestors.
     *
     * @param binding   the binding
     * @param operation the operaton
     * @return the agreggated policy sets
     */
    private Set<QName> aggregatePolicySets(LogicalBinding<?> binding, LogicalOperation operation) {
        LogicalScaArtifact<?> temp = operation;
        Set<QName> policySetNames = new LinkedHashSet<QName>();
        while (temp != null) {
            policySetNames.addAll(temp.getPolicySets());
            temp = temp.getParent();
        }
        // also attach policy sets specified on the binding
        for (QName name : binding.getPolicySets()) {
            policySetNames.add(name);
        }
        return policySetNames;
    }


}
