/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.fabric.generator.policy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.generator.EffectivePolicy;
import org.fabric3.spi.generator.PolicyMetadata;
import org.fabric3.spi.generator.policy.PolicyResolutionException;
import org.fabric3.spi.generator.policy.PolicyResolver;
import org.fabric3.spi.generator.policy.PolicyResult;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalWire;

/**
 * No-op resolver used during bootstrap.
 *
 * @version $Rev$ $Date$
 */
public class NullPolicyResolver implements PolicyResolver {
    private static final PolicyResult EMPTY_RESULT = new NullPolicyResult();

    public PolicyResult resolvePolicies(LogicalBinding<?> binding) throws PolicyResolutionException {
        return EMPTY_RESULT;
    }

    public PolicyResult resolveCallbackPolicies(LogicalBinding<?> binding) throws PolicyResolutionException {
        return EMPTY_RESULT;
    }

    public PolicyResult resolvePolicies(LogicalConsumer consumer) throws PolicyResolutionException {
        return EMPTY_RESULT;
    }

    public PolicyResult resolveLocalPolicies(LogicalWire wire) throws PolicyResolutionException {
        return EMPTY_RESULT;
    }

    public PolicyResult resolveLocalCallbackPolicies(LogicalWire wire) throws PolicyResolutionException {
        return EMPTY_RESULT;
    }

    public PolicyResult resolveRemotePolicies(LogicalWire wire) throws PolicyResolutionException {
        return EMPTY_RESULT;
    }

    public PolicyResult resolveRemoteCallbackPolicies(LogicalWire wire) throws PolicyResolutionException {
        return EMPTY_RESULT;
    }

    private static class NullPolicyResult implements PolicyResult {
        private PolicyMetadata metadata = new PolicyMetadata();

        public Set<PolicySet> getInterceptedEndpointPolicySets() {
            return Collections.emptySet();
        }

        public Map<LogicalOperation, List<PolicySet>> getInterceptedPolicySets() {
            return Collections.emptyMap();
        }

        public List<PolicySet> getInterceptedPolicySets(LogicalOperation operation) {
            return Collections.emptyList();
        }

        public Map<LogicalOperation, PolicyMetadata> getMetadata() {
            return Collections.emptyMap();
        }

        public PolicyMetadata getMetadata(LogicalOperation operation) {
            return metadata;
        }

        public EffectivePolicy getSourcePolicy() {
            return new NullEffectivePolicy();
        }

        public EffectivePolicy getTargetPolicy() {
            return new NullEffectivePolicy();
        }

    }

    private static class NullEffectivePolicy implements EffectivePolicy {
        public Set<Intent> getEndpointIntents() {
            return Collections.emptySet();
        }

        public Set<PolicySet> getEndpointPolicySets() {
            return Collections.emptySet();
        }

        public List<Intent> getIntents(LogicalOperation operation) {
            return Collections.emptyList();
        }

        public List<PolicySet> getPolicySets(LogicalOperation operation) {
            return Collections.emptyList();
        }

        public List<Intent> getOperationIntents() {
            return Collections.emptyList();
        }

        public Map<LogicalOperation, List<PolicySet>> getOperationPolicySets() {
            return Collections.emptyMap();
        }
    }


}


