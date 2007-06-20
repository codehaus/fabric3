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
package org.fabric3.messaging.jxta;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.jxta.impl.protocol.ResolverQuery;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.resolver.QueryHandler;
import net.jxta.resolver.ResolverService;

import org.fabric3.extension.messaging.AbstractMessagingService;
import org.fabric3.jxta.JxtaService;
import org.fabric3.messaging.jxta.prp.Fabric3QueryHandler;
import org.fabric3.spi.model.topology.RuntimeInfo;
import org.fabric3.spi.services.discovery.DiscoveryService;
import org.fabric3.spi.services.messaging.MessagingException;
import org.fabric3.spi.util.stax.StaxUtil;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * Messaging service implemented using JXTA.
 *
 * @version $Revision$ $Date$
 */
public class JxtaMessagingService extends AbstractMessagingService {

    /**
     * Resolver service.
     */
    private ResolverService resolverService;

    /**
     * Domain group.
     */
    private PeerGroup domainGroup;

    /**
     * JXTA service.
     */
    private JxtaService jxtaService;

    /**
     * Discovery service.
     */
    private DiscoveryService discoveryService;

    /**
     * Injected JXTA service to be used.
     *
     * @param jxtaService JXTA service..
     */
    @Reference
    public void setJxtaService(JxtaService jxtaService) {
        this.jxtaService = jxtaService;
    }

    /**
     * Injected discovery service to be used.
     *
     * @param discoveryService Discovery service..
     */
    @Reference
    public void setDiscoveryService(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /**
     * Sends a message to the specified runtime.
     *
     * @param runtimeId Runtime id of recipient. If null, the message is broadcasted to all runtimes in the domain.
     * @param content   Message content.
     * @return The message id.
     * @throws MessagingException In case of Messaging errors.
     */
    public void sendMessage(final String runtimeId, final XMLStreamReader content) throws MessagingException {

        RuntimeInfo runtimeInfo = discoveryService.getRuntimeInfo(runtimeId);
        String messageDestination = (String) runtimeInfo.getMessageDestination();

        PeerID peerID;
        try {
            peerID = (PeerID) PeerID.create(new URI(messageDestination));
        } catch (URISyntaxException ex) {
            throw new MessagingException(ex);
        }

        String message;
        try {
            message = StaxUtil.serialize(content);
        } catch (XMLStreamException ex) {
            throw new MessagingException(ex);
        }

        ResolverQuery query = new ResolverQuery();
        query.setHandlerName(Fabric3QueryHandler.class.getSimpleName());
        query.setQuery(message);
        query.setSrc(domainGroup.getPeerID().toString());

        if (peerID == null) {
            resolverService.sendQuery(null, query);
        } else {
            resolverService.sendQuery(peerID.toString(), query);
        }

    }

    /**
     * @see org.fabric3.spi.services.messaging.MessagingService#getMessageDestination()
     */
    public Object getMessageDestination() {
        return domainGroup.getPeerID();
    }

    /**
     * Start method.
     *
     */
    @Init
    public void start() {

        domainGroup = jxtaService.getDomainGroup();
        setupResolver();

    }

    /**
     * Sets up the resolver service.
     */
    private void setupResolver() {

        resolverService = domainGroup.getResolverService();
        QueryHandler queryHandler = new Fabric3QueryHandler(this);
        resolverService.registerHandler(Fabric3QueryHandler.class.getSimpleName(), queryHandler);

    }

}
