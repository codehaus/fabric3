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

import java.util.LinkedHashSet;
import java.util.Set;

import org.fabric3.spi.command.Command;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.osoa.sca.annotations.Property;

/**
 *
 * @version $Revision$ $Date$
 */
public class ComponentStartCommandGenerator implements CommandGenerator {

    private final int order;

    public ComponentStartCommandGenerator(@Property(name="order") int order) {
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
        } else if (!component.isProvisioned()) {
            commandSet.add(new ComponentStartCommand(component.getUri(), order));
        }
        
        return commandSet;
        
    }

}
