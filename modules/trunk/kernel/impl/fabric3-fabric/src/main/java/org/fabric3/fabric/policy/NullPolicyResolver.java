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


