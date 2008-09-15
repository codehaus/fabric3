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
package org.fabric3.fabric.generator.wire;

import java.net.URI;
import java.util.List;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.AttachWireCommand;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.generator.AddCommandGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

/**
 * Generate a command to create a wire from a service endpoint to a component.
 *
 * @version $Revision$ $Date$
 */
public class ServiceWireCommandGenerator implements AddCommandGenerator {

    private final PhysicalWireGenerator physicalWireGenerator;
    private final int order;

    public ServiceWireCommandGenerator(@Reference PhysicalWireGenerator physicalWireGenerator, @Property(name = "order")int order) {
        this.physicalWireGenerator = physicalWireGenerator;
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public AttachWireCommand generate(LogicalComponent<?> component) throws GenerationException {
        if (component instanceof LogicalCompositeComponent) {
            return null;
        }
        AttachWireCommand command = new AttachWireCommand(order);
        generatePhysicalWires(component, command);
        return command;
    }

    private void generatePhysicalWires(LogicalComponent<?> component, AttachWireCommand command) throws GenerationException {

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
                callbackUri = callbackBinding.getBinding().getTargetUri();
            }

            for (LogicalBinding<?> binding : service.getBindings()) {
                if (!binding.isProvisioned()) {
                    PhysicalWireDefinition pwd = physicalWireGenerator.generateBoundServiceWire(service, binding, component, callbackUri);
                    command.addPhysicalWireDefinition(pwd);
                    binding.setProvisioned(true);
                }
            }
            // generate the callback command set
            if (callbackBinding != null && !callbackBinding.isProvisioned()) {
                PhysicalWireDefinition callbackPwd = physicalWireGenerator.generateBoundCallbackServiceWire(component, service, callbackBinding);
                command.addPhysicalWireDefinition(callbackPwd);
                callbackBinding.setProvisioned(true);
            }
        }

    }


}