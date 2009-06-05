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
package org.fabric3.policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.policy.Policy;
import org.fabric3.spi.policy.PolicyResult;

/**
 * @version $Revision$ $Date$
 */
public class PolicyResultImpl implements PolicyResult {

    private final PolicyImpl sourcePolicy = new PolicyImpl();
    private final PolicyImpl targetPolicy = new PolicyImpl();

    private final Map<LogicalOperation, List<PolicySet>> interceptedPolicySets = new HashMap<LogicalOperation, List<PolicySet>>();

    public Policy getSourcePolicy() {
        return sourcePolicy;
    }

    public Policy getTargetPolicy() {
        return targetPolicy;
    }

    public List<PolicySet> getInterceptedPolicySets(LogicalOperation operation) {
        return interceptedPolicySets.get(operation);
    }

    void addSourceIntents(LogicalOperation operation, Set<Intent> intents) {
        sourcePolicy.addIntents(operation, intents);
    }

    void addTargetIntents(LogicalOperation operation, Set<Intent> intents) {
        targetPolicy.addIntents(operation, intents);
    }

    void addSourcePolicySets(LogicalOperation operation, Set<PolicySet> policySets) {
        sourcePolicy.addPolicySets(operation, policySets);
    }

    void addTargetPolicySets(LogicalOperation operation, Set<PolicySet> policySets) {
        targetPolicy.addPolicySets(operation, policySets);
    }

    void addInterceptedPolicySets(LogicalOperation operation, Set<PolicySet> policySets) {

        if (!interceptedPolicySets.containsKey(operation)) {
            interceptedPolicySets.put(operation, new ArrayList<PolicySet>());
        }

        List<PolicySet> interceptedSets = interceptedPolicySets.get(operation);
        for (PolicySet policySet : policySets) {
            if (!interceptedSets.contains(policySet)) {
                // Check to see if the policy set has already been added. This can happen for intents specified on service contracts, as they will
                // be picked up on both the reference and service sides of a wire.
                interceptedSets.add(policySet);
            }
        }

    }


}
