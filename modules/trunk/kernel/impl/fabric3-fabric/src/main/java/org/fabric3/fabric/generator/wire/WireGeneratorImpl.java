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
package org.fabric3.fabric.generator.wire;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.generator.GeneratorRegistry;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ResourceDefinition;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorNotFoundException;
import org.fabric3.spi.generator.InterceptorGenerator;
import org.fabric3.spi.generator.ResourceWireGenerator;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
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
import org.fabric3.spi.policy.PolicyMetadata;
import org.fabric3.spi.policy.PolicyResolutionException;
import org.fabric3.spi.policy.PolicyResolver;
import org.fabric3.spi.policy.PolicyResult;

/**
 * Default implementation of WireGenerator.
 *
 * @version $Rev$ $Date$
 */
public class WireGeneratorImpl implements WireGenerator {

    private final GeneratorRegistry generatorRegistry;
    private final PolicyResolver policyResolver;
    private final PhysicalOperationMapper mapper;

    /**
     * Constructor.
     *
     * @param generatorRegistry the generator registry.
     * @param policyResolver    the policy resolver
     * @param mapper            the physical operation helper
     */
    public WireGeneratorImpl(@Reference GeneratorRegistry generatorRegistry,
                             @Reference PolicyResolver policyResolver,
                             @Reference PhysicalOperationMapper mapper) {
        this.generatorRegistry = generatorRegistry;
        this.policyResolver = policyResolver;
        this.mapper = mapper;
    }

    @SuppressWarnings("unchecked")
    public PhysicalWireDefinition generateResourceWire(LogicalResource<?> resource) throws GenerationException {

        ResourceDefinition resourceDefinition = resource.getResourceDefinition();
        LogicalComponent<?> component = resource.getParent();
        // Generates the source side of the wire
        ComponentGenerator sourceGenerator = getGenerator(component);
        PhysicalWireSourceDefinition pwsd = sourceGenerator.generateResourceWireSource(resource);
        pwsd.setClassLoaderId(component.getDefinition().getContributionUri());
        // Generates the target side of the wire
        ResourceWireGenerator targetGenerator = getGenerator(resourceDefinition);
        PhysicalWireTargetDefinition pwtd = targetGenerator.generateWireTarget(resource);
        pwtd.setClassLoaderId(resource.getParent().getDefinition().getContributionUri());
        boolean optimizable = pwtd.isOptimizable();

        // Create the wire from the component to the resource
        Set<PhysicalOperationDefinition> operations = generateOperations(resource.getOperations(), null, true);
        PhysicalWireDefinition wireDefinition = new PhysicalWireDefinition(pwsd, pwtd, operations);
        wireDefinition.setOptimizable(optimizable);

        return wireDefinition;
    }

    @SuppressWarnings("unchecked")
    public PhysicalWireDefinition generateCollocatedWire(LogicalReference reference, LogicalService service) throws GenerationException {
        LogicalComponent source = reference.getParent();
        LogicalComponent target = service.getParent();
        ReferenceDefinition referenceDefinition = reference.getDefinition();
        ServiceContract<?> serviceContract = referenceDefinition.getServiceContract();

        LogicalBinding<LocalBindingDefinition> sourceBinding = new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, reference);
        LogicalBinding<LocalBindingDefinition> targetBinding = new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, service);

        PolicyResult policyResult = resolvePolicies(reference.getOperations(), sourceBinding, targetBinding, source, target);
        Policy sourcePolicy = policyResult.getSourcePolicy();
        Policy targetPolicy = policyResult.getTargetPolicy();

        ComponentGenerator targetGenerator = getGenerator(target);
        // generate metadata for the target side of the wire
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateWireTarget(service, targetPolicy);
        targetDefinition.setClassLoaderId(target.getDefinition().getContributionUri());
        ServiceContract<?> callbackContract = reference.getDefinition().getServiceContract().getCallbackContract();
        if (callbackContract != null) {
            // if there is a callback wire associated with this forward wire, calculate its URI
            URI callbackUri = generateCallbackUri(source, callbackContract, referenceDefinition.getName());
            targetDefinition.setCallbackUri(callbackUri);
        }

        ComponentGenerator sourceGenerator = getGenerator(source);
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(reference, sourcePolicy);
        sourceDefinition.setClassLoaderId(source.getDefinition().getContributionUri());
        sourceDefinition.setKey(target.getDefinition().getKey());

        Set<PhysicalOperationDefinition> operations = generateOperations(reference.getOperations(), policyResult, true);
        QName sourceDeployable = null;
        QName targetDeployable = null;
        if (LogicalState.NEW == target.getState()) {
            sourceDeployable = source.getDeployable();
            targetDeployable = target.getDeployable();
        }

        PhysicalWireDefinition wireDefinition =
                new PhysicalWireDefinition(sourceDefinition, sourceDeployable, targetDefinition, targetDeployable, operations);
        boolean optimizable = sourceDefinition.isOptimizable() && targetDefinition.isOptimizable() && checkOptimization(serviceContract, operations);
        wireDefinition.setOptimizable(optimizable);
        return wireDefinition;
    }

    public PhysicalWireDefinition generateCollocatedCallbackWire(LogicalService service, LogicalReference reference) throws GenerationException {
        LogicalComponent<?> targetComponent = reference.getParent();
        ServiceContract<?> contract = reference.getDefinition().getServiceContract().getCallbackContract();
        LogicalService callbackService = targetComponent.getService(contract.getInterfaceName());
        LogicalBinding<LocalBindingDefinition> sourceBinding =
                new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, callbackService);
        LogicalBinding<LocalBindingDefinition> targetBinding = new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, reference);
        LogicalComponent sourceComponent = service.getParent();
        ComponentGenerator sourceGenerator = getGenerator(sourceComponent);
        ComponentGenerator targetGenerator = getGenerator(targetComponent);
        PolicyResult policyResult = resolvePolicies(service.getCallbackOperations(), sourceBinding, targetBinding, sourceComponent, targetComponent);
        Policy sourcePolicy = policyResult.getSourcePolicy();
        Policy targetPolicy = policyResult.getTargetPolicy();
        Set<PhysicalOperationDefinition> callbackOperations = generateOperations(reference.getCallbackOperations(), policyResult, true);
        PhysicalWireSourceDefinition sourceDefinition =
                sourceGenerator.generateCallbackWireSource(sourceComponent, contract, sourcePolicy);
        sourceDefinition.setClassLoaderId(sourceComponent.getDefinition().getContributionUri());
        PhysicalWireTargetDefinition targetDefinition =
                targetGenerator.generateWireTarget(callbackService, targetPolicy);
        targetDefinition.setClassLoaderId(targetComponent.getDefinition().getContributionUri());
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

        PolicyResult policyResult = resolvePolicies(logicalService.getOperations(), binding, targetBinding, null, component);
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
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateWireTarget(targetService, targetPolicy);
        targetDefinition.setClassLoaderId(targetService.getParent().getDefinition().getContributionUri());
        targetDefinition.setCallbackUri(callbackUri);
        BindingGenerator sourceGenerator = getGenerator(binding);
        List<LogicalOperation> logicalOperations = logicalService.getOperations();
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(binding, contract, logicalOperations, sourcePolicy);
        sourceDefinition.setClassLoaderId(binding.getParent().getParent().getDefinition().getContributionUri());

        Set<PhysicalOperationDefinition> operations = generateOperations(logicalOperations, policyResult, false);
        PhysicalWireDefinition pwd = new PhysicalWireDefinition(sourceDefinition, targetDefinition, operations);
        boolean optimizable = sourceDefinition.isOptimizable() && targetDefinition.isOptimizable() && checkOptimization(contract, operations);
        pwd.setOptimizable(optimizable);
        return pwd;

    }

    @SuppressWarnings("unchecked")
    public PhysicalWireDefinition generateBoundReferenceWire(LogicalReference reference, LogicalBinding<?> binding) throws GenerationException {

        LogicalComponent component = reference.getParent();
        ReferenceDefinition referenceDefinition = reference.getDefinition();
        ServiceContract<?> contract = referenceDefinition.getServiceContract();
        LogicalBinding<LocalBindingDefinition> sourceBinding = new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, reference);

        PolicyResult policyResult = resolvePolicies(reference.getOperations(), sourceBinding, binding, component, null);
        Policy sourcePolicy = policyResult.getSourcePolicy();
        Policy targetPolicy = policyResult.getTargetPolicy();

        BindingGenerator targetGenerator = getGenerator(binding);
        List<LogicalOperation> operations = reference.getOperations();
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateWireTarget(binding, contract, operations, targetPolicy);
        ServiceContract<?> callbackContract = contract.getCallbackContract();
        if (callbackContract != null) {
            // if there is a callback wire associated with this forward wire, calculate its URI
            URI callbackUri = generateCallbackUri(component, callbackContract, referenceDefinition.getName());
            targetDefinition.setCallbackUri(callbackUri);
        }
        targetDefinition.setKey(binding.getDefinition().getKey());
        targetDefinition.setClassLoaderId(binding.getParent().getParent().getDefinition().getContributionUri());


        ComponentGenerator sourceGenerator = getGenerator(component);

        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(reference, sourcePolicy);
        sourceDefinition.setClassLoaderId(component.getDefinition().getContributionUri());

        Set<PhysicalOperationDefinition> operation = generateOperations(operations, policyResult, false);

        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, operation);

    }

    @SuppressWarnings({"unchecked"})
    public PhysicalWireDefinition generateBoundCallbackReferenceWire(LogicalReference reference, LogicalBinding<?> binding)
            throws GenerationException {
        LogicalComponent<?> component = reference.getParent();
        ReferenceDefinition definition = reference.getDefinition();
        ServiceContract<?> contract = definition.getServiceContract();
        ServiceContract<?> callbackContract = contract.getCallbackContract();

        LogicalService callbackService = component.getService(callbackContract.getInterfaceName());

        LogicalBinding<LocalBindingDefinition> sourceBinding = new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, reference);

        List<LogicalOperation> logicalOperations = reference.getCallbackOperations();
        PolicyResult policyResult = resolvePolicies(logicalOperations, sourceBinding, binding, component, null);
        Policy sourcePolicy = policyResult.getSourcePolicy();
        Policy targetPolicy = policyResult.getTargetPolicy();
        BindingGenerator bindingGenerator = getGenerator(binding);
        ComponentGenerator componentGenerator = getGenerator(component);

        PhysicalWireSourceDefinition sourceDefinition =
                bindingGenerator.generateWireSource(binding, callbackContract, logicalOperations, targetPolicy);
        sourceDefinition.setClassLoaderId(binding.getParent().getParent().getDefinition().getContributionUri());
        PhysicalWireTargetDefinition targetDefinition = componentGenerator.generateWireTarget(callbackService, sourcePolicy);
        targetDefinition.setClassLoaderId(callbackService.getParent().getDefinition().getContributionUri());
        targetDefinition.setCallback(true);
        Set<PhysicalOperationDefinition> operation = generateOperations(logicalOperations, policyResult, false);
        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, operation);

    }

    @SuppressWarnings({"unchecked"})
    public PhysicalWireDefinition generateBoundCallbackServiceWire(LogicalService service, LogicalBinding<?> binding) throws GenerationException {
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
        ServiceContract<?> callbackContract = contract.getCallbackContract();

        LogicalBinding<LocalBindingDefinition> sourceBinding = new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, service);
        List<LogicalOperation> callbackOperations = service.getCallbackOperations();
        PolicyResult policyResult = resolvePolicies(callbackOperations, sourceBinding, binding, component, null);
        Policy targetPolicy = policyResult.getSourcePolicy();
        Policy sourcePolicy = policyResult.getTargetPolicy();

        ComponentGenerator componentGenerator = getGenerator(component);
        PhysicalWireSourceDefinition sourceDefinition = componentGenerator.generateCallbackWireSource(component, callbackContract, sourcePolicy);
        sourceDefinition.setClassLoaderId(component.getDefinition().getContributionUri());

        BindingGenerator bindingGenerator = getGenerator(binding);
        PhysicalWireTargetDefinition targetDefinition =
                bindingGenerator.generateWireTarget(binding, callbackContract, callbackOperations, targetPolicy);
        targetDefinition.setClassLoaderId(binding.getParent().getParent().getDefinition().getContributionUri());

        Set<PhysicalOperationDefinition> operations = generateOperations(service.getCallbackOperations(), policyResult, false);
        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, operations);

    }

    private Set<PhysicalOperationDefinition> generateOperations(List<LogicalOperation> operations, PolicyResult policyResult, boolean collocated)
            throws GenerationException {

        Set<PhysicalOperationDefinition> physicalOperations = new HashSet<PhysicalOperationDefinition>(operations.size());

        for (LogicalOperation operation : operations) {
            PhysicalOperationDefinition physicalOperation = mapper.map(operation.getDefinition());
            if (policyResult != null) {
                List<PolicySet> policies = policyResult.getInterceptedPolicySets(operation);
                PolicyMetadata metadata = policyResult.getMetadata();
                Set<PhysicalInterceptorDefinition> interceptors = generateInterceptorDefinitions(policies, operation, collocated, metadata);
                physicalOperation.setInterceptors(interceptors);
            }
            physicalOperations.add(physicalOperation);
        }

        return physicalOperations;

    }

    @SuppressWarnings("unchecked")
    private Set<PhysicalInterceptorDefinition> generateInterceptorDefinitions(List<PolicySet> policies,
                                                                              LogicalOperation operation,
                                                                              boolean collocated,
                                                                              PolicyMetadata metadata) throws GenerationException {

        if (policies == null) {
            return Collections.EMPTY_SET;
        }

        Set<PhysicalInterceptorDefinition> interceptors = new LinkedHashSet<PhysicalInterceptorDefinition>();
        for (PolicySet policy : policies) {
            QName qName = policy.getExtensionName();
            InterceptorGenerator generator = generatorRegistry.getInterceptorDefinitionGenerator(qName);
            PhysicalInterceptorDefinition pid = generator.generate(policy.getExtension(), metadata, operation, collocated);
            if (pid != null) {
                URI contributionClassLoaderId = operation.getParent().getParent().getDefinition().getContributionUri();
                pid.setWireClassLoaderId(contributionClassLoaderId);
                pid.setPolicyClassLoaderid(policy.getContributionUri());
                interceptors.add(pid);
            }
        }
        return interceptors;
    }

    private PolicyResult resolvePolicies(List<LogicalOperation> operations,
                                         LogicalBinding<?> sourceBinding,
                                         LogicalBinding<?> targetBinding,
                                         LogicalComponent<?> source,
                                         LogicalComponent<?> target) throws PolicyGenerationException {
        try {
            return policyResolver.resolvePolicies(operations, sourceBinding, targetBinding, source, target);
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
    private <T extends ResourceDefinition> ResourceWireGenerator<T> getGenerator(T definition) throws GeneratorNotFoundException {
        return (ResourceWireGenerator<T>) generatorRegistry.getResourceWireGenerator(definition.getClass());
    }

    @SuppressWarnings("unchecked")
    private <T extends BindingDefinition> BindingGenerator<T> getGenerator(LogicalBinding<T> binding) throws GeneratorNotFoundException {
        return (BindingGenerator<T>) generatorRegistry.getBindingGenerator(binding.getDefinition().getClass());
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
