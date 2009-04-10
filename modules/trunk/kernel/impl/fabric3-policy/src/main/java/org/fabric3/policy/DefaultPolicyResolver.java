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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.host.Namespaces;
import org.fabric3.model.type.definitions.PolicyPhase;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.policy.helper.ImplementationPolicyHelper;
import org.fabric3.policy.helper.InteractionPolicyHelper;
import org.fabric3.policy.infoset.PolicyEvaluationException;
import org.fabric3.policy.infoset.PolicyEvaluator;
import org.fabric3.spi.model.instance.LogicalAttachPoint;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalScaArtifact;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.policy.Policy;
import org.fabric3.spi.policy.PolicyRegistry;
import org.fabric3.spi.policy.PolicyResolutionException;
import org.fabric3.spi.policy.PolicyResolver;
import org.fabric3.spi.policy.PolicyResult;
import org.fabric3.util.closure.Closure;
import org.fabric3.util.closure.CollectionUtils;

/**
 * @version $Revision$ $Date$
 */
public class DefaultPolicyResolver implements PolicyResolver {
    private static final QName IMPLEMENTATION_SYSTEM = new QName(Namespaces.IMPLEMENTATION, "implementation.system");
    private static final QName IMPLEMENTATION_SINGLETON = new QName(Namespaces.IMPLEMENTATION, "singleton");
    private static final PolicyResult EMPTY_RESULT = new NullPolicyResult();

    /**
     * Closure for filtering intercepted policies.
     */
    private static final Closure<PolicySet, Boolean> INTERCEPTION = new Closure<PolicySet, Boolean>() {
        public Boolean execute(PolicySet policySet) {
            return policySet.getPhase() == PolicyPhase.INTERCEPTION;
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

    private InteractionPolicyHelper interactionPolicyHelper;
    private ImplementationPolicyHelper implementationPolicyHelper;
    private PolicyEvaluator policyEvaluator;
    private PolicyRegistry policyRegistry;

    public DefaultPolicyResolver(@Reference InteractionPolicyHelper interactionPolicyHelper,
                                 @Reference ImplementationPolicyHelper implementationPolicyHelper,
                                 @Reference PolicyEvaluator policyEvaluator,
                                 @Reference PolicyRegistry policyRegistry) {
        this.interactionPolicyHelper = interactionPolicyHelper;
        this.implementationPolicyHelper = implementationPolicyHelper;
        this.policyEvaluator = policyEvaluator;
        this.policyRegistry = policyRegistry;
    }

    public PolicyResult resolvePolicies(List<LogicalOperation> operations,
                                        LogicalBinding<?> sourceBinding,
                                        LogicalBinding<?> targetBinding,
                                        LogicalComponent<?> source,
                                        LogicalComponent<?> target) throws PolicyResolutionException {
        if (noPolicy(source) && noPolicy(target)) {
            return EMPTY_RESULT;
        }
        PolicyResultImpl policyResult = new PolicyResultImpl();

        for (LogicalOperation operation : operations) {
            policyResult.addSourceIntents(operation, interactionPolicyHelper.getProvidedIntents(sourceBinding, operation));

            policyResult.addTargetIntents(operation, interactionPolicyHelper.getProvidedIntents(targetBinding, operation));
            if (target != null) {
                policyResult.addSourceIntents(operation, implementationPolicyHelper.getProvidedIntents(target, operation));
            }

            Set<PolicySet> policies;
            policies = interactionPolicyHelper.resolve(sourceBinding, operation);
            policyResult.addSourcePolicySets(operation, CollectionUtils.filter(policies, PROVIDED));
            policyResult.addInterceptedPolicySets(operation, CollectionUtils.filter(policies, INTERCEPTION));

            policies = interactionPolicyHelper.resolve(targetBinding, operation);
            policyResult.addTargetPolicySets(operation, CollectionUtils.filter(policies, PROVIDED));
            policyResult.addInterceptedPolicySets(operation, CollectionUtils.filter(policies, INTERCEPTION));

            if (target != null) {
                policies = implementationPolicyHelper.resolve(target, operation);
                policyResult.addTargetPolicySets(operation, CollectionUtils.filter(policies, PROVIDED));
                policyResult.addInterceptedPolicySets(operation, CollectionUtils.filter(policies, INTERCEPTION));
            }

        }

        return policyResult;

    }

    public void attachPolicies(LogicalComponent<?> component, boolean incremental) throws PolicyEvaluationException {
        List<PolicySet> policySets = policyRegistry.getExternalAttachmentPolicies();
        for (PolicySet policySet : policySets) {
            List<LogicalScaArtifact<?>> results = policyEvaluator.evaluate(policySet.getAttachTo(), component);
            // attach policy sets
            for (LogicalScaArtifact<?> result : results) {
                attach(policySet.getName(), result, incremental);
            }
        }
    }

    /**
     * Performs the actual attachment on the target artifact.
     *
     * @param policySet   the PolicySet to attach
     * @param target      the target to attach to
     * @param incremental if the attachment is being performed as part of an incremental deployment. If true, the state of the target is set to NEW.
     * @throws PolicyEvaluationException if an error accurs performing the attachment
     */
    void attach(QName policySet, LogicalScaArtifact<?> target, boolean incremental) throws PolicyEvaluationException {
        if (target instanceof LogicalComponent) {
            LogicalComponent<?> component = (LogicalComponent<?>) target;
            if (component.getPolicySets().contains(policySet)) {
                return;
            }
            if (incremental && !component.getPolicySets().contains(policySet)) {
                component.addPolicySet(policySet);
                processComponent(component, policySet, incremental);
            } else if (!incremental) {
                component.addPolicySet(policySet);
            }
        } else if (target instanceof LogicalService) {
            LogicalService service = (LogicalService) target;
            // add the policy to the service but mark bindings as NEW for (re)provisioning
            if (service.getPolicySets().contains(policySet)) {
                return;
            }
            service.addPolicySet(policySet);
            processService(service, policySet, incremental);
        } else if (target instanceof LogicalReference) {
            LogicalReference reference = (LogicalReference) target;
            if (reference.getPolicySets().contains(policySet)) {
                return;
            }
            reference.addPolicySet(policySet);
            processReference(reference, policySet, incremental);

        } else if (target instanceof LogicalOperation) {
            LogicalOperation operation = (LogicalOperation) target;
            if (operation.getPolicySets().contains(policySet)) {
                return;
            }
            operation.addPolicySet(policySet);
            LogicalAttachPoint attachPoint = operation.getParent();
            if (attachPoint instanceof LogicalReference) {
                processReference((LogicalReference) attachPoint, policySet, incremental);
            } else if (attachPoint instanceof LogicalService) {
                processService((LogicalService) attachPoint, policySet, incremental);
            } else {
                throw new PolicyEvaluationException("Invalid policy attachment type: " + target.getClass());
            }
        } else if (target instanceof LogicalBinding) {
            LogicalBinding<?> binding = (LogicalBinding<?>) target;
            if (binding.getPolicySets().contains(policySet)) {
                return;
            }
            binding.addPolicySet(policySet);
            binding.setState(LogicalState.NEW);
        } else {
            throw new PolicyEvaluationException("Invalid policy attachment type: " + target.getClass());
        }
    }

    private void processComponent(LogicalComponent<?> component, QName policySet, boolean incremental) {
        // do not mark the component as new, just the wires since the implementation does not need to be reprovisioned
        for (LogicalReference reference : component.getReferences()) {
            processReference(reference, policySet, incremental);
        }
        for (LogicalService service : component.getServices()) {
            processService(service, policySet, incremental);
        }
    }

    private void processService(LogicalService service, QName policySet, boolean incremental) {
        for (LogicalBinding<?> binding : service.getBindings()) {
            if (incremental && binding.getPolicySets().contains(policySet)) {
                continue;
            }
            binding.setState(LogicalState.NEW);
        }
        // TODO check collocated wires, i.e. references attached directly to the service so they can be reprovisioned
    }

    private void processReference(LogicalReference reference, QName policySet, boolean incremental) {
        for (LogicalWire wire : reference.getWires()) {
            wire.setState(LogicalState.NEW);
        }
        for (LogicalBinding<?> binding : reference.getBindings()) {
            if (incremental && binding.getPolicySets().contains(policySet)) {
                continue;
            }
            binding.setState(LogicalState.NEW);
        }
    }

    private boolean noPolicy(LogicalComponent<?> component) {
        return component != null && (component.getDefinition().getImplementation().isType(IMPLEMENTATION_SYSTEM)
                || component.getDefinition().getImplementation().isType(IMPLEMENTATION_SINGLETON));
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
