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
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.jxta.JxtaService;
import org.fabric3.messaging.jxta.prp.Fabric3QueryHandler;
import org.fabric3.spi.model.topology.RuntimeInfo;
import org.fabric3.spi.services.discovery.DiscoveryService;
import org.fabric3.messaging.MessagingEventService;
import org.fabric3.messaging.MessagingException;
import org.fabric3.messaging.MessagingService;

/**
 * Messaging service implemented using JXTA.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class JxtaMessagingService implements MessagingService {

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
    private MessagingEventService eventService;
    private MessagingMonitor monitor;


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

    @Reference
    public void setEventService(MessagingEventService eventService) {
        this.eventService = eventService;
    }

    @Monitor
    public void setMonitor(MessagingMonitor monitor) {
        this.monitor = monitor;
    }

    public String getScheme() {
        return "jxta";
    }

    /**
     * Sends a message to the specified runtime.
     *
     * @param runtimeId Runtime id of recipient. If null, the message is broadcasted to all runtimes in the domain.
     * @param content   Message content.
     * @throws MessagingException In case of Messaging errors.
     */
    public void sendMessage(final URI runtimeId, final XMLStreamReader content) throws MessagingException {

        RuntimeInfo runtimeInfo = discoveryService.getRuntimeInfo(runtimeId);
        if (runtimeInfo == null) {
            throw new MessagingException("Runtime not found:" + runtimeId);
        }
        String messageDestination = runtimeInfo.getMessageDestination();

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
     * Start method.
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
        QueryHandler queryHandler = new Fabric3QueryHandler(eventService, monitor);
        resolverService.registerHandler(Fabric3QueryHandler.class.getSimpleName(), queryHandler);
    }

}
