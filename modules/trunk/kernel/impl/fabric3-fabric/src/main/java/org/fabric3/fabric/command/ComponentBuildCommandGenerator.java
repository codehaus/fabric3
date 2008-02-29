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

import org.fabric3.fabric.implementation.singleton.SingletonImplementation;
import org.fabric3.scdl.Implementation;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

/**
 *
 * @version $Revision$ $Date$
 */
public class ComponentBuildCommandGenerator implements CommandGenerator {
    
    private final GeneratorRegistry generatorRegistry;
    private final int order;

    public ComponentBuildCommandGenerator(@Reference GeneratorRegistry generatorRegistry, @Property(name="order") int order) {
        this.generatorRegistry = generatorRegistry;
        this.order = order;
    }

    @SuppressWarnings("unchecked")
    public ComponentBuildCommand generate(LogicalComponent<?> component) throws GenerationException {
        
        ComponentBuildCommand command = new ComponentBuildCommand(order);
        Implementation<?> implementation = component.getDefinition().getImplementation();
        
        if (component instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent compositeComponent = (LogicalCompositeComponent) component;
            for (LogicalComponent<?> child : compositeComponent.getComponents()) {
                command.addPhysicalComponentDefinitions(generate(child).getPhysicalComponentDefinitions());
            }
        } else if (!component.isProvisioned() && !implementation.isType(SingletonImplementation.IMPLEMENTATION_SINGLETON)) {
            ComponentGenerator generator =  generatorRegistry.getComponentGenerator(component.getDefinition().getImplementation().getClass());
            PhysicalComponentDefinition physicalComponentDefinition = generator.generate(component);
            command.addPhysicalComponentDefinition(physicalComponentDefinition);
        }
        
        return command;
        
    }

}
