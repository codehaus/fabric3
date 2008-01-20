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
import org.fabric3.spi.deployer.Deployer;
import org.fabric3.spi.marshaller.MarshalService;
import org.fabric3.spi.model.physical.PhysicalChangeSet;
import org.fabric3.spi.services.messaging.MessagingEventService;
import org.fabric3.spi.services.messaging.RequestListener;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class PhysicalChangeSetListener implements RequestListener {
    private MarshalService marshalService;
    private ListenerMonitor monitor;
    private MessagingEventService messagingService;
    private Deployer deployer;

    public PhysicalChangeSetListener(@Reference MarshalService marshalService,
                                     @Reference MessagingEventService messagingService,
                                     @Reference Deployer deployer,
                                     @Reference MonitorFactory factory) {
        this.marshalService = marshalService;
        this.messagingService = messagingService;
        this.deployer = deployer;
        monitor = factory.getMonitor(ListenerMonitor.class);
    }

    @Init
    public void init() {
        QName qName = new QName(PhysicalChangeSet.class.getName());
        messagingService.registerRequestListener(qName, this);
    }

    /**
     * Applies the changes set
     *
     * @param content the serialized changeset.
     * @return Response to the request message.
     */
    public XMLStreamReader onRequest(XMLStreamReader content) {
        try {
            PhysicalChangeSet changeSet = marshalService.unmarshall(PhysicalChangeSet.class, content);
            deployer.applyChangeSet(changeSet);
        } catch (Throwable ex) {
            monitor.error(ex);
            return null;
        }
        // TODO Handle response messages.
        return null;
    }
}