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

import java.util.Collection;
import java.util.List;

import org.fabric3.fabric.services.routing.RoutingException;
import org.fabric3.fabric.services.routing.RoutingService;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.CommandMap;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * Default implementation of the physical model generator.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class PhysicalModelGeneratorImpl implements PhysicalModelGenerator {

    private final List<CommandGenerator> commandGenerators;
    private final RoutingService routingService;

    /**
     * @param generatorRegistry
     */
    public PhysicalModelGeneratorImpl(@Reference(name="commandGenerators") List<CommandGenerator> commandGenerators, 
                                      @Reference RoutingService routingService) {
        this.commandGenerators = commandGenerators;
        this.routingService = routingService;
    }

    public CommandMap generate(Collection<LogicalComponent<?>> components) throws GenerationException {

        CommandMap commandMap = new CommandMap();
        
        for (LogicalComponent<?> component : components) {
            for (CommandGenerator commandGenerator : commandGenerators) {
                Command command = commandGenerator.generate(component);
                commandMap.addCommand(component.getRuntimeId(), command);
            }
            component.setProvisioned(true);
        }
        
        return commandMap;

    }

    public void provision(CommandMap commandMap) throws RoutingException {
        routingService.route(commandMap);
    }

}
