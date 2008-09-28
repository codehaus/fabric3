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

import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.policy.Policy;
import org.fabric3.spi.policy.PolicyResolutionException;
import org.fabric3.spi.policy.PolicyResolver;
import org.fabric3.spi.policy.PolicyResult;

/**
 * @version $Revision$ $Date$
 */
public class NullPolicyResolver implements PolicyResolver {
    
    public PolicyResult resolvePolicies(ServiceContract<?> serviceContract,
                                            LogicalBinding<?> sourceBinding, 
                                            LogicalBinding<?> targetBinding, 
                                            LogicalComponent<?> source, 
                                            LogicalComponent<?> target) throws PolicyResolutionException {
        return new PolicyResult() {

            public List<PolicySet> getInterceptedPolicySets(Operation<?> operation) {
                return Collections.emptyList();
            }

            public Policy getSourcePolicy() {
                return new Policy() {
                    public List<Intent> getProvidedIntents(Operation<?> operation) {
                        return Collections.emptyList();
                    }
                    public List<PolicySet> getProvidedPolicySets(Operation<?> operation) {
                        return Collections.emptyList();
                    }
                };
            }

            public Policy getTargetPolicy() {
                return new Policy() {
                    public List<Intent> getProvidedIntents(Operation<?> operation) {
                        return Collections.emptyList();
                    }
                    public List<PolicySet> getProvidedPolicySets(Operation<?> operation) {
                        return Collections.emptyList();
                    }
                };
            }
            
        };
    }

}
