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
 */
package org.fabric3.messaging.listener;

import java.util.LinkedHashSet;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.messaging.MessagingEventService;
import org.fabric3.messaging.RequestListener;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.services.marshaller.MarshalException;
import org.fabric3.spi.services.marshaller.MarshalService;

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
                              @Monitor ListenerMonitor monitor) {
        this.eventService = eventService;
        this.executorRegistry = executorRegistry;
        this.marshalService = marshalService;
        this.monitor = monitor;
    }

    @Init
    public void init() {
        QName qName = new QName(null, "linked-hash-set");
        eventService.registerRequestListener(qName, this);
    }

    public XMLStreamReader onRequest(XMLStreamReader reader) {
        try {
            LinkedHashSet<Command> commands = marshalService.unmarshall(LinkedHashSet.class, reader);
            for (Command command : commands) {
                executorRegistry.execute(command);
            }
            monitor.executed();
        } catch (MarshalException e) {
            monitor.error(e);
        } catch (ExecutionException e) {
            monitor.error(e);
            // TODO send error notification back to controller
        }
        return null;
    }
}