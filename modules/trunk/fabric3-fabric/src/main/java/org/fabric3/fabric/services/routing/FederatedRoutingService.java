/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.fabric.services.routing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Set;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.marshaller.MarshalException;
import org.fabric3.spi.marshaller.MarshallerRegistry;
import org.fabric3.spi.model.physical.PhysicalChangeSet;
import org.fabric3.spi.services.messaging.MessagingException;
import org.fabric3.spi.services.messaging.MessagingService;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.component.RegistrationException;
import org.fabric3.fabric.deployer.Deployer;

/**
 * A routing service implementation that routes physical changesets across a domain
 *
 * @version $Rev$ $Date$
 */
public class FederatedRoutingService implements RoutingService {
    private final MarshallerRegistry marshallerRegistry;
    private final MessagingService messagingService;
    private final Deployer deployer;

    public FederatedRoutingService(@Reference Deployer deployer,
                                   @Reference MarshallerRegistry marshallerRegistry,
                                   @Reference MessagingService messagingService) {
        this.deployer = deployer;
        this.marshallerRegistry = marshallerRegistry;
        this.messagingService = messagingService;
    }

    public void route(URI runtimeId, PhysicalChangeSet pcs) throws RoutingException {
        if (runtimeId == null) {
            try {
                deployer.applyChangeSet(pcs);
            } catch (BuilderException e) {
                throw new RoutingException(e);
            } catch (RegistrationException e) {
                throw new RoutingException(e);
            }

        } else {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                XMLStreamWriter pcsWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
                marshallerRegistry.marshall(pcs, pcsWriter);
                ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                XMLStreamReader pcsReader = XMLInputFactory.newInstance().createXMLStreamReader(in);
                messagingService.sendMessage(runtimeId.toASCIIString(), pcsReader);
            } catch (XMLStreamException e) {
                throw new RoutingException("Error routing change set", e);
            } catch (MarshalException e) {
                throw new RoutingException("Error routing change set", e);
            } catch (MessagingException e) {
                throw new RoutingException("Error routing change set", e);
            }
        }
    }

    public Set<String> getRuntimeIds() {
        return messagingService.getRuntimeIds();
    }
}
