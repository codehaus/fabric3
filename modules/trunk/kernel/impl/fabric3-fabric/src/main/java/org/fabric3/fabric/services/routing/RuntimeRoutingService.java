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
package org.fabric3.fabric.services.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.fabric3.scdl.Scope;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.command.ExecutionException;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.component.TargetResolutionException;
import org.fabric3.spi.generator.CommandMap;
import org.osoa.sca.annotations.Reference;

/**
 * A routing service implementation that routes to the local runtime instance. For example, this service is used to
 * route changesets for runtime extensions.
 *
 * @version $Rev$ $Date$
 */
public class RuntimeRoutingService implements RoutingService {

    private final CommandExecutorRegistry registry;
    private final ScopeRegistry scopeRegistry;

    public RuntimeRoutingService(@Reference CommandExecutorRegistry registry, @Reference ScopeRegistry scopeRegistry) {
        this.registry = registry;
        this.scopeRegistry = scopeRegistry;
    }

    public void route(CommandMap commandMap) throws RoutingException {

        Set<Command> commands = commandMap.getCommandsForRuntime(null);
        List<Command> orderedCommands = new ArrayList<Command>(commands);
        Collections.sort(orderedCommands);
        
        for (Command command : orderedCommands) {
            try {
                registry.execute(command);
            } catch (ExecutionException e) {
                throw new RoutingException(e);
            }
        }
        
        try {
            if (scopeRegistry != null) {
                scopeRegistry.getScopeContainer(Scope.COMPOSITE).reinject();
            }
        } catch (TargetResolutionException e) {
            throw new RoutingException(e);
        }

    }

}
