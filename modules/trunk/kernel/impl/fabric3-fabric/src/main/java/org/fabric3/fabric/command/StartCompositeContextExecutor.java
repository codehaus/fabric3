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
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.command.AbstractCommandExecutor;
import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.command.ExecutionException;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.marshaller.MarshallerRegistry;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.services.messaging.MessagingService;

/**
 * Executes a {@link org.fabric3.fabric.command.StartCompositeContextCommand}. This implementation may be dispatched to
 * locally or remotely theough the {@link MessagingService}.
 *
 * @version $Rev$ $Date$
 */
public class StartCompositeContextExecutor extends AbstractCommandExecutor<StartCompositeContextCommand> {
    private ScopeContainer<URI> container;

    @Constructor
    public StartCompositeContextExecutor(@Reference MessagingService messagingService,
                                         @Reference MarshallerRegistry marshallerRegistry,
                                         @Reference CommandExecutorRegistry executorRegistry,
                                         @Reference ScopeRegistry scopeRegistry,
                                         @Reference MonitorFactory factory) {
        super(messagingService, marshallerRegistry, executorRegistry, factory);
        this.container = scopeRegistry.getScopeContainer(Scope.COMPOSITE);
    }

    public StartCompositeContextExecutor(CommandExecutorRegistry executorRegistry,
                                         ScopeRegistry scopeRegistry,
                                         MonitorFactory factory) {
        super(null, null, executorRegistry, factory);
        this.container = scopeRegistry.getScopeContainer(Scope.COMPOSITE);
    }

    public void execute(StartCompositeContextCommand command) throws ExecutionException {
        WorkContext workContext = new SimpleWorkContext();
        URI id = command.getGroupId();
        workContext.setScopeIdentifier(Scope.COMPOSITE, id);
        try {
            container.startContext(workContext, id);
        } catch (GroupInitializationException e) {
            throw new ExecutionException("Error executing command", e);
        }
    }

    protected QName getCommandQName() {
        return StartCompositeContextCommand.QNAME;
    }

    protected Class<StartCompositeContextCommand> getCommandType() {
        return StartCompositeContextCommand.class;
    }


}
