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
package org.fabric3.fabric.executor;

import java.net.URI;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.StartComponentCommand;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.services.componentmanager.ComponentManager;

/**
 * Eagerly initializes a component on a service node.
 *
 * @version $Rev: 2878 $ $Date: 2008-02-23 18:42:09 +0000 (Sat, 23 Feb 2008) $
 */
@EagerInit
public class StartComponentCommandExecutor implements CommandExecutor<StartComponentCommand> {

    private final ComponentManager componentManager;
    private CommandExecutorRegistry commandExecutorRegistry;

    @Constructor
    public StartComponentCommandExecutor(@Reference ComponentManager componentManager,
                                         @Reference CommandExecutorRegistry commandExecutorRegistry) {
        this.componentManager = componentManager;
        this.commandExecutorRegistry = commandExecutorRegistry;
    }

    public StartComponentCommandExecutor(ComponentManager componentManager) {
        this.componentManager = componentManager;
    }

    @Init
    public void init() {
        commandExecutorRegistry.register(StartComponentCommand.class, this);
    }

    public void execute(StartComponentCommand command) throws ExecutionException {
        URI uri = command.getUri();
        Component component = componentManager.getComponent(uri);
        component.start();
    }
}
