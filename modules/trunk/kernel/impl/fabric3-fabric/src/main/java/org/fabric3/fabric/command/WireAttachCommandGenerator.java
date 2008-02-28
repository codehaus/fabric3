/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.fabric.command;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.fabric3.fabric.model.physical.PhysicalWireGenerator;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.runtime.assembly.LogicalComponentManager;
import org.fabric3.spi.util.UriHelper;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

/**
 *
 * @version $Revision$ $Date$
 */
public class WireAttachCommandGenerator implements CommandGenerator {

    private final PhysicalWireGenerator physicalWireGenerator;
    private final LogicalComponentManager logicalComponentManager;
    private final int order;

    public WireAttachCommandGenerator(@Reference PhysicalWireGenerator physicalWireGenerator,
                                      @Reference(name = "logicalComponentManager") LogicalComponentManager logicalComponentManager,
                                      @Property(name="order") int order) {
        this.physicalWireGenerator = physicalWireGenerator;
        this.logicalComponentManager = logicalComponentManager;
        this.order = order;
    }

    @SuppressWarnings("unchecked")
    public Set<Command> generate(LogicalComponent<?> component) throws GenerationException {
        
        Set<Command> commandSet = new LinkedHashSet<Command>();
        
        if (component instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent compositeComponent = (LogicalCompositeComponent) component;
            for (LogicalComponent<?> child : compositeComponent.getComponents()) {
                commandSet.addAll(generate(child));
            }
        } else {
            generatePhysicalWires(component, commandSet);
        }
        
        return commandSet;
    }

    @SuppressWarnings("unchecked")
    private void generatePhysicalWires(LogicalComponent<?> component, Set<Command> commandSet) throws GenerationException {

        generateReferenceWires(component, commandSet);
        generateServiceWires(component, commandSet);
        generateResourceWires(component, commandSet);

    }

    @SuppressWarnings("unchecked")
    private void generateResourceWires(LogicalComponent<?> component, Set<Command> commandSet) throws GenerationException {
        for (LogicalResource<?> resource : component.getResources()) {
            PhysicalWireDefinition pwd = physicalWireGenerator.generateResourceWire(component, resource);
            commandSet.add(new WireAttachCommand(pwd, order));
        }
    }

    @SuppressWarnings("unchecked")
    private void generateServiceWires(LogicalComponent<?> component, Set<Command> commandSet) throws GenerationException {

        for (LogicalService service : component.getServices()) {
            List<LogicalBinding<?>> bindings = service.getBindings();
            if (bindings.isEmpty()) {
                continue;
            }
            for (LogicalBinding<?> binding : service.getBindings()) {
                PhysicalWireDefinition pwd = physicalWireGenerator.generateBoundServiceWire(service, binding, component, null);
                commandSet.add(new WireAttachCommand(pwd, order));
            }
        }

    }

    @SuppressWarnings("unchecked")
    private void generateReferenceWires(LogicalComponent<?> component, Set<Command> commandSet) throws GenerationException {

        for (LogicalReference logicalReference : component.getReferences()) {
            if (logicalReference.getBindings().isEmpty()) {
                generateUnboundReferenceWires(logicalReference, commandSet);
            } else {
                // TODO this should be extensible and moved out
                LogicalBinding<?> logicalBinding = logicalReference.getBindings().get(0);
                PhysicalWireDefinition pwd = physicalWireGenerator.generateBoundReferenceWire(component, logicalReference, logicalBinding);
                commandSet.add(new WireAttachCommand(pwd, order));
            }
        }
        
    }

    @SuppressWarnings("unchecked")
    private void generateUnboundReferenceWires(LogicalReference logicalReference, Set<Command> commandSet) throws GenerationException {

        LogicalComponent<?> component = logicalReference.getParent();
        
        for (LogicalWire logicalWire : logicalReference.getWires()) {
            
            if (logicalWire.isProvisioned()) {
                continue;
            }

            URI uri = logicalWire.getTargetUri();
            String serviceName = uri.getFragment();
            
            LogicalComponent<?> target = logicalComponentManager.getComponent(uri);
            
            if (target == null) {
                System.err.println("++++++++++++++++++++++++++++++++++++++++++++");
                System.err.println(uri + " is unavailable in" + logicalComponentManager.getDomain().getUri());
                System.err.println("++++++++++++++++++++++++++++++++++++++++++++");
            }
            LogicalService targetService = target.getService(serviceName);
            
            assert targetService != null;
            while (CompositeImplementation.class.isInstance(target.getDefinition().getImplementation())) {
                LogicalCompositeComponent composite = (LogicalCompositeComponent) target;
                URI promoteUri = targetService.getPromotedUri();
                URI promotedComponent = UriHelper.getDefragmentedName(promoteUri);
                target = composite.getComponent(promotedComponent);
                targetService = target.getService(promoteUri.getFragment());
            }

            LogicalReference reference = logicalWire.getSource();
            PhysicalWireDefinition pwd = physicalWireGenerator.generateUnboundWire(component, reference, targetService, target);
            commandSet.add(new WireAttachCommand(pwd, order));
            
            // generate physical callback wires if the forward service is bidirectional
            if (reference.getDefinition().getServiceContract().getCallbackContract() != null) {
                pwd = physicalWireGenerator.generateUnboundCallbackWire(target, reference, component);
                commandSet.add(new WireAttachCommand(pwd, order));
            }
            
            logicalWire.setProvisioned(true);
            
        }

    }

}
