/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
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
package org.fabric3.fabric.services.routing;

import java.util.Set;

import org.osoa.sca.annotations.Reference;

import org.fabric3.scdl.Scope;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.generator.CommandMap;
import org.fabric3.spi.services.routing.RoutingException;
import org.fabric3.spi.services.routing.RoutingService;

/**
 * A routing service implementation that routes commands to the local runtime instance.
 *
 * @version $Rev$ $Date$
 */
public class LocalRoutingService implements RoutingService {

    private CommandExecutorRegistry registry;
    private ScopeRegistry scopeRegistry;

    public LocalRoutingService(@Reference CommandExecutorRegistry registry, @Reference ScopeRegistry scopeRegistry) {
        this.registry = registry;
        this.scopeRegistry = scopeRegistry;
    }

    public void route(String id, CommandMap commandMap) throws RoutingException {

        Set<Command> commands = commandMap.getCommandsForZone(null);
        for (Command command : commands) {
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
        } catch (InstanceLifecycleException e) {
            throw new RoutingException(e);
        }

    }

}
