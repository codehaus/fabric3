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
package org.fabric3.extension.command;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.CommandExecutor;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.command.ExecutionException;
import org.fabric3.spi.marshaller.MarshalException;
import org.fabric3.spi.marshaller.MarshalService;
import org.fabric3.spi.services.messaging.MessagingEventService;
import org.fabric3.spi.services.messaging.RequestListener;

/**
 * Base Implementation of a CommandExecutor. This implementation may be dispatched to locally or remotely through the
 * {@link org.fabric3.spi.services.messaging.MessagingEventService}.
 *
 * @version $Rev$ $Date$
 */
@Service(CommandExecutor.class)
@EagerInit
public abstract class AbstractCommandExecutor<T extends Command> implements CommandExecutor<T>, RequestListener {
    private MessagingEventService eventService;
    private MarshalService marshalService;
    private CommandExecutorRegistry commandExecutorRegistry;
    private CommandListenerMonitor monitor;

    public AbstractCommandExecutor(@Reference MessagingEventService eventService,
                                   @Reference MarshalService marshalService,
                                   @Reference CommandExecutorRegistry commandExecutorRegistry,
                                   CommandListenerMonitor monitor) {
        this.eventService = eventService;
        this.marshalService = marshalService;
        this.commandExecutorRegistry = commandExecutorRegistry;
        this.monitor = monitor;
    }

    @Init
    public void init() {
        if (eventService != null) {
            eventService.registerRequestListener(getCommandQName(), this);
        }
        commandExecutorRegistry.register(getCommandType(), this);
    }

    public XMLStreamReader onRequest(XMLStreamReader reader) {
        try {
            T command = marshalService.unmarshall(getCommandType(), reader);
            execute(command);
        } catch (MarshalException e) {
            monitor.error(e);
        } catch (ExecutionException e) {
            monitor.error(e);
            // TODO send error notification back to controller
        }
        return null;
    }

    protected abstract QName getCommandQName();

    protected abstract Class<T> getCommandType();
}
