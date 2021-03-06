/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.fabric.generator.wire;

import java.net.URI;
import java.util.List;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.AttachWireCommand;
import org.fabric3.fabric.command.DetachWireCommand;
import org.fabric3.fabric.command.ConnectionCommand;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

/**
 * Generates commands to create wires from a service endpoint to a component for components being deployed and commands to detach wires for components
 * being undeployed.
 *
 * @version $Revision$ $Date$
 */
public class ServiceWireCommandGenerator implements CommandGenerator {
    private final PhysicalWireGenerator physicalWireGenerator;
    private final int order;

    public ServiceWireCommandGenerator(@Reference PhysicalWireGenerator physicalWireGenerator, @Property(name = "order") int order) {
        this.physicalWireGenerator = physicalWireGenerator;
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public ConnectionCommand generate(LogicalComponent<?> component, boolean incremental) throws GenerationException {
        if (component instanceof LogicalCompositeComponent) {
            return null;
        }

        // determine if a new binding has been added to a provisioned component. If so, an AttachWireCommand must be generated.
        boolean newBinding = false;
        for (LogicalService service : component.getServices()) {
            for (LogicalBinding<?> binding : service.getBindings()) {
                if (binding.getState() == LogicalState.NEW) {
                    newBinding = true;
                    break;
                }
            }
        }
        if (LogicalState.PROVISIONED == component.getState() && incremental && !newBinding) {
            return null;
        }

        ConnectionCommand command = new ConnectionCommand();
        generatePhysicalWires(component, command, incremental);
        if (command.getAttachCommands().isEmpty() && command.getDetachCommands().isEmpty()) {
            return null;
        }
        return command;
    }

    private void generatePhysicalWires(LogicalComponent<?> component, ConnectionCommand command, boolean incremental)
            throws GenerationException {
        for (LogicalService service : component.getServices()) {
            List<LogicalBinding<?>> bindings = service.getBindings();
            if (bindings.isEmpty()) {
                continue;
            }
            ServiceContract<?> callbackContract = service.getDefinition().getServiceContract().getCallbackContract();
            LogicalBinding<?> callbackBinding = null;
            URI callbackUri = null;
            if (callbackContract != null) {
                List<LogicalBinding<?>> callbackBindings = service.getCallbackBindings();
                if (callbackBindings.size() != 1) {
                    String uri = service.getUri().toString();
                    throw new UnsupportedOperationException("The runtime requires exactly one callback binding to be specified on service: " + uri);
                }
                callbackBinding = callbackBindings.get(0);
                // xcv FIXME should be on the logical binding
                callbackUri = callbackBinding.getDefinition().getTargetUri();
            }

            for (LogicalBinding<?> binding : service.getBindings()) {
                if (binding.getState() == LogicalState.NEW || binding.getState() == LogicalState.NEW || !incremental) {
                    PhysicalWireDefinition pwd = physicalWireGenerator.generateBoundServiceWire(service, binding, callbackUri);
                    if (LogicalState.MARKED == component.getState()) {
                        DetachWireCommand detachWireCommand = new DetachWireCommand();
                        detachWireCommand.setPhysicalWireDefinition(pwd);
                        command.add(detachWireCommand);
                    } else {
                        AttachWireCommand attachWireCommand = new AttachWireCommand();
                        attachWireCommand.setPhysicalWireDefinition(pwd);
                        command.add(attachWireCommand);
                    }

                }
            }
            // generate the callback command set
            if (callbackBinding != null
                    && (callbackBinding.getState() == LogicalState.NEW
                    || callbackBinding.getState() == LogicalState.MARKED
                    || !incremental)) {
                PhysicalWireDefinition callbackPwd = physicalWireGenerator.generateBoundCallbackServiceWire(service, callbackBinding);
                if (LogicalState.MARKED == component.getState()) {
                    DetachWireCommand detachWireCommand = new DetachWireCommand();
                    detachWireCommand.setPhysicalWireDefinition(callbackPwd);
                    command.add(detachWireCommand);
                } else {
                    AttachWireCommand attachWireCommand = new AttachWireCommand();
                    attachWireCommand.setPhysicalWireDefinition(callbackPwd);
                    command.add(attachWireCommand);
                }
            }
        }
    }


}