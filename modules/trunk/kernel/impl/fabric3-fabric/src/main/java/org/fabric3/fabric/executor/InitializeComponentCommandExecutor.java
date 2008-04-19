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
import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.ComponentInitializationUri;
import org.fabric3.fabric.command.InitializeComponentCommand;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.runtime.component.ComponentManager;

/**
 * Eagerly initializes a component on a service node.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class InitializeComponentCommandExecutor implements CommandExecutor<InitializeComponentCommand> {
    private CommandExecutorRegistry commandExecutorRegistry;
    private ComponentManager manager;
    private ScopeContainer<?> scopeContainer;

    public InitializeComponentCommandExecutor(ScopeRegistry scopeRegistry, ComponentManager manager) {
        this(null, scopeRegistry, manager);
    }

    @Constructor
    public InitializeComponentCommandExecutor(@Reference CommandExecutorRegistry commandExecutorRegistry,
                                              @Reference ScopeRegistry scopeRegistry,
                                              @Reference ComponentManager manager) {
        this.commandExecutorRegistry = commandExecutorRegistry;
        this.manager = manager;
        this.scopeContainer = scopeRegistry.getScopeContainer(Scope.COMPOSITE);
    }

    @Init
    public void init() {
        commandExecutorRegistry.register(InitializeComponentCommand.class, this);
    }

    public void execute(InitializeComponentCommand command) throws ExecutionException {

        ComponentInitializationUri componentInitializationUri = command.getUri();
        URI groupId = componentInitializationUri.getGroupId();
        URI uri = componentInitializationUri.getUri();
        Component component = manager.getComponent(uri);
        if (!(component instanceof AtomicComponent)) {
            throw new ComponentNotRegisteredException("Component not registered: " + uri.toString(), uri.toString());
        }
        WorkContext workContext = new WorkContext();
        CallFrame frame = new CallFrame(groupId);
        workContext.addCallFrame(frame);
        List<AtomicComponent<?>> atomicComponents = new ArrayList<AtomicComponent<?>>();
        atomicComponents.add((AtomicComponent<?>) component);
        try {
            scopeContainer.initializeComponents(atomicComponents, workContext);
        } catch (GroupInitializationException e) {
            throw new ExecutionException("Error starting components", e);
        }


    }
}
