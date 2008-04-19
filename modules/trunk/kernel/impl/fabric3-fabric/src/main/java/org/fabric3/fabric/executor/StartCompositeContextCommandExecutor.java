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

import org.fabric3.fabric.command.StartCompositeContextCommand;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;

/**
 * Executes a {@link org.fabric3.fabric.command.StartCompositeContextCommand}.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class StartCompositeContextCommandExecutor implements CommandExecutor<StartCompositeContextCommand> {
    private ScopeContainer<URI> container;
    private CommandExecutorRegistry commandExecutorRegistry;

    @Constructor
    public StartCompositeContextCommandExecutor(@Reference CommandExecutorRegistry commandExecutorRegistry,
                                                @Reference ScopeRegistry scopeRegistry) {
        this.commandExecutorRegistry = commandExecutorRegistry;
        this.container = scopeRegistry.getScopeContainer(Scope.COMPOSITE);
    }

    public StartCompositeContextCommandExecutor(ScopeRegistry scopeRegistry) {
        this.container = scopeRegistry.getScopeContainer(Scope.COMPOSITE);
    }

    @Init
    public void init() {
        commandExecutorRegistry.register(StartCompositeContextCommand.class, this);
    }

    public void execute(StartCompositeContextCommand command) throws ExecutionException {
        URI groupId = command.getGroupId();
        WorkContext workContext = new WorkContext();
        CallFrame frame = new CallFrame(groupId);
        workContext.addCallFrame(frame);
        try {
            container.startContext(workContext);
        } catch (GroupInitializationException e) {
            throw new ExecutionException("Error executing command", e);
        }
    }

}
