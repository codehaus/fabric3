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
package org.fabric3.messaging.listener;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.monitor.MonitorFactory;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.command.CommandSet;
import org.fabric3.spi.command.ExecutionException;
import org.fabric3.spi.marshaller.MarshalException;
import org.fabric3.spi.marshaller.MarshalService;
import org.fabric3.spi.services.messaging.MessagingEventService;
import org.fabric3.spi.services.messaging.RequestListener;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class CommandSetListener implements RequestListener {
    private MessagingEventService eventService;
    private CommandExecutorRegistry executorRegistry;
    private MarshalService marshalService;
    private ListenerMonitor monitor;

    public CommandSetListener(@Reference MessagingEventService eventService,
                              @Reference CommandExecutorRegistry executorRegistry,
                              @Reference MarshalService marshalService,
                              @Reference MonitorFactory factory) {
        this.eventService = eventService;
        this.executorRegistry = executorRegistry;
        this.marshalService = marshalService;
        monitor = factory.getMonitor(ListenerMonitor.class);
    }

    @Init
    public void init() {
        QName qName = new QName(CommandSet.class.getName());
        eventService.registerRequestListener(qName, this);
    }

    public XMLStreamReader onRequest(XMLStreamReader reader) {
        try {
            CommandSet set = marshalService.unmarshall(CommandSet.class, reader);
            for (Command command : set.getCommands(CommandSet.Phase.FIRST)) {
                executorRegistry.execute(command);
            }
            for (Command command : set.getCommands(CommandSet.Phase.STANDARD)) {
                executorRegistry.execute(command);
            }
            for (Command command : set.getCommands(CommandSet.Phase.LAST)) {
                executorRegistry.execute(command);
            }
        } catch (MarshalException e) {
            monitor.error(e);
        } catch (ExecutionException e) {
            monitor.error(e);
            // TODO send error notification back to controller
        }
        return null;
    }
}
