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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.command.AbstractCommandExecutor;
import org.fabric3.extension.command.CommandListenerMonitor;
import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.command.ExecutionException;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.marshaller.MarshalService;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.services.messaging.MessagingEventService;

/**
 * Eagerly initializes a component on a service node.
 *
 * @version $Rev$ $Date$
 */
public class InitializeComponentExecutor extends AbstractCommandExecutor<InitializeComponentCommand> {
    private ComponentManager manager;
    private ScopeContainer<?> scopeContainer;

    @Constructor
    public InitializeComponentExecutor(@Reference MessagingEventService eventService,
                                       @Reference MarshalService marshalService,
                                       @Reference CommandExecutorRegistry commandExecutorRegistry,
                                       @Reference ScopeRegistry scopeRegistry,
                                       @Reference ComponentManager manager,
                                       @Reference MonitorFactory factory) {

        super(eventService,
              marshalService,
              commandExecutorRegistry,
              factory.getMonitor(CommandListenerMonitor.class));
        this.manager = manager;
        scopeContainer = scopeRegistry.getScopeContainer(Scope.COMPOSITE);
    }

    public void execute(InitializeComponentCommand command) throws ExecutionException {
        List<URI> uris = command.getUris();
        List<AtomicComponent<?>> components = new ArrayList<AtomicComponent<?>>();
        for (URI uri : uris) {
            Component component = manager.getComponent(uri);
            if (!(component instanceof AtomicComponent)) {
                throw new ComponentNotRegisteredException("Component not registered", uri.toString());
            }
            components.add((AtomicComponent) component);
        }
        WorkContext workContext = new SimpleWorkContext();
        workContext.setScopeIdentifier(Scope.COMPOSITE, command.getGroupId());
        try {
            scopeContainer.initializeComponents(components, workContext);
        } catch (GroupInitializationException e) {
            throw new ExecutionException("Error starting components", e);
        }
    }

    protected QName getCommandQName() {
        return InitializeComponentCommand.QNAME;
    }

    protected Class<InitializeComponentCommand> getCommandType() {
        return InitializeComponentCommand.class;
    }
}
