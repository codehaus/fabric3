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
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.generator.GeneratorRegistry;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ResourceDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorNotFoundException;
import org.fabric3.spi.generator.ResourceGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.type.binding.LocalBindingDefinition;
import org.fabric3.spi.model.type.binding.RemoteBindingDefinition;
import org.fabric3.spi.policy.EffectivePolicy;
import org.fabric3.spi.policy.PolicyResolutionException;
import org.fabric3.spi.policy.PolicyResolver;
import org.fabric3.spi.policy.PolicyResult;

/**
 * Default implementation of WireGenerator.
 *
 * @version $Rev$ $Date$
 */
public class WireGeneratorImpl implements WireGenerator {
    private GeneratorRegistry generatorRegistry;
    private ContractMatcher matcher;
    private PolicyResolver policyResolver;
    private PhysicalOperationGenerator operationGenerator;

    public WireGeneratorImpl(@Reference GeneratorRegistry generatorRegistry,
                             @Reference ContractMatcher matcher,
                             @Reference PolicyResolver policyResolver,
                             @Reference PhysicalOperationGenerator operationGenerator) {
        this.generatorRegistry = generatorRegistry;
        this.matcher = matcher;
        this.policyResolver = policyResolver;
        this.operationGenerator = operationGenerator;
    }

    public <T extends BindingDefinition> PhysicalWireDefinition generateBoundService(LogicalBinding<T> binding, URI callbackUri)
            throws GenerationException {
        if (!(binding.getParent() instanceof LogicalService)) {
            throw new AssertionError("Expected " + LogicalService.class.getName() + " as parent to binding");
        }
        LogicalService service = (LogicalService) binding.getParent();
        LogicalComponent<?> component = service.getLeafComponent();
        ServiceContract contract = service.getServiceContract();

        LogicalBinding<RemoteBindingDefinition> targetBinding =
                new LogicalBinding<RemoteBindingDefinition>(RemoteBindingDefinition.INSTANCE, service);

        PolicyResult policyResult = resolvePolicies(service.getOperations(), binding, targetBinding, null, component);
        EffectivePolicy sourcePolicy = policyResult.getSourcePolicy();
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        ComponentGenerator targetGenerator = getGenerator(component);
        PhysicalTargetDefinition targetDefinition = targetGenerator.generateTarget(service, targetPolicy);
        targetDefinition.setClassLoaderId(service.getParent().getDefinition().getContributionUri());
        targetDefinition.setCallbackUri(callbackUri);
        BindingGenerator<T> sourceGenerator = getGenerator(binding);
        List<LogicalOperation> logicalOperations = service.getOperations();
        PhysicalSourceDefinition sourceDefinition = sourceGenerator.generateSource(binding, contract, logicalOperations, sourcePolicy);
        sourceDefinition.setClassLoaderId(service.getParent().getDefinition().getContributionUri());

        Set<PhysicalOperationDefinition> operations = operationGenerator.generateOperations(logicalOperations, policyResult);
        PhysicalWireDefinition pwd = new PhysicalWireDefinition(sourceDefinition, targetDefinition, operations);
        boolean optimizable = sourceDefinition.isOptimizable() && targetDefinition.isOptimizable() && checkOptimization(contract, operations);
        pwd.setOptimizable(optimizable);
        return pwd;
    }

    public <T extends BindingDefinition> PhysicalWireDefinition generateBoundServiceCallback(LogicalBinding<T> binding) throws GenerationException {
        if (!(binding.getParent() instanceof LogicalService)) {
            throw new AssertionError("Expected " + LogicalService.class.getName() + " as parent to binding");
        }

        LogicalService service = (LogicalService) binding.getParent();
        LogicalComponent<?> component = service.getLeafComponent();

        ServiceContract contract = service.getServiceContract();
        ServiceContract callbackContract = contract.getCallbackContract();

        LogicalBinding<RemoteBindingDefinition> sourceBinding =
                new LogicalBinding<RemoteBindingDefinition>(RemoteBindingDefinition.INSTANCE, service);
        List<LogicalOperation> callbackOperations = service.getCallbackOperations();
        PolicyResult policyResult = resolvePolicies(callbackOperations, sourceBinding, binding, component, null);
        EffectivePolicy sourcePolicy = policyResult.getSourcePolicy();
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        ComponentGenerator componentGenerator = getGenerator(component);
        PhysicalSourceDefinition sourceDefinition = componentGenerator.generateCallbackSource(service, sourcePolicy);
        sourceDefinition.setClassLoaderId(component.getDefinition().getContributionUri());

        BindingGenerator<T> bindingGenerator = getGenerator(binding);
        PhysicalTargetDefinition targetDefinition =
                bindingGenerator.generateTarget(binding, callbackContract, callbackOperations, targetPolicy);
        targetDefinition.setClassLoaderId(binding.getParent().getParent().getDefinition().getContributionUri());

        Set<PhysicalOperationDefinition> operations = operationGenerator.generateOperations(service.getCallbackOperations(), policyResult);
        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, operations);
    }

    public <T extends BindingDefinition> PhysicalWireDefinition generateBoundReference(LogicalBinding<T> binding) throws GenerationException {
        if (!(binding.getParent() instanceof LogicalReference)) {
            throw new AssertionError("Expected " + LogicalReference.class.getName() + " as parent to binding");
        }
        LogicalReference reference = (LogicalReference) binding.getParent();
        LogicalComponent component = reference.getParent();
        ReferenceDefinition referenceDefinition = reference.getDefinition();
        ServiceContract contract = reference.getServiceContract();
        LogicalBinding<RemoteBindingDefinition> sourceBinding =
                new LogicalBinding<RemoteBindingDefinition>(RemoteBindingDefinition.INSTANCE, reference);

        PolicyResult policyResult = resolvePolicies(reference.getOperations(), sourceBinding, binding, component, null);
        EffectivePolicy sourcePolicy = policyResult.getSourcePolicy();
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        BindingGenerator<T> targetGenerator = getGenerator(binding);
        List<LogicalOperation> operations = reference.getOperations();
        PhysicalTargetDefinition targetDefinition = targetGenerator.generateTarget(binding, contract, operations, targetPolicy);
        ServiceContract callbackContract = contract.getCallbackContract();
        if (callbackContract != null) {
            // if there is a callback wire associated with this forward wire, calculate its URI
            URI callbackUri = generateCallbackUri(component, callbackContract, referenceDefinition.getName());
            targetDefinition.setCallbackUri(callbackUri);
        }
        targetDefinition.setClassLoaderId(binding.getParent().getParent().getDefinition().getContributionUri());


        ComponentGenerator sourceGenerator = getGenerator(component);

        PhysicalSourceDefinition sourceDefinition = sourceGenerator.generateSource(reference, sourcePolicy);
        sourceDefinition.setClassLoaderId(component.getDefinition().getContributionUri());

        Set<PhysicalOperationDefinition> operation = operationGenerator.generateOperations(operations, policyResult);

        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, operation);

    }

    public <T extends BindingDefinition> PhysicalWireDefinition generateBoundReferenceCallback(LogicalBinding<T> binding) throws GenerationException {
        if (!(binding.getParent() instanceof LogicalReference)) {
            throw new AssertionError("Expected " + LogicalReference.class.getName() + " as parent to binding");
        }
        LogicalReference reference = (LogicalReference) binding.getParent();

        LogicalComponent<?> component = reference.getParent();
        ServiceContract contract = reference.getServiceContract();
        ServiceContract callbackContract = contract.getCallbackContract();

        LogicalService callbackService = component.getService(callbackContract.getInterfaceName());

        LogicalBinding<RemoteBindingDefinition> sourceBinding =
                new LogicalBinding<RemoteBindingDefinition>(RemoteBindingDefinition.INSTANCE, reference);

        List<LogicalOperation> logicalOperations = reference.getCallbackOperations();
        PolicyResult policyResult = resolvePolicies(logicalOperations, sourceBinding, binding, component, null);
        EffectivePolicy sourcePolicy = policyResult.getSourcePolicy();
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();
        BindingGenerator<T> bindingGenerator = getGenerator(binding);
        ComponentGenerator componentGenerator = getGenerator(component);

        PhysicalSourceDefinition sourceDefinition =
                bindingGenerator.generateSource(binding, callbackContract, logicalOperations, targetPolicy);
        sourceDefinition.setClassLoaderId(binding.getParent().getParent().getDefinition().getContributionUri());
        PhysicalTargetDefinition targetDefinition = componentGenerator.generateTarget(callbackService, sourcePolicy);
        targetDefinition.setClassLoaderId(callbackService.getParent().getDefinition().getContributionUri());
        targetDefinition.setCallback(true);
        Set<PhysicalOperationDefinition> operation = operationGenerator.generateOperations(logicalOperations, policyResult);
        return new PhysicalWireDefinition(sourceDefinition, targetDefinition, operation);

    }

    public PhysicalWireDefinition generateWire(LogicalWire wire) throws GenerationException {
        LogicalReference reference = wire.getSource();
        LogicalService service = wire.getTarget();
        LogicalComponent source = reference.getParent();
        LogicalComponent target = service.getLeafComponent();
        ReferenceDefinition referenceDefinition = reference.getDefinition();
        ServiceContract referenceContract = reference.getServiceContract();

        LogicalBinding<LocalBindingDefinition> sourceBinding = new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, reference);
        LogicalBinding<LocalBindingDefinition> targetBinding = new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, service);

        PolicyResult policyResult = resolvePolicies(reference.getOperations(), sourceBinding, targetBinding, source, target);
        EffectivePolicy sourcePolicy = policyResult.getSourcePolicy();
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        ComponentGenerator targetGenerator = getGenerator(target);
        // generate metadata for the target side of the wire
        PhysicalTargetDefinition targetDefinition = targetGenerator.generateTarget(service, targetPolicy);
        targetDefinition.setClassLoaderId(target.getDefinition().getContributionUri());
        ServiceContract serviceContract = service.getServiceContract();
        ServiceContract callbackContract = serviceContract.getCallbackContract();
        if (callbackContract != null) {
            // if there is a callback wire associated with this forward wire, calculate its URI
            URI callbackUri = generateCallbackUri(source, callbackContract, referenceDefinition.getName());
            targetDefinition.setCallbackUri(callbackUri);
        }

        ComponentGenerator sourceGenerator = getGenerator(source);
        PhysicalSourceDefinition sourceDefinition = sourceGenerator.generateSource(reference, sourcePolicy);
        sourceDefinition.setClassLoaderId(source.getDefinition().getContributionUri());
        String key = target.getDefinition().getKey();
        sourceDefinition.setKey(key);

        Set<PhysicalOperationDefinition> operations;
        if (referenceContract.getClass().equals(serviceContract.getClass())) {
            operations = operationGenerator.generateOperations(reference.getOperations(), policyResult);
        } else {
            operations = operationGenerator.generateOperations(reference.getOperations(), service.getOperations(), policyResult);
        }
        QName sourceDeployable = null;
        QName targetDeployable = null;
        if (LogicalState.NEW == target.getState()) {
            sourceDeployable = source.getDeployable();
            targetDeployable = target.getDeployable();
        }

        PhysicalWireDefinition wireDefinition =
                new PhysicalWireDefinition(sourceDefinition, sourceDeployable, targetDefinition, targetDeployable, operations);
        boolean optimizable =
                sourceDefinition.isOptimizable() && targetDefinition.isOptimizable() && checkOptimization(referenceContract, operations);
        wireDefinition.setOptimizable(optimizable);
        return wireDefinition;
    }

    public PhysicalWireDefinition generateWireCallback(LogicalWire wire) throws GenerationException {
        LogicalReference reference = wire.getSource();
        LogicalService service = wire.getTarget();
        LogicalComponent<?> targetComponent = reference.getParent();
        ServiceContract referenceCallbackContract = reference.getServiceContract().getCallbackContract();
        // FIXME getInterfaceName
        LogicalService callbackTargetService = targetComponent.getService(referenceCallbackContract.getInterfaceName());
        LogicalBinding<LocalBindingDefinition> sourceBinding =
                new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, callbackTargetService);
        LogicalBinding<LocalBindingDefinition> targetBinding = new LogicalBinding<LocalBindingDefinition>(LocalBindingDefinition.INSTANCE, reference);
        LogicalComponent sourceComponent = service.getLeafComponent();
        ComponentGenerator sourceGenerator = getGenerator(sourceComponent);
        ComponentGenerator targetGenerator = getGenerator(targetComponent);
        PolicyResult policyResult = resolvePolicies(service.getCallbackOperations(), sourceBinding, targetBinding, sourceComponent, targetComponent);
        EffectivePolicy sourcePolicy = policyResult.getSourcePolicy();
        EffectivePolicy targetPolicy = policyResult.getTargetPolicy();

        Set<PhysicalOperationDefinition> callbackOperations;
        ServiceContract callbackContract = service.getServiceContract().getCallbackContract();
        if (referenceCallbackContract.getClass().equals(callbackContract.getClass())) {
            List<LogicalOperation> operations = callbackTargetService.getOperations();
            callbackOperations = operationGenerator.generateOperations(operations, policyResult);
        } else {
            List<LogicalOperation> targetOperations = callbackTargetService.getOperations();
            List<LogicalOperation> sourceOperations = service.getCallbackOperations();
            callbackOperations = operationGenerator.generateOperations(targetOperations, sourceOperations, policyResult);
        }

        PhysicalSourceDefinition sourceDefinition = sourceGenerator.generateCallbackSource(service, sourcePolicy);
        sourceDefinition.setClassLoaderId(sourceComponent.getDefinition().getContributionUri());
        PhysicalTargetDefinition targetDefinition = targetGenerator.generateTarget(callbackTargetService, targetPolicy);
        targetDefinition.setClassLoaderId(targetComponent.getDefinition().getContributionUri());
        targetDefinition.setCallback(true);
        PhysicalWireDefinition wireDefinition = new PhysicalWireDefinition(sourceDefinition, targetDefinition, callbackOperations);
        wireDefinition.setOptimizable(false);

        return wireDefinition;
    }

    public <T extends ResourceDefinition> PhysicalWireDefinition generateResource(LogicalResource<T> resource) throws GenerationException {
        T resourceDefinition = resource.getResourceDefinition();
        LogicalComponent<?> component = resource.getParent();

        // Generates the wire source metadata
        ComponentGenerator sourceGenerator = getGenerator(component);
        PhysicalSourceDefinition sourceDefinition = sourceGenerator.generateResourceSource(resource);
        sourceDefinition.setClassLoaderId(component.getDefinition().getContributionUri());

        // Generates the wire target metadata
        ResourceGenerator<T> targetGenerator = getGenerator(resourceDefinition);
        PhysicalTargetDefinition targetDefinition = targetGenerator.generateWireTarget(resource);
        targetDefinition.setClassLoaderId(resource.getParent().getDefinition().getContributionUri());
        boolean optimizable = targetDefinition.isOptimizable();

        // Create the wire from the component to the resource
        Set<PhysicalOperationDefinition> operations = operationGenerator.generateOperations(resource.getOperations(), null);
        PhysicalWireDefinition wireDefinition = new PhysicalWireDefinition(sourceDefinition, targetDefinition, operations);
        wireDefinition.setOptimizable(optimizable);

        return wireDefinition;
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

    private <S extends LogicalComponent<?>> URI generateCallbackUri(S source, ServiceContract contract, String sourceName)
            throws GenerationException {
        LogicalService candidate = null;
        for (LogicalService entry : source.getServices()) {
            MatchResult result = matcher.isAssignableFrom(contract, entry.getServiceContract(), false);
            if (result.isAssignable()) {
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
    private <T extends ResourceDefinition> ResourceGenerator<T> getGenerator(T definition) throws GeneratorNotFoundException {
        return (ResourceGenerator<T>) generatorRegistry.getResourceWireGenerator(definition.getClass());
    }

    @SuppressWarnings("unchecked")
    private <T extends BindingDefinition> BindingGenerator<T> getGenerator(LogicalBinding<T> binding) throws GeneratorNotFoundException {
        return (BindingGenerator<T>) generatorRegistry.getBindingGenerator(binding.getDefinition().getClass());
    }

    private boolean checkOptimization(ServiceContract serviceContract, Set<PhysicalOperationDefinition> operationDefinitions) {
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
