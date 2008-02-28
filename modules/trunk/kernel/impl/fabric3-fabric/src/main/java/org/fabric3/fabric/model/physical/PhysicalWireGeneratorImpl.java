/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.fabric.model.physical;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.fabric3.fabric.generator.PolicyException;
import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ResourceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.definitions.PolicySet;
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
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.type.SCABindingDefinition;
import org.fabric3.spi.policy.Policy;
import org.fabric3.spi.policy.PolicyResolutionException;
import org.fabric3.spi.policy.PolicyResolver;
import org.fabric3.spi.policy.PolicyResult;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class PhysicalWireGeneratorImpl implements PhysicalWireGenerator {

    private final GeneratorRegistry generatorRegistry;
    private final PolicyResolver policyResolver;
    private final PhysicalOperationHelper physicalOperationHelper;

    /**
     * Injects generator registry and assembly store.
     *
     * @param generatorRegistry       Generator registry.
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
    public <C extends LogicalComponent<?>> PhysicalWireDefinition generateResourceWire(C source, LogicalResource<?> resource)
            throws GenerationException {

        ResourceDefinition resourceDefinition = resource.getResourceDefinition();
        ServiceContract<?> serviceContract = resourceDefinition.getServiceContract();

        // Generates the source side of the wire
        ComponentGenerator<C> sourceGenerator = getGenerator(source);
        PhysicalWireSourceDefinition pwsd = sourceGenerator.generateResourceWireSource(source, resource);

        // Generates the target side of the wire
        ResourceWireGenerator targetGenerator = getGenerator(resourceDefinition);
        @SuppressWarnings("unchecked")
        PhysicalWireTargetDefinition pwtd = targetGenerator.generateWireTargetDefinition(resource);
        boolean optimizable = pwtd.isOptimizable();

        // Create the wire from the component to the resource
        Set<PhysicalOperationDefinition> operations = generateOperations(serviceContract, null, null);
        PhysicalWireDefinition wireDefinition = new PhysicalWireDefinition(pwsd, pwtd, operations);
        wireDefinition.setOptimizable(optimizable);

        return wireDefinition;

    }

    public <S extends LogicalComponent<?>, T extends LogicalComponent<?>> PhysicalWireDefinition generateUnboundWire(S source,
                                                                                                                     LogicalReference reference,
                                                                                                                     LogicalService service,
                                                                                                                     T target)
            throws GenerationException {

        ReferenceDefinition referenceDefinition = reference.getDefinition();
        ServiceContract<?> serviceContract = referenceDefinition.getServiceContract();

        LogicalBinding<SCABindingDefinition> sourceBinding = new LogicalBinding<SCABindingDefinition>(SCABindingDefinition.INSTANCE, reference);
        LogicalBinding<SCABindingDefinition> targetBinding = new LogicalBinding<SCABindingDefinition>(SCABindingDefinition.INSTANCE, service);

        PolicyResult policyResult = resolvePolicies(serviceContract, sourceBinding, targetBinding, source, target);
        Policy sourcePolicy = policyResult.getSourcePolicy();
        Policy targetPolicy = policyResult.getTargetPolicy();

        ComponentGenerator<T> targetGenerator = getGenerator(target);
        // generate metadata for the target side of the wire
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateWireTarget(service, target, targetPolicy);
        ServiceContract<?> callbackContract = reference.getDefinition().getServiceContract().getCallbackContract();
        if (callbackContract != null) {
            // if there is a callback wire associated with this forward wire, calculate its URI
            URI callbackUri = generateCallbackUri(source, callbackContract, referenceDefinition.getName());
            targetDefinition.setCallbackUri(callbackUri);
        }

        ComponentGenerator<S> sourceGenerator = getGenerator(source);
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(source, reference, sourcePolicy);
        sourceDefinition.setKey(target.getDefinition().getKey());

        Set<PhysicalOperationDefinition> operations = generateOperations(serviceContract, policyResult, sourceBinding);

        PhysicalWireDefinition wireDefinition = new PhysicalWireDefinition(sourceDefinition, targetDefinition, operations);
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
        LogicalBinding<SCABindingDefinition> sourceBinding = new LogicalBinding<SCABindingDefinition>(SCABindingDefinition.INSTANCE, callbackService);
        LogicalBinding<SCABindingDefinition> targetBinding = new LogicalBinding<SCABindingDefinition>(SCABindingDefinition.INSTANCE, reference);
        ComponentGenerator<S> sourceGenerator = getGenerator(source);
        ComponentGenerator<T> targetGenerator = getGenerator(target);
        PolicyResult policyResult = resolvePolicies(contract, sourceBinding, targetBinding, source, target);
        Policy sourcePolicy = policyResult.getSourcePolicy();
        Policy targetPolicy = policyResult.getTargetPolicy();
        Set<PhysicalOperationDefinition> callbackOperations = generateOperations(contract, policyResult, targetBinding);
        PhysicalWireSourceDefinition sourceDefinition =
                sourceGenerator.generateCallbackWireSource(source, contract, sourcePolicy);
        PhysicalWireTargetDefinition targetDefinition =
                targetGenerator.generateWireTarget(callbackService, target, targetPolicy);
        targetDefinition.setCallback(true);
        PhysicalWireDefinition wireDefinition =
                new PhysicalWireDefinition(sourceDefinition, targetDefinition, callbackOperations);
        wireDefinition.setOptimizable(false);
        
        return wireDefinition;
        
    }

    @SuppressWarnings("unchecked")

    public <C extends LogicalComponent<?>> PhysicalWireDefinition generateBoundServiceWire(LogicalService service,
                                                                                           LogicalBinding<?> binding,
                                                                                           C component,
                                                                                           URI callbackUri) throws GenerationException {

        // use the service contract from the binding's parent service if it is defined, otherwise default to the one
        // defined on the original component
        Bindable bindable = binding.getParent();
        assert bindable instanceof LogicalService;
        LogicalService logicalService = (LogicalService) bindable;

        ServiceContract<?> contract = logicalService.getDefinition().getServiceContract();
        if (contract == null) {
            contract = service.getDefinition().getServiceContract();
        }

        LogicalBinding<SCABindingDefinition> targetBinding = new LogicalBinding<SCABindingDefinition>(SCABindingDefinition.INSTANCE, service);

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

        ComponentGenerator<C> targetGenerator = getGenerator(component);
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateWireTarget(targetService, component, targetPolicy);
        targetDefinition.setCallbackUri(callbackUri);
        BindingGenerator sourceGenerator = getGenerator(binding);
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(binding, sourcePolicy, service.getDefinition());

        Set<PhysicalOperationDefinition> operations = generateOperations(contract, policyResult, binding);
        PhysicalWireDefinition wireDefinition = new PhysicalWireDefinition(sourceDefinition, targetDefinition, operations);

        return wireDefinition;

    }

    @SuppressWarnings("unchecked")
    public <C extends LogicalComponent<?>> PhysicalWireDefinition generateBoundReferenceWire(C component,
                                                                           LogicalReference reference,
                                                                           LogicalBinding<?> binding) throws GenerationException {

        ReferenceDefinition referenceDefinition = reference.getDefinition();
        ServiceContract<?> contract = referenceDefinition.getServiceContract();
        LogicalBinding<SCABindingDefinition> sourceBinding = new LogicalBinding<SCABindingDefinition>(SCABindingDefinition.INSTANCE, reference);

        PolicyResult policyResult = resolvePolicies(contract, sourceBinding, binding, component, null);
        Policy sourcePolicy = policyResult.getSourcePolicy();
        Policy targetPolicy = policyResult.getTargetPolicy();

        BindingGenerator targetGenerator = getGenerator(binding);
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateWireTarget(binding, targetPolicy, reference.getDefinition());
        ServiceContract<?> callbackContract = contract.getCallbackContract();
        if (callbackContract != null) {
            // if there is a callback wire associated with this forward wire, calculate its URI
            URI callbackUri = generateCallbackUri(component, callbackContract, referenceDefinition.getName());
            targetDefinition.setCallbackUri(callbackUri);
        }


        ComponentGenerator<C> sourceGenerator = getGenerator(component);

        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(component, reference, sourcePolicy);

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

        LogicalBinding<SCABindingDefinition> sourceBinding = new LogicalBinding<SCABindingDefinition>(SCABindingDefinition.INSTANCE, reference);

        PolicyResult policyResult = resolvePolicies(contract, sourceBinding, binding, component, null);
        Policy sourcePolicy = policyResult.getSourcePolicy();
        Policy targetPolicy = policyResult.getTargetPolicy();
        BindingGenerator bindingGenerator = getGenerator(binding);
        ComponentGenerator<C> componentGenerator = getGenerator(component);

        PhysicalWireSourceDefinition sourceDefinition = bindingGenerator.generateWireSource(binding, targetPolicy, serviceDefinition);
        PhysicalWireTargetDefinition targetDefinition = componentGenerator.generateWireTarget(callbackService, component, sourcePolicy);
        targetDefinition.setCallback(true);
        Set<PhysicalOperationDefinition> operation = generateOperations(callbackContract, policyResult, binding);
        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, operation);

    }

    @SuppressWarnings({"unchecked"})
    public <C extends LogicalComponent<?>> PhysicalWireDefinition generateBoundCallbackServiceWire(C component, LogicalService service,
                                                                                 LogicalBinding<?> binding) throws GenerationException {

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
        LogicalBinding<SCABindingDefinition> targetBinding = new LogicalBinding<SCABindingDefinition>(SCABindingDefinition.INSTANCE, service);
        PolicyResult policyResult = resolvePolicies(callbackContract, binding, targetBinding, null, component);
        Policy targetPolicy = policyResult.getSourcePolicy();
        Policy sourcePolicy = policyResult.getTargetPolicy();

        ComponentGenerator<C> componentGenerator = getGenerator(component);
        PhysicalWireSourceDefinition sourceDefinition = componentGenerator.generateCallbackWireSource(component, callbackContract, sourcePolicy);

        BindingGenerator bindingGenerator = getGenerator(binding);
        // xcv FIXME refactor null param to use ServiceContract
        PhysicalWireTargetDefinition targetDefinition = bindingGenerator.generateWireTarget(binding, targetPolicy, null);

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
                Set<PolicySet> policies = policyResult.getInterceptedPolicySets(operation);
                Set<PhysicalInterceptorDefinition> interceptors = generateInterceptorDefinitions(policies, operation, logicalBinding);
                physicalOperation.setInterceptors(interceptors);
            }
            physicalOperations.add(physicalOperation);
        }

        return physicalOperations;

    }

    @SuppressWarnings("unchecked")
    private Set<PhysicalInterceptorDefinition> generateInterceptorDefinitions(Set<PolicySet> policies,
                                                                              Operation<?> operation,
                                                                              LogicalBinding<?> logicalBinding) throws GenerationException {

        if (policies == null) {
            return Collections.EMPTY_SET;
        }

        Set<PhysicalInterceptorDefinition> interceptors = new HashSet<PhysicalInterceptorDefinition>();
        for (PolicySet policy : policies) {
            QName qName = policy.getExtensionName();
            InterceptorDefinitionGenerator idg = generatorRegistry.getInterceptorDefinitionGenerator(qName);
            PhysicalInterceptorDefinition pid = idg.generate(policy.getExtension(), operation, logicalBinding);
            interceptors.add(pid);
        }
        return interceptors;

    }

    private PolicyResult resolvePolicies(ServiceContract<?> serviceContract,
                                         LogicalBinding<?> sourceBinding,
                                         LogicalBinding<?> targetBinding,
                                         LogicalComponent<?> source,
                                         LogicalComponent<?> target) throws PolicyException {

        try {
            return policyResolver.resolvePolicies(serviceContract, sourceBinding, targetBinding, source, target);
        } catch (PolicyResolutionException e) {
            throw new PolicyException(e);
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
            throw new CallbackServiceNotFoundException("Callback service not found ["
                    + name + "] on component [" + uri + "] originating from reference [" + sourceName + "]", name);
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
        return (BindingGenerator<?, ?, T>) generatorRegistry.getBindingGenerator(binding.getBinding().getClass());
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
