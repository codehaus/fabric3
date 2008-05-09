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

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.WireAttachCommand;
import org.fabric3.spi.generator.AddCommandGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

/**
 * Generate a command to attach local wires from a component to its resources.
 *
 * @version $Revision$ $Date$
 */
public class ResourceWireCommandGenerator implements AddCommandGenerator {

    private final PhysicalWireGenerator physicalWireGenerator;
    private final int order;

    public ResourceWireCommandGenerator(@Reference PhysicalWireGenerator physicalWireGenerator, @Property(name = "order")int order) {
        this.physicalWireGenerator = physicalWireGenerator;
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public WireAttachCommand generate(LogicalComponent<?> component) throws GenerationException {
        if (component instanceof LogicalCompositeComponent) {
            return null;
        }
        WireAttachCommand command = new WireAttachCommand(order);
        generatePhysicalWires(component, command);
        return command;
    }

    private void generatePhysicalWires(LogicalComponent<?> component, WireAttachCommand command) throws GenerationException {
        for (LogicalResource<?> resource : component.getResources()) {
            PhysicalWireDefinition pwd = physicalWireGenerator.generateResourceWire(component, resource);
            command.addPhysicalWireDefinition(pwd);
        }
    }

}