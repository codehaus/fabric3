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

import java.util.List;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.model.physical.PhysicalWireGenerator;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

/**
 * Generate commands to attach component reference wires to their transports.
 *
 * @version $Revision$ $Date$
 */
public class ReferenceWireCommandGenerator implements CommandGenerator {

    private final PhysicalWireGenerator physicalWireGenerator;
    private final int order;

    public ReferenceWireCommandGenerator(@Reference PhysicalWireGenerator physicalWireGenerator,
                                         @Property(name = "order")int order) {
        this.physicalWireGenerator = physicalWireGenerator;
        this.order = order;
    }

    @SuppressWarnings("unchecked")
    public WireAttachCommand generate(LogicalComponent<?> component) throws GenerationException {

        WireAttachCommand command = new WireAttachCommand(order);

        if (component instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent compositeComponent = (LogicalCompositeComponent) component;
            for (LogicalComponent<?> child : compositeComponent.getComponents()) {
                command.addPhysicalWireDefinitions(generate(child).getPhysicalWireDefinitions());
            }
        } else {
            generatePhysicalWires(component, command);
        }

        return command;
    }

    private void generatePhysicalWires(LogicalComponent<?> component, WireAttachCommand command) throws GenerationException {

        for (LogicalReference logicalReference : component.getReferences()) {
            if (logicalReference.getBindings().isEmpty()) {
                continue;
            }

            // TODO this should be extensible and moved out
            LogicalBinding<?> logicalBinding = logicalReference.getBindings().get(0);
            PhysicalWireDefinition pwd = physicalWireGenerator.generateBoundReferenceWire(component, logicalReference, logicalBinding);
            command.addPhysicalWireDefinition(pwd);
            if (logicalReference.getDefinition().getServiceContract().getCallbackContract() != null) {
                List<LogicalBinding<?>> callbackBindings = logicalReference.getCallbackBindings();
                if (callbackBindings.size() != 1) {
                    String uri = logicalReference.getUri().toString();
                    throw new UnsupportedOperationException("The runtime requires exactly one callback binding to be specified on reference ["
                            + uri + "]");
                }
                LogicalBinding<?> callbackBinding = callbackBindings.get(0);
                // generate the callback wire
                PhysicalWireDefinition callbackPwd = physicalWireGenerator.generateBoundCallbackRerenceWire(logicalReference, callbackBinding,
                                                                                                            component);
                command.addPhysicalWireDefinition(callbackPwd);
            }
        }
    }

}
