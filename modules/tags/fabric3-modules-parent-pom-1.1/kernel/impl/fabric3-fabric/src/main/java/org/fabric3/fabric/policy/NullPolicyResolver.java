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
package org.fabric3.fabric.policy;

import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.policy.Policy;
import org.fabric3.spi.policy.PolicyResolutionException;
import org.fabric3.spi.policy.PolicyResolver;
import org.fabric3.spi.policy.PolicyResult;

/**
 * No-op resolver used during bootstrap.
 *
 * @version $Revision$ $Date$
 */
public class NullPolicyResolver implements PolicyResolver {
    private static final PolicyResult EMPTY_RESULT = new NullPolicyResult();

    public void attachPolicies(LogicalComponent<?> component, boolean incremental) {
        // no-op
    }

    public void attachPolicies(List<PolicySet> policySets, LogicalComponent<?> component, boolean incremental) throws PolicyResolutionException {
        // no-op
    }

    public void detachPolicies(List<PolicySet> policySets, LogicalComponent<?> component) throws PolicyResolutionException {
        // no-op
    }

    public PolicyResult resolvePolicies(List<LogicalOperation> operations,
                                        LogicalBinding<?> sourceBinding,
                                        LogicalBinding<?> targetBinding,
                                        LogicalComponent<?> source,
                                        LogicalComponent<?> target) throws PolicyResolutionException {
        return EMPTY_RESULT;
    }

    private static class NullPolicyResult implements PolicyResult {

        public List<PolicySet> getInterceptedPolicySets(LogicalOperation operation) {
            return Collections.emptyList();
        }

        public Policy getSourcePolicy() {
            return new NullPolicy();
        }

        public Policy getTargetPolicy() {
            return new NullPolicy();
        }

    }

    private static class NullPolicy implements Policy {
        public List<QName> getProvidedIntents(LogicalOperation operation) {
            return Collections.emptyList();
        }

        public List<PolicySet> getProvidedPolicySets(LogicalOperation operation) {
            return Collections.emptyList();
        }

        public List<QName> getProvidedIntents() {
            return Collections.emptyList();
        }

        public List<PolicySet> getProvidedPolicySets() {
            return Collections.emptyList();
        }
    }


}


