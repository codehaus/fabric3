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
import java.util.Set;

import javax.xml.namespace.QName;

import org.fabric3.fabric.policy.helper.ImplementationPolicyHelper;
import org.fabric3.fabric.policy.helper.InteractionPolicyHelper;
import org.fabric3.fabric.policy.infoset.PolicyInfosetBuilder;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicyPhase;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.Namespaces;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.policy.Policy;
import org.fabric3.spi.policy.PolicyResolutionException;
import org.fabric3.spi.policy.PolicyResolver;
import org.fabric3.spi.policy.PolicyResult;
import org.fabric3.util.closure.Closure;
import org.fabric3.util.closure.CollectionUtils;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
public class DefaultPolicyResolver implements PolicyResolver {
    private static final QName IMPLEMENTATION_SYSTEM = new QName(Namespaces.IMPLEMENTATION, "implementation.system");
    private static final QName IMPLEMENTATION_SINGLETON = new QName(Namespaces.IMPLEMENTATION, "singleton");

    /**
     * Closure for filtering intercepted policies.
     */
    private static final Closure<PolicySet, Boolean> INTERCEPTION = new Closure<PolicySet, Boolean>() {
        public Boolean execute(PolicySet policySet) {
            return policySet.getPhase() == PolicyPhase.INTERCEPTION;
        }
    };

    private static final PolicyResult RESULT = new PolicyResult() {

        public List<PolicySet> getInterceptedPolicySets(Operation<?> operation) {
            return Collections.emptyList();
        }

        public Policy getSourcePolicy() {
            return new Policy() {
                public List<QName> getProvidedIntents(Operation<?> operation) {
                    return Collections.emptyList();
                }

                public List<PolicySet> getProvidedPolicySets(Operation<?> operation) {
                    return Collections.emptyList();
                }

                public List<QName> getProvidedIntents() {
                    return Collections.emptyList();
                }

                public List<PolicySet> getProvidedPolicySets() {
                    return Collections.emptyList();
                }
            };
        }

        public Policy getTargetPolicy() {
            return new Policy() {
                public List<QName> getProvidedIntents(Operation<?> operation) {
                    return Collections.emptyList();
                }

                public List<PolicySet> getProvidedPolicySets(Operation<?> operation) {
                    return Collections.emptyList();
                }

                public List<QName> getProvidedIntents() {
                    return Collections.emptyList();
                }

                public List<PolicySet> getProvidedPolicySets() {
                    return Collections.emptyList();
                }
            };
        }

    };

    /**
     * Closure for filtering provided policies by bindings or implementations.
     */
    private static final Closure<PolicySet, Boolean> PROVIDED = new Closure<PolicySet, Boolean>() {
        public Boolean execute(PolicySet policySet) {
            return policySet.getPhase() == PolicyPhase.PROVIDED;
        }
    };

    private final InteractionPolicyHelper interactionPolicyHelper;
    private final ImplementationPolicyHelper implementationPolicyHelper;
    private final PolicyInfosetBuilder policyInfosetBuilder;

    public DefaultPolicyResolver(@Reference InteractionPolicyHelper interactionPolicyHelper,
                                 @Reference ImplementationPolicyHelper implementationPolicyHelper,
                                 @Reference PolicyInfosetBuilder policyInfosetBuilder) {
        this.interactionPolicyHelper = interactionPolicyHelper;
        this.implementationPolicyHelper = implementationPolicyHelper;
        this.policyInfosetBuilder = policyInfosetBuilder;
    }

    /**
     * Resolves all the interaction and implementation intents for the wire.
     *
     * @param serviceContract Service contract for the wire.
     * @param sourceBinding   Source binding.
     * @param targetBinding   Target binding.
     * @param source          Source component.
     * @param target          Target component.
     * @return Policy resolution result.
     * @throws PolicyResolutionException If unable to resolve any policies.
     */
    public PolicyResult resolvePolicies(ServiceContract<?> serviceContract,
                                        LogicalBinding<?> sourceBinding,
                                        LogicalBinding<?> targetBinding,
                                        LogicalComponent<?> source,
                                        LogicalComponent<?> target) throws PolicyResolutionException {
        if (noPolicy(source) && noPolicy(target)) {
            return RESULT;
        }
        PolicyResultImpl policyResult = new PolicyResultImpl();

        for (Operation<?> operation : serviceContract.getOperations()) {

            policyResult.addSourceIntents(operation, interactionPolicyHelper.getProvidedIntents(sourceBinding, operation));

            policyResult.addTargetIntents(operation, interactionPolicyHelper.getProvidedIntents(targetBinding, operation));
            if (target != null) {
                policyResult.addSourceIntents(operation, implementationPolicyHelper.getProvidedIntents(target, operation));
            }

            Set<PolicySet> policies;
            Element policyInfoset;

            policyInfoset = policyInfosetBuilder.buildInfoSet(sourceBinding);
            policies = interactionPolicyHelper.resolveIntents(sourceBinding, operation, policyInfoset);
            policyResult.addSourcePolicySets(operation, CollectionUtils.filter(policies, PROVIDED));
            policyResult.addInterceptedPolicySets(operation, CollectionUtils.filter(policies, INTERCEPTION));

            policyInfoset = policyInfosetBuilder.buildInfoSet(targetBinding);
            policies = interactionPolicyHelper.resolveIntents(targetBinding, operation, policyInfoset);
            policyResult.addTargetPolicySets(operation, CollectionUtils.filter(policies, PROVIDED));
            policyResult.addInterceptedPolicySets(operation, CollectionUtils.filter(policies, INTERCEPTION));

            if (target != null) {
                policyInfoset = policyInfosetBuilder.buildInfoSet(target);
                policies = implementationPolicyHelper.resolveIntents(target, operation, policyInfoset);
                policyResult.addTargetPolicySets(operation, CollectionUtils.filter(policies, PROVIDED));
                policyResult.addInterceptedPolicySets(operation, CollectionUtils.filter(policies, INTERCEPTION));
            }

        }                       

        return policyResult;

    }

    private boolean noPolicy(LogicalComponent<?> component) {
        return component != null && (component.getDefinition().getImplementation().isType(IMPLEMENTATION_SYSTEM)
                || component.getDefinition().getImplementation().isType(IMPLEMENTATION_SINGLETON));
    }

}
