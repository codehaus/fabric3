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
package org.fabric3.fabric.command;

import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.command.CommandExecutor;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.command.ExecutionException;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.runtime.component.RegistrationException;
import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * Eagerly initializes a component on a service node.
 *
 * @version $Rev: 2878 $ $Date: 2008-02-23 18:42:09 +0000 (Sat, 23 Feb 2008) $
 */
@EagerInit
public class ComponentBuildCommandExecutor implements CommandExecutor<ComponentBuildCommand> {
    
    private final ComponentBuilderRegistry componentBuilderRegistry;
    private final ComponentManager componentManager;
    private CommandExecutorRegistry commandExecutorRegistry;

    @Constructor
    public ComponentBuildCommandExecutor(@Reference ComponentBuilderRegistry componentBuilderRegistry,
                                         @Reference ComponentManager componentManager,
                                         @Reference CommandExecutorRegistry commandExecutorRegistry) {
        this.componentBuilderRegistry = componentBuilderRegistry;
        this.componentManager = componentManager;
        this.commandExecutorRegistry = commandExecutorRegistry;
    }

    public ComponentBuildCommandExecutor(@Reference ComponentBuilderRegistry componentBuilderRegistry, @Reference ComponentManager componentManager) {
        this.componentBuilderRegistry = componentBuilderRegistry;
        this.componentManager = componentManager;
    }

    @Init
    public void init() {
        commandExecutorRegistry.register(ComponentBuildCommand.class, this);
    }

    public void execute(ComponentBuildCommand command) throws ExecutionException {
        
        try {
            for (PhysicalComponentDefinition physicalComponentDefinition : command.getPhysicalComponentDefinitions()) {
                final Component component = componentBuilderRegistry.build(physicalComponentDefinition);
                componentManager.register(component);
            }
        } catch (BuilderException e) {
            throw new ExecutionException(e.getMessage(), e);
        } catch (RegistrationException e) {
            throw new ExecutionException(e.getMessage(), e);
        }
    }
}
