package org.fabric3.fabric.executor;

import java.net.URI;

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.EagerInit;

import org.fabric3.fabric.command.StopComponentCommand;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.services.componentmanager.RegistrationException;
import org.fabric3.spi.component.Component;

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
@EagerInit
public class StopComponentCommandExecutor implements CommandExecutor<StopComponentCommand> {

    private final ComponentManager componentManager;
    private final CommandExecutorRegistry commandExecutorRegistry;

    public StopComponentCommandExecutor(@Reference ComponentManager componentManager,
                                         @Reference CommandExecutorRegistry commandExecutorRegistry) {
        this.componentManager = componentManager;
        this.commandExecutorRegistry = commandExecutorRegistry;
    }

    @Init
    public void init() {
        commandExecutorRegistry.register(StopComponentCommand.class, this);
    }

    public void execute(StopComponentCommand command) throws ExecutionException {
        URI uri = command.getUri();
        Component component = componentManager.getComponent(uri);
        component.stop();
        try {
            componentManager.unregister(component);
        } catch (RegistrationException re) {
            throw new ExecutionException("Unexpected exception unregistering component", re);
        }
    }
}

