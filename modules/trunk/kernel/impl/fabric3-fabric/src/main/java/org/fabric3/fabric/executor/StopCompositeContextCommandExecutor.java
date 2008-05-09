package org.fabric3.fabric.executor;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Init;

import org.fabric3.fabric.command.StopCompositeContextCommand;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.scdl.Scope;

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
public class StopCompositeContextCommandExecutor implements CommandExecutor<StopCompositeContextCommand> {

    private ScopeContainer<URI> container;
    private CommandExecutorRegistry commandExecutorRegistry;


    public StopCompositeContextCommandExecutor(@Reference CommandExecutorRegistry commandExecutorRegistry,
                                                @Reference ScopeRegistry scopeRegistry) {
        this.commandExecutorRegistry = commandExecutorRegistry;
        this.container = scopeRegistry.getScopeContainer(Scope.COMPOSITE);
    }


    @Init
    public void init() {
        commandExecutorRegistry.register(StopCompositeContextCommand.class, this);
    }

    public void execute(StopCompositeContextCommand command) throws ExecutionException {
        URI groupId = command.getGroupId();
        WorkContext workContext = new WorkContext();
        CallFrame frame = new CallFrame(groupId);
        workContext.addCallFrame(frame);
        container.stopContext(workContext);

    }

}

