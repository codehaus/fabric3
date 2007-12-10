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
import java.util.List;

import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.ResourceWireGenerator;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.type.SCABindingDefinition;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class PhysicalWireGeneratorImpl implements PhysicalWireGenerator {

    private final GeneratorRegistry generatorRegistry;
    private final PhysicalPolicyGenerator physicalPolicyGenerator;
    private final PhysicalOperationHelper physicalOperationHelper;
    
    /**
     * Injects generator registry and assembly store.
     * 
     * @param generatorRegistry Generator registry.
     * @param assemblyStore Assembly store.
     */
    public PhysicalWireGeneratorImpl(@Reference GeneratorRegistry generatorRegistry,
                                     @Reference PhysicalPolicyGenerator physicalPolicyGenerator,
                                     @Reference PhysicalOperationHelper physicalOperationHelper) {
        this.generatorRegistry = generatorRegistry;
        this.physicalPolicyGenerator = physicalPolicyGenerator;
        this.physicalOperationHelper = physicalOperationHelper;
    }

    @SuppressWarnings("unchecked")
    public <C extends LogicalComponent<?>> void generateResourceWire(C source,
                                                                     LogicalResource<?> resource,
                                                                     GeneratorContext context)
            throws GenerationException {

        // Generates the source side of the wire
        
        ComponentGenerator<C> sourceGenerator = (ComponentGenerator<C>) 
            generatorRegistry.getComponentGenerator(source.getDefinition().getImplementation().getClass());
        PhysicalWireSourceDefinition pwsd = sourceGenerator.generateResourceWireSource(source, resource, context);

        // Generates the target side of the wire
        ResourceWireGenerator targetGenerator = 
            generatorRegistry.getResourceWireGenerator(resource.getResourceDefinition().getClass());
        PhysicalWireTargetDefinition pwtd = targetGenerator.genearteWireTargetDefinition(resource, context);
        pwsd.setOptimizable(pwtd.getUri() != null);

        // Create the wire from the component to the resource
        ServiceContract serviceContract = resource.getResourceDefinition().getServiceContract();

        PhysicalWireDefinition wireDefinition = new PhysicalWireDefinition();
        List<Operation<?>> operations = serviceContract.getOperations();
        for (Operation operation : operations) {
            PhysicalOperationDefinition physicalOperation = physicalOperationHelper.mapOperation(operation);
            wireDefinition.addOperation(physicalOperation);
        }

        wireDefinition.setSource(pwsd);
        wireDefinition.setTarget(pwtd);

        context.getPhysicalChangeSet().addWireDefinition(wireDefinition);

    }

    @SuppressWarnings({"unchecked"})
    public <S extends LogicalComponent<?>, T extends LogicalComponent<?>> void generateUnboundWire(S source,
                                                                                                   LogicalReference reference,
                                                                                                   LogicalService service,
                                                                                                   T target,
                                                                                                   GeneratorContext context)
            throws GenerationException {

        ReferenceDefinition referenceDefinition = reference.getDefinition();
        ServiceContract serviceContract = referenceDefinition.getServiceContract();
        
        LogicalBinding<SCABindingDefinition> sourceBinding = 
            new LogicalBinding<SCABindingDefinition>(SCABindingDefinition.INSTANCE, reference);
        LogicalBinding<SCABindingDefinition> targetBinding = 
            new LogicalBinding<SCABindingDefinition>(SCABindingDefinition.INSTANCE, service);
        
        PhysicalPolicyResult policyResult = physicalPolicyGenerator.generatePhysicalPolicies(serviceContract,
                                                                                             sourceBinding, 
                                                                                             targetBinding, 
                                                                                             source, 
                                                                                             target);
        PhysicalWireDefinition wireDefinition = policyResult.getPhysicalWireDefinition();

        ComponentGenerator<T> targetGenerator = (ComponentGenerator<T>) 
            generatorRegistry.getComponentGenerator(target.getDefinition().getImplementation().getClass());
        
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateWireTarget(service, 
                                                                                           target, 
                                                                                           policyResult.getIntentsProvidedByTarget(),
                                                                                           policyResult.getPolicySetsProvidedByTarget(),
                                                                                           context);
        wireDefinition.setTarget(targetDefinition);

        ComponentGenerator<S> sourceGenerator = (ComponentGenerator<S>) 
            generatorRegistry.getComponentGenerator(source.getDefinition().getImplementation().getClass());

        // determine if it is optimizable
        boolean optimizable = !serviceContract.isConversational() && !serviceContract.isRemotable();
        if (optimizable) {
            for (PhysicalOperationDefinition operation : policyResult.getPhysicalWireDefinition().getOperations()) {
                if (!operation.getInterceptors().isEmpty()) {
                    optimizable = false;
                    break;
                }
            }
        }

        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(source, 
                                                                                           reference, 
                                                                                           optimizable,
                                                                                           policyResult.getIntentsProvidedBySource(),
                                                                                           policyResult.getPolicySetsProvidedBySource(),
                                                                                           context);
        sourceDefinition.setKey(target.getDefinition().getKey());
        wireDefinition.setSource(sourceDefinition);

        context.getPhysicalChangeSet().addWireDefinition(wireDefinition);

    }

    @SuppressWarnings({"unchecked"})
    public <C extends LogicalComponent<?>> void generateBoundServiceWire(LogicalService service,
                                                                         LogicalBinding<?> binding,
                                                                         C component,
                                                                         GeneratorContext context)
            throws GenerationException {

        // use the service contract from the binding's parent service if it is defined, otherwise default to the one
        // defined on the original component
        ServiceContract contract;
        Bindable bindable = binding.getParent();
        assert bindable instanceof LogicalService;
        LogicalService logicalService = (LogicalService) bindable;
        
        ServiceContract<?> promotedContract = logicalService.getDefinition().getServiceContract();
        if (promotedContract == null) {
            contract = service.getDefinition().getServiceContract();
        } else {
            contract = promotedContract;
        }

        LogicalBinding<?> sourceBinding = binding;
        LogicalBinding<SCABindingDefinition> targetBinding = 
            new LogicalBinding<SCABindingDefinition>(SCABindingDefinition.INSTANCE, service);
        
        PhysicalPolicyResult policyResult = physicalPolicyGenerator.generatePhysicalPolicies(contract,
                                                                                             sourceBinding, 
                                                                                             targetBinding, 
                                                                                             null, 
                                                                                             component);
        PhysicalWireDefinition wireDefinition = policyResult.getPhysicalWireDefinition();
        
        ComponentGenerator<C> targetGenerator = (ComponentGenerator<C>) 
            generatorRegistry.getComponentGenerator(component.getDefinition().getImplementation().getClass());
        
        URI targetUri = service.getPromote();
        LogicalService targetService;
        if (targetUri == null) {
            // the service is on the component
            targetService = service;
        } else {
            // service is defined on a composite and wired to a component service
            targetService = component.getService(targetUri.getFragment());
        }
        
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateWireTarget(targetService, 
                                                                                           component, 
                                                                                           policyResult.getIntentsProvidedByTarget(),
                                                                                           policyResult.getPolicySetsProvidedByTarget(),
                                                                                           context);
        wireDefinition.setTarget(targetDefinition);
        
        BindingGenerator sourceGenerator = generatorRegistry.getBindingGenerator(binding.getBinding().getClass());

        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(binding,
                                                                                           policyResult.getIntentsProvidedBySource(),
                                                                                           policyResult.getPolicySetsProvidedBySource(),
                                                                                           context,
                                                                                           service.getDefinition());
        wireDefinition.setSource(sourceDefinition);
        context.getPhysicalChangeSet().addWireDefinition(wireDefinition);

    }

    @SuppressWarnings({"unchecked"})
    public <C extends LogicalComponent<?>> void generateBoundReferenceWire(C component,
                                                                           LogicalReference reference,
                                                                           LogicalBinding<?> binding,
                                                                           GeneratorContext context)
            throws GenerationException {

        ServiceContract contract = reference.getDefinition().getServiceContract();

        LogicalBinding<SCABindingDefinition> sourceBinding = 
            new LogicalBinding<SCABindingDefinition>(SCABindingDefinition.INSTANCE, reference);
        LogicalBinding<?> targetBinding = binding;
        
        PhysicalPolicyResult policyResult = physicalPolicyGenerator.generatePhysicalPolicies(contract,
                                                                                             sourceBinding, 
                                                                                             targetBinding, 
                                                                                             component, 
                                                                                             null);
        PhysicalWireDefinition wireDefinition = policyResult.getPhysicalWireDefinition();

        BindingGenerator targetGenerator = generatorRegistry.getBindingGenerator(binding.getBinding().getClass());
        
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateWireTarget(binding, 
                                                                                           policyResult.getIntentsProvidedByTarget(), 
                                                                                           policyResult.getPolicySetsProvidedByTarget(),
                                                                                           context, 
                                                                                           reference.getDefinition());
        wireDefinition.setTarget(targetDefinition);

        ComponentGenerator<C> sourceGenerator = (ComponentGenerator<C>) 
            generatorRegistry.getComponentGenerator(component.getDefinition().getImplementation().getClass());
        
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(component, 
                                                                                           reference, 
                                                                                           false, 
                                                                                           policyResult.getIntentsProvidedBySource(),
                                                                                           policyResult.getPolicySetsProvidedBySource(),
                                                                                           context);
        wireDefinition.setSource(sourceDefinition);

        context.getPhysicalChangeSet().addWireDefinition(wireDefinition);

    }

}
