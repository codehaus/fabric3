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

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.generator.PolicyException;
import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ResourceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
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
import org.fabric3.spi.policy.PolicyResolutionException;
import org.fabric3.spi.policy.PolicyResolver;
import org.fabric3.spi.policy.PolicyResult;

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

    public <C extends LogicalComponent<?>> void generateResourceWire(C source,
                                                                     LogicalResource<?> resource,
                                                                     GeneratorContext context)
            throws GenerationException {

        ResourceDefinition resourceDefinition = resource.getResourceDefinition();
        ServiceContract<?> serviceContract = resourceDefinition.getServiceContract();

        // Generates the source side of the wire
        ComponentGenerator<C> sourceGenerator = getGenerator(source);
        PhysicalWireSourceDefinition pwsd = sourceGenerator.generateResourceWireSource(source, resource, context);

        // Generates the target side of the wire
        ResourceWireGenerator targetGenerator = getGenerator(resourceDefinition);
        @SuppressWarnings("unchecked")
        PhysicalWireTargetDefinition pwtd = targetGenerator.generateWireTargetDefinition(resource, context);
        pwsd.setOptimizable(pwtd.getUri() != null);

        // Create the wire from the component to the resource
        PhysicalWireDefinition wireDefinition = new PhysicalWireDefinition(pwsd, pwtd);
        List<? extends Operation<?>> operations = serviceContract.getOperations();
        for (Operation operation : operations) {
            PhysicalOperationDefinition physicalOperation = physicalOperationHelper.mapOperation(operation);
            wireDefinition.addOperation(physicalOperation);
        }

        context.getPhysicalChangeSet().addWireDefinition(wireDefinition);

    }

    public <S extends LogicalComponent<?>, T extends LogicalComponent<?>> void generateUnboundWire(S source,
                                                                                                   LogicalReference reference,
                                                                                                   LogicalService service,
                                                                                                   T target,
                                                                                                   GeneratorContext context)
            throws GenerationException {

        ReferenceDefinition referenceDefinition = reference.getDefinition();
        ServiceContract<?> serviceContract = referenceDefinition.getServiceContract();

        LogicalBinding<SCABindingDefinition> sourceBinding =
                new LogicalBinding<SCABindingDefinition>(SCABindingDefinition.INSTANCE, reference);
        LogicalBinding<SCABindingDefinition> targetBinding =
                new LogicalBinding<SCABindingDefinition>(SCABindingDefinition.INSTANCE, service);


        PolicyResult result = resolvePolicies(serviceContract, sourceBinding, targetBinding, source, target);

        PhysicalWireDefinition wireDefinition = new PhysicalWireDefinition();
        List<? extends Operation<?>> operations = serviceContract.getOperations();
        for (Operation operation : operations) {
            setOperationDefinition(operation, wireDefinition, result.getInterceptedPolicies().get(operation));
        }

        ComponentGenerator<T> targetGenerator = getGenerator(target);

        PhysicalWireTargetDefinition targetDefinition =
                targetGenerator.generateWireTarget(service, target, result.getTargetIntents(), result.getTargetPolicies(), context);

        wireDefinition.setTarget(targetDefinition);

        ComponentGenerator<S> sourceGenerator = getGenerator(source);

        // determine if it is optimizable
        boolean optimizable = !serviceContract.isConversational() && !serviceContract.isRemotable();
        if (optimizable) {
            for (PhysicalOperationDefinition operation : wireDefinition.getOperations()) {
                if (!operation.getInterceptors().isEmpty()) {
                    optimizable = false;
                    break;
                }
            }
        }

        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(source,
                                                                                           reference,
                                                                                           optimizable,
                                                                                           result.getSourceIntents(),
                                                                                           result.getSourcePolicies(),
                                                                                           context);
        sourceDefinition.setKey(target.getDefinition().getKey());
        wireDefinition.setSource(sourceDefinition);

        setCallbackOperationDefinitions(serviceContract, wireDefinition);

        context.getPhysicalChangeSet().addWireDefinition(wireDefinition);

    }

    public <C extends LogicalComponent<?>> void generateBoundServiceWire(LogicalService service,
                                                                         LogicalBinding<?> binding,
                                                                         C component,
                                                                         GeneratorContext context)
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

        LogicalBinding<SCABindingDefinition> targetBinding =
                new LogicalBinding<SCABindingDefinition>(SCABindingDefinition.INSTANCE, service);

        PolicyResult result = resolvePolicies(contract, binding, targetBinding, null, component);


        URI targetUri = service.getPromote();
        LogicalService targetService;
        if (targetUri == null) {
            // the service is on the component
            targetService = service;
        } else {
            // service is defined on a composite and wired to a component service
            targetService = component.getService(targetUri.getFragment());
        }

        ComponentGenerator<C> targetGenerator = getGenerator(component);
        PhysicalWireTargetDefinition targetDefinition =
                targetGenerator.generateWireTarget(targetService, component, result.getTargetIntents(), result.getTargetPolicies(), context);

        BindingGenerator sourceGenerator = getGenerator(binding);
        @SuppressWarnings("unchecked")
        PhysicalWireSourceDefinition sourceDefinition =
                sourceGenerator.generateWireSource(binding, result.getSourceIntents(), result.getSourcePolicies(), context, service.getDefinition());

        PhysicalWireDefinition wireDefinition = new PhysicalWireDefinition(sourceDefinition, targetDefinition);
        List<? extends Operation<?>> operations = contract.getOperations();
        for (Operation operation : operations) {
            setOperationDefinition(operation, wireDefinition, result.getInterceptedPolicies().get(operation));
        }
        setCallbackOperationDefinitions(contract, wireDefinition);

        context.getPhysicalChangeSet().addWireDefinition(wireDefinition);

    }

    public <C extends LogicalComponent<?>> void generateBoundReferenceWire(C component,
                                                                           LogicalReference reference,
                                                                           LogicalBinding<?> binding,
                                                                           GeneratorContext context)
            throws GenerationException {

        ServiceContract<?> contract = reference.getDefinition().getServiceContract();
        LogicalBinding<SCABindingDefinition> sourceBinding =
                new LogicalBinding<SCABindingDefinition>(SCABindingDefinition.INSTANCE, reference);
        PolicyResult result = resolvePolicies(contract, sourceBinding, binding, component, null);


        BindingGenerator targetGenerator = getGenerator(binding);
        @SuppressWarnings("unchecked")
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateWireTarget(binding,
                                                                                           result.getTargetIntents(),
                                                                                           result.getTargetPolicies(),
                                                                                           context,
                                                                                           reference.getDefinition());
        ComponentGenerator<C> sourceGenerator = getGenerator(component);
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(component,
                                                                                           reference,
                                                                                           false,
                                                                                           result.getSourceIntents(),
                                                                                           result.getSourcePolicies(),
                                                                                           context);
        PhysicalWireDefinition wireDefinition = new PhysicalWireDefinition(sourceDefinition, targetDefinition);
        List<? extends Operation<?>> operations = contract.getOperations();
        for (Operation operation : operations) {
            setOperationDefinition(operation, wireDefinition, result.getInterceptedPolicies().get(operation));
        }
        setCallbackOperationDefinitions(contract, wireDefinition);

        context.getPhysicalChangeSet().addWireDefinition(wireDefinition);

    }

    private void setOperationDefinition(Operation<?> operation, PhysicalWireDefinition wireDefinition, Set<PolicySet> policies)
            throws GenerationException {

        PhysicalOperationDefinition physicalOperation = physicalOperationHelper.mapOperation(operation);
        wireDefinition.addOperation(physicalOperation);
        for (PhysicalInterceptorDefinition interceptorDefinition : generateInterceptorDefinitions(policies)) {
            physicalOperation.addInterceptor(interceptorDefinition);
        }

    }

    @SuppressWarnings({"unchecked"})
    private void setCallbackOperationDefinitions(ServiceContract<?> contract, PhysicalWireDefinition wireDefinition) {

        for (Operation o : contract.getCallbackOperations()) {
            PhysicalOperationDefinition physicalOperation = physicalOperationHelper.mapOperation(o);
            physicalOperation.setCallback(true);
            wireDefinition.addOperation(physicalOperation);
        }

    }

    @SuppressWarnings("unchecked")
    private Set<PhysicalInterceptorDefinition> generateInterceptorDefinitions(Set<PolicySet> policies) throws GenerationException {

        if (policies == null) {
            return Collections.EMPTY_SET;
        }

        Set<PhysicalInterceptorDefinition> interceptors = new HashSet<PhysicalInterceptorDefinition>();
        for (PolicySet policy : policies) {
            QName qName = policy.getExtensionName();
            InterceptorDefinitionGenerator interceptorDefinitionGenerator =
                    generatorRegistry.getInterceptorDefinitionGenerator(qName);
            interceptors.add(interceptorDefinitionGenerator.generate(policy.getExtension(), null));
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
}
