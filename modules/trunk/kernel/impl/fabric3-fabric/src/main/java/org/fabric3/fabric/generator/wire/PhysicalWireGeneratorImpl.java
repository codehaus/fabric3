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
package org.fabric3.fabric.generator.wire;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ResourceDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.model.type.service.Operation;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorNotFoundException;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.generator.ResourceWireGenerator;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.type.LocalBindingDefinition;
import org.fabric3.spi.policy.Policy;
import org.fabric3.spi.policy.PolicyResolutionException;
import org.fabric3.spi.policy.PolicyResolver;
import org.fabric3.spi.policy.PolicyResult;

/**
 * Default implementation of PhysicalWireGenerator.
 *
 * @version $Revision$ $Date$
 */
public class PhysicalWireGeneratorImpl implements PhysicalWireGenerator {

    private final GeneratorRegistry generatorRegistry;
    private final PolicyResolver policyResolver;
    private final PhysicalOperationHelper physicalOperationHelper;

    /**
     * Constructor.
     *
     * @param generatorRegistry       the generator registry.
     * @param policyResolver          the policy resolver
     * @param physicalOperationHelper the physical operation helper
     */
    public PhysicalWireGeneratorImpl(@Reference GeneratorRegistry generatorRegistry,
                                     @Reference PolicyResolver policyResolver,
                                     @Reference PhysicalOperationHelper physicalOperationHelper) {
        this.generatorRegistry = generatorRegistry;
        this.policyResolver = policyResolver;
        this.physicalOperationHelper = physicalOperationHelper;
    }

    @SuppressWarnings("unchecked")
    public PhysicalWireDefinition generateResourceWire(LogicalResource<?> resource) throws GenerationException {

        ResourceDefinition resourceDefinition = resource.getResourceDefinition();
        ServiceContract<?> serviceContract = resourceDefinition.getServiceContract();
        LogicalComponent<?> component = resource.getParent();
        // Generates the source side of the wire
        ComponentGenerator sourceGenerator = getGenerator(component);
        PhysicalWireSourceDefinition pwsd = sourceGenerator.generateResourceWireSource(component, resource);
        pwsd.setClassLoaderId(component.getDefinition().getContributionUri());
        // Generates the target side of the wire
        ResourceWireGenerator targetGenerator = getGenerator(resourceDefinition);
        @SuppressWarnings("unchecked")
        PhysicalWireTargetDefinition pwtd = targetGenerator.generateWireTargetDefinition(resource);
        pwtd.setClassLoaderId(resource.getParent().getDefinition().getContributionUri());
        boolean optimizable = pwtd.isOptimizable();

        // Create the wire from the component to the resource
        Set<PhysicalOperationDefinition> operations = generateOperations(serviceContract, null, null);
        PhysicalWireDefinition wireDefinition = new PhysicalWireDefinition(pwsd, pwtd, operations);
        wireDefinition.setOptimizable(optimizable);

        return wireDefinition;

    }

    public PhysicalWireDefinition generateUnboundWire(LogicalReference reference, LogicalService service) throws GenerationException {
        LogicalComponent source = reference.getParent();
        LogicalComponent target = service.getParent();
        ReferenceDefinition referenceDefinition = reference.getDefinition();
        ServiceContract<?> serviceContract = referenceDefinition.getServiceContract();

        LogicalBinding<LocalBindingDefinition> sourceBinding = new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, reference);
        LogicalBinding<LocalBindingDefinition> targetBinding = new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, service);

        PolicyResult policyResult = resolvePolicies(serviceContract, sourceBinding, targetBinding, source, target);
        Policy sourcePolicy = policyResult.getSourcePolicy();
        Policy targetPolicy = policyResult.getTargetPolicy();

        ComponentGenerator targetGenerator = getGenerator(target);
        // generate metadata for the target side of the wire
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateWireTarget(service, target, targetPolicy);
        targetDefinition.setClassLoaderId(target.getDefinition().getContributionUri());
        ServiceContract<?> callbackContract = reference.getDefinition().getServiceContract().getCallbackContract();
        if (callbackContract != null) {
            // if there is a callback wire associated with this forward wire, calculate its URI
            URI callbackUri = generateCallbackUri(source, callbackContract, referenceDefinition.getName());
            targetDefinition.setCallbackUri(callbackUri);
        }

        ComponentGenerator sourceGenerator = getGenerator(source);
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(source, reference, sourcePolicy);
        sourceDefinition.setClassLoaderId(source.getDefinition().getContributionUri());
        sourceDefinition.setKey(target.getDefinition().getKey());

        Set<PhysicalOperationDefinition> operations = generateOperations(serviceContract, policyResult, sourceBinding);
        // xcv 123 this needs to be made more explicit
        QName sourceDeployable = null;
        QName targetDeployable = null;
        if (LogicalState.NEW == target.getState()) {
            sourceDeployable = source.getDeployable();
            targetDeployable = target.getDeployable();
        }

        PhysicalWireDefinition wireDefinition =
                new PhysicalWireDefinition(sourceDefinition, sourceDeployable, targetDefinition, targetDeployable, operations);
        boolean optimizable = sourceDefinition.isOptimizable() &&
                targetDefinition.isOptimizable() &&
                checkOptimization(serviceContract, operations);

        wireDefinition.setOptimizable(optimizable);

        return wireDefinition;

    }

    public <S extends LogicalComponent<?>, T extends LogicalComponent<?>> PhysicalWireDefinition generateUnboundCallbackWire(S source,
                                                                                                                             LogicalReference reference,
                                                                                                                             T target)
            throws GenerationException {

        ServiceContract<?> contract = reference.getDefinition().getServiceContract().getCallbackContract();
        LogicalService callbackService = target.getService(contract.getInterfaceName());
        assert callbackService != null;
        LogicalBinding<LocalBindingDefinition> sourceBinding =
                new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, callbackService);
        LogicalBinding<LocalBindingDefinition> targetBinding = new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, reference);
        ComponentGenerator<S> sourceGenerator = getGenerator(source);
        ComponentGenerator<T> targetGenerator = getGenerator(target);
        PolicyResult policyResult = resolvePolicies(contract, sourceBinding, targetBinding, source, target);
        Policy sourcePolicy = policyResult.getSourcePolicy();
        Policy targetPolicy = policyResult.getTargetPolicy();
        Set<PhysicalOperationDefinition> callbackOperations = generateOperations(contract, policyResult, targetBinding);
        PhysicalWireSourceDefinition sourceDefinition =
                sourceGenerator.generateCallbackWireSource(source, contract, sourcePolicy);
        sourceDefinition.setClassLoaderId(source.getDefinition().getContributionUri());
        PhysicalWireTargetDefinition targetDefinition =
                targetGenerator.generateWireTarget(callbackService, target, targetPolicy);
        targetDefinition.setClassLoaderId(target.getDefinition().getContributionUri());
        targetDefinition.setCallback(true);
        PhysicalWireDefinition wireDefinition =
                new PhysicalWireDefinition(sourceDefinition, targetDefinition, callbackOperations);
        wireDefinition.setOptimizable(false);

        return wireDefinition;

    }

    @SuppressWarnings("unchecked")

    public PhysicalWireDefinition generateBoundServiceWire(LogicalService service, LogicalBinding<?> binding, URI callbackUri)
            throws GenerationException {

        LogicalComponent<?> component = service.getParent();
        // use the service contract from the binding's parent service if it is defined, otherwise default to the one
        // defined on the original component
        Bindable bindable = binding.getParent();
        assert bindable instanceof LogicalService;
        LogicalService logicalService = (LogicalService) bindable;

        ServiceContract<?> contract = logicalService.getDefinition().getServiceContract();
        if (contract == null) {
            contract = service.getDefinition().getServiceContract();
        }

        LogicalBinding<LocalBindingDefinition> targetBinding = new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, service);

        PolicyResult policyResult = resolvePolicies(contract, binding, targetBinding, null, component);
        Policy sourcePolicy = policyResult.getSourcePolicy();
        Policy targetPolicy = policyResult.getTargetPolicy();


        URI targetUri = service.getPromotedUri();
        LogicalService targetService;
        if (targetUri == null) {
            // the service is on the component
            targetService = service;
        } else {
            // service is defined on a composite and wired to a component service
            targetService = component.getService(targetUri.getFragment());
        }

        ComponentGenerator targetGenerator = getGenerator(component);
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateWireTarget(targetService, component, targetPolicy);
        targetDefinition.setClassLoaderId(targetService.getParent().getDefinition().getContributionUri());
        targetDefinition.setCallbackUri(callbackUri);
        BindingGenerator sourceGenerator = getGenerator(binding);
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(binding, sourcePolicy, service.getDefinition());
        sourceDefinition.setClassLoaderId(binding.getParent().getParent().getDefinition().getContributionUri());

        Set<PhysicalOperationDefinition> operations = generateOperations(contract, policyResult, binding);
        PhysicalWireDefinition pwd = new PhysicalWireDefinition(sourceDefinition, targetDefinition, operations);
        boolean optimizable = sourceDefinition.isOptimizable() &&
                targetDefinition.isOptimizable() &&
                checkOptimization(contract, operations);

        pwd.setOptimizable(optimizable);
        return pwd;

    }

    @SuppressWarnings("unchecked")
    public PhysicalWireDefinition generateBoundReferenceWire(LogicalReference reference, LogicalBinding<?> binding) throws GenerationException {

        LogicalComponent component = reference.getParent();
        ReferenceDefinition referenceDefinition = reference.getDefinition();
        ServiceContract<?> contract = referenceDefinition.getServiceContract();
        LogicalBinding<LocalBindingDefinition> sourceBinding = new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, reference);

        PolicyResult policyResult = resolvePolicies(contract, sourceBinding, binding, component, null);
        Policy sourcePolicy = policyResult.getSourcePolicy();
        Policy targetPolicy = policyResult.getTargetPolicy();

        BindingGenerator targetGenerator = getGenerator(binding);
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateWireTarget(binding, targetPolicy, contract);
        ServiceContract<?> callbackContract = contract.getCallbackContract();
        if (callbackContract != null) {
            // if there is a callback wire associated with this forward wire, calculate its URI
            URI callbackUri = generateCallbackUri(component, callbackContract, referenceDefinition.getName());
            targetDefinition.setCallbackUri(callbackUri);
        }
        targetDefinition.setKey(binding.getDefinition().getKey());
        targetDefinition.setClassLoaderId(binding.getParent().getParent().getDefinition().getContributionUri());


        ComponentGenerator sourceGenerator = getGenerator(component);

        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(component, reference, sourcePolicy);
        sourceDefinition.setClassLoaderId(component.getDefinition().getContributionUri());

        Set<PhysicalOperationDefinition> operation = generateOperations(contract, policyResult, binding);

        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, operation);

    }

    @SuppressWarnings({"unchecked"})
    public <C extends LogicalComponent<?>> PhysicalWireDefinition generateBoundCallbackRerenceWire(LogicalReference reference,
                                                                                                   LogicalBinding<?> binding,
                                                                                                   C component) throws GenerationException {
        ReferenceDefinition definition = reference.getDefinition();
        ServiceContract<?> contract = definition.getServiceContract();
        ServiceContract<?> callbackContract = contract.getCallbackContract();

        LogicalService callbackService = component.getService(callbackContract.getInterfaceName());

        ServiceDefinition serviceDefinition = callbackService.getDefinition();

        LogicalBinding<LocalBindingDefinition> sourceBinding = new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, reference);

        PolicyResult policyResult = resolvePolicies(callbackContract, sourceBinding, binding, component, null);
        Policy sourcePolicy = policyResult.getSourcePolicy();
        Policy targetPolicy = policyResult.getTargetPolicy();
        BindingGenerator bindingGenerator = getGenerator(binding);
        ComponentGenerator<C> componentGenerator = getGenerator(component);

        PhysicalWireSourceDefinition sourceDefinition = bindingGenerator.generateWireSource(binding, targetPolicy, serviceDefinition);
        sourceDefinition.setClassLoaderId(binding.getParent().getParent().getDefinition().getContributionUri());
        PhysicalWireTargetDefinition targetDefinition = componentGenerator.generateWireTarget(callbackService, component, sourcePolicy);
        targetDefinition.setClassLoaderId(callbackService.getParent().getDefinition().getContributionUri());
        targetDefinition.setCallback(true);
        Set<PhysicalOperationDefinition> operation = generateOperations(callbackContract, policyResult, binding);
        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, operation);

    }

    @SuppressWarnings({"unchecked"})
    public <C extends LogicalComponent<?>> PhysicalWireDefinition generateBoundCallbackServiceWire(C component, LogicalService service,
                                                                                                   LogicalBinding<?> binding)
            throws GenerationException {

        // use the service contract from the binding's parent service if it is defined, otherwise default to the one
        // defined on the original component
        Bindable bindable = binding.getParent();
        assert bindable instanceof LogicalService;
        LogicalService logicalService = (LogicalService) bindable;

        ServiceContract<?> contract = logicalService.getDefinition().getServiceContract();
        if (contract == null) {
            contract = service.getDefinition().getServiceContract();
        }
        ServiceContract<?> callbackContract = contract.getCallbackContract();

        // TODO policies are not correctly calculated
        LogicalBinding<LocalBindingDefinition> targetBinding = new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, service);
        PolicyResult policyResult = resolvePolicies(callbackContract, binding, targetBinding, null, component);
        Policy targetPolicy = policyResult.getSourcePolicy();
        Policy sourcePolicy = policyResult.getTargetPolicy();

        ComponentGenerator<C> componentGenerator = getGenerator(component);
        PhysicalWireSourceDefinition sourceDefinition = componentGenerator.generateCallbackWireSource(component, callbackContract, sourcePolicy);
        sourceDefinition.setClassLoaderId(component.getDefinition().getContributionUri());

        BindingGenerator bindingGenerator = getGenerator(binding);
        // xcv FIXME refactor null param to use ServiceContract
        PhysicalWireTargetDefinition targetDefinition = bindingGenerator.generateWireTarget(binding, targetPolicy, callbackContract);
        targetDefinition.setClassLoaderId(binding.getParent().getParent().getDefinition().getContributionUri());

        Set<PhysicalOperationDefinition> operations = generateOperations(callbackContract, policyResult, binding);
        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, operations);

    }

    private Set<PhysicalOperationDefinition> generateOperations(ServiceContract<?> contract,
                                                                PolicyResult policyResult,
                                                                LogicalBinding<?> logicalBinding) throws GenerationException {

        List<? extends Operation<?>> operations = contract.getOperations();
        Set<PhysicalOperationDefinition> physicalOperations = new HashSet<PhysicalOperationDefinition>(operations.size());

        for (Operation<?> operation : operations) {
            PhysicalOperationDefinition physicalOperation = physicalOperationHelper.mapOperation(operation);
            if (policyResult != null) {
                List<PolicySet> policies = policyResult.getInterceptedPolicySets(operation);
                Set<PhysicalInterceptorDefinition> interceptors = generateInterceptorDefinitions(policies, operation, logicalBinding);
                physicalOperation.setInterceptors(interceptors);
            }
            physicalOperations.add(physicalOperation);
        }

        return physicalOperations;

    }

    @SuppressWarnings("unchecked")
    private Set<PhysicalInterceptorDefinition> generateInterceptorDefinitions(List<PolicySet> policies,
                                                                              Operation<?> operation,
                                                                              LogicalBinding<?> logicalBinding) throws GenerationException {

        if (policies == null) {
            return Collections.EMPTY_SET;
        }

        Set<PhysicalInterceptorDefinition> interceptors = new LinkedHashSet<PhysicalInterceptorDefinition>();
        for (PolicySet policy : policies) {
            QName qName = policy.getExtensionName();
            InterceptorDefinitionGenerator idg = generatorRegistry.getInterceptorDefinitionGenerator(qName);
            PhysicalInterceptorDefinition pid = idg.generate(policy.getExtension(), operation, logicalBinding);
            if (pid != null) {
                pid.setClassLoaderId(logicalBinding.getParent().getParent().getDefinition().getContributionUri());
                interceptors.add(pid);
            }
        }
        return interceptors;

    }

    private PolicyResult resolvePolicies(ServiceContract<?> serviceContract,
                                         LogicalBinding<?> sourceBinding,
                                         LogicalBinding<?> targetBinding,
                                         LogicalComponent<?> source,
                                         LogicalComponent<?> target) throws PolicyGenerationException {

        try {
            return policyResolver.resolvePolicies(serviceContract, sourceBinding, targetBinding, source, target);
        } catch (PolicyResolutionException e) {
            throw new PolicyGenerationException(e);
        }

    }

    private <S extends LogicalComponent<?>> URI generateCallbackUri(S source, ServiceContract<?> contract, String sourceName)
            throws CallbackServiceNotFoundException {
        LogicalService candidate = null;
        for (LogicalService entry : source.getServices()) {
            if (contract.isAssignableFrom(entry.getDefinition().getServiceContract())) {
                candidate = entry;
                break;
            }
        }
        if (candidate == null) {
            String name = contract.getInterfaceName();
            URI uri = source.getUri();
            throw new CallbackServiceNotFoundException("Callback service not found: "
                    + name + " on component: " + uri + " originating from reference :" + sourceName, name);
        }
        return URI.create(source.getUri().toString() + "#" + candidate.getDefinition().getName());
    }

    @SuppressWarnings("unchecked")
    private <C extends LogicalComponent<?>> ComponentGenerator<C> getGenerator(C component) throws GeneratorNotFoundException {
        Implementation<?> implementation = component.getDefinition().getImplementation();
        return (ComponentGenerator<C>) generatorRegistry.getComponentGenerator(implementation.getClass());
    }

    @SuppressWarnings("unchecked")
    private <T extends ResourceDefinition> ResourceWireGenerator<?, T> getGenerator(T definition) throws GeneratorNotFoundException {
        return (ResourceWireGenerator<?, T>) generatorRegistry.getResourceWireGenerator(definition.getClass());
    }

    @SuppressWarnings("unchecked")
    private <T extends BindingDefinition> BindingGenerator<?, ?, T> getGenerator(LogicalBinding<T> binding) throws GeneratorNotFoundException {
        return (BindingGenerator<?, ?, T>) generatorRegistry.getBindingGenerator(binding.getDefinition().getClass());
    }

    private boolean checkOptimization(ServiceContract<?> serviceContract, Set<PhysicalOperationDefinition> operationDefinitions) {

        if (serviceContract.isConversational()) {
            return false;
        }

        if (serviceContract.isRemotable()) {
            return false;
        }

        for (PhysicalOperationDefinition operation : operationDefinitions) {
            if (!operation.getInterceptors().isEmpty()) {
                return false;
            }
        }

        return true;

    }

}
