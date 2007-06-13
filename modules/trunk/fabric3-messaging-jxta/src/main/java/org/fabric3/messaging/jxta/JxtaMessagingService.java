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

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.security.cert.CertificateException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.discovery.DiscoveryService;
import net.jxta.exception.PeerGroupException;
import net.jxta.impl.id.UUID.UUID;
import net.jxta.impl.protocol.ResolverQuery;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.NetPeerGroupFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.resolver.QueryHandler;
import net.jxta.resolver.ResolverService;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.messaging.AbstractMessagingService;
import org.fabric3.messaging.jxta.pdp.PeerListener;
import org.fabric3.messaging.jxta.prp.Fabric3QueryHandler;
import org.fabric3.spi.services.messaging.DomainJoinException;
import org.fabric3.spi.services.messaging.MessagingException;
import org.fabric3.spi.services.messaging.MessagingTimeoutException;
import org.fabric3.spi.services.work.NotificationListener;
import org.fabric3.spi.services.work.NotificationListenerAdaptor;
import org.fabric3.spi.services.work.WorkScheduler;
import org.fabric3.spi.util.stax.StaxUtil;

/**
 * Messaging service implemented using JXTA.
 *
 * @version $Revision$ $Date$
 */
public class JxtaMessagingService extends AbstractMessagingService {

    /**
     * Well known peer group id.
     */
    private static final Fabric3PeerGroupID PEER_GROUP_ID =
            new Fabric3PeerGroupID(new UUID("aea468a4-6450-47dc-a288-a7f1bbcc5927"));

    /**
     * Default Messaging interval.
     */
    private static long DEFAULT_INTERVAL = 10000L;

    /**
     * Latch to block send operations until peer communications have been established
     */
    private CountDownLatch latch = new CountDownLatch(1);

    /**
     * Peer listener.
     */
    private PeerListener peerListener;

    /**
     * Resolver service.
     */
    private ResolverService resolverService;

    /**
     * Domain group.
     */
    private PeerGroup domainGroup;

    /**
     * Network platform configurator.
     */
    private NetworkConfigurator configurator;

    /**
     * Work scheduler.
     */
    private WorkScheduler workScheduler;

    /**
     * Interval for sending discivery messages .
     */
    private long interval = DEFAULT_INTERVAL;

    /**
     * Started flag.
     */
    private final AtomicBoolean started = new AtomicBoolean();

    /**
     * Message id generator.
     */
    private final AtomicInteger messageIdGenerator = new AtomicInteger();

    /**
     * TCP Port
     */
    private int tcpPort;

    /**
     * Adds a network configurator for this service.
     *
     * @param configurator Network configurator.
     */
    @Reference
    public void setConfigurator(NetworkConfigurator configurator) {
        this.configurator = configurator;
    }

    /**
     * Adds a work scheduler for runningbackground Messaging operations.
     *
     * @param workScheduler Work scheduler.
     */
    @Reference
    public void setWorkScheduler(WorkScheduler workScheduler) {
        this.workScheduler = workScheduler;
    }

    @Property
    public void setTcpPort(String tcpPort) {
        this.tcpPort = Integer.parseInt(tcpPort);
    }

    /**
     * Sets the interval at which Messaging messages are sent.
     *
     * @param interval Interval at which Messaging messages are sent.
     */
    // @Property
    public void setInterval(long interval) {
        this.interval = interval;
    }

    /**
     * Joins the Domain and runs the Messaging service in a different thread.
     *
     * @throws DomainJoinException       any unexpected JXTA exception to bubble up the call stack.
     * @throws MessagingTimeoutException if a timeout occurs joining the peer group
     */
    public void joinDomain(long waitTime) throws DomainJoinException, MessagingTimeoutException {

        try {
            // perform the join synchronously
            configure();
            createAndJoinDomainGroup();

            setupMessaging();
            setupResolver();

            started.set(true);
            latch.countDown();
            Runnable runnable = new Runnable() {
                public void run() {
                    // run the listener on a different thread
                    peerListener.start();
                }
            };
            NotificationListener<Runnable> listener = new NotificationListenerAdaptor<Runnable>();
            workScheduler.scheduleWork(runnable, listener);
        } catch (PeerGroupException ex) {
            throw new DomainJoinException("Error joining the domain", ex);
        } catch (IOException ex) {
            throw new DomainJoinException("Error joining the domain", ex);
        } catch (Exception ex) {
            throw new DomainJoinException("Error joining the domain", ex);
        }

    }

    /**
     * Sends a message to the specified runtime.
     *
     * @param runtimeId Runtime id of recipient. If null, the message is broadcasted to all runtimes in the domain.
     * @param content   Message content.
     * @return The message id.
     * @throws MessagingException In case of Messaging errors.
     */
    public int sendMessage(final String runtimeId, final XMLStreamReader content) throws MessagingException {

        if (content == null) {
            throw new IllegalArgumentException("Content id is null");
        }
        try {
            latch.await(10000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new MessagingException("Error waiting for JXTA messaging service to start", e);
        }
        PeerID peerID = null;
        if (runtimeId != null) {
            peerID = peerListener.getPeerId(runtimeId);
            if (peerID == null) {
                throw new MessagingException("Unrecognized runtime " + runtimeId);
            }
        }

        String message;
        try {
            message = StaxUtil.serialize(content);
        } catch (XMLStreamException ex) {
            throw new MessagingException(ex);
        }

        int messageId = messageIdGenerator.incrementAndGet();

        ResolverQuery query = new ResolverQuery();
        query.setHandlerName(Fabric3QueryHandler.class.getSimpleName());
        query.setQuery(message);
        query.setSrc(domainGroup.getPeerID().toString());

        if (peerID == null) {
            resolverService.sendQuery(null, query);
        } else {
            resolverService.sendQuery(peerID.toString(), query);
        }

        return messageId;

    }

    /**
     * Returns the available runtimes in the current domain.
     *
     * @return List of available runtimes.
     */
    public Set<String> getRuntimeIds() {
        return peerListener.getRuntimeIds();
    }

    /**
     * Checks whether the service is started.
     *
     * @return True if the service is started.
     */
    public boolean isStarted() {
        return started.get();
    }

    public void leaveDomain() {
        peerListener.stop();
        started.set(false);
    }

    /**
     * Configures the platform.
     *
     * @throws MessagingException any unexpected JXTA exception to bubble up the call stack.
     */
    private void configure() throws MessagingException {

        try {

            String runtimeId = getRuntimeInfo().getRuntimeId();

            configurator.setName(runtimeId);
            configurator.setHome(new File(runtimeId));
            configurator.setTcpPort(tcpPort);
            // FIXME Once property support is available
            configurator.setPassword("test-password");
            configurator.setPrincipal("test-principal");

            if (configurator.exists()) {
                File pc = new File(configurator.getHome(), "PlatformConfig");
                configurator.load(pc.toURI());
                configurator.save();
            } else {
                configurator.save();
            }

        } catch (IOException ex) {
            throw new MessagingException(ex);
        } catch (CertificateException ex) {
            throw new MessagingException(ex);
        }

    }

    /**
     * Creates and joins the domain peer group.
     *
     * @throws Exception In case of unexpected JXTA exceptions.
     */
    private void createAndJoinDomainGroup() throws Exception {

        String domain = getRuntimeInfo().getDomain().toString();

        PeerGroup netGroup = new NetPeerGroupFactory().getInterface();
        ModuleImplAdvertisement implAdv = netGroup.getAllPurposePeerGroupImplAdvertisement();
        domainGroup = netGroup.newGroup(PEER_GROUP_ID, implAdv, domain, "Fabric3 domain group");

        AuthenticationCredential authCred = new AuthenticationCredential(domainGroup, null, null);
        MembershipService membership = domainGroup.getMembershipService();
        Authenticator auth = membership.apply(authCred);

        if (auth.isReadyForJoin()) {
            membership.join(auth);
        } else {
            throw new MessagingException("Unable to join domain group");
        }

    }

    /**
     * Sets up the resolver service.
     */
    private void setupResolver() {

        resolverService = domainGroup.getResolverService();
        QueryHandler queryHandler = new Fabric3QueryHandler(resolverService, this);
        resolverService.registerHandler(Fabric3QueryHandler.class.getSimpleName(), queryHandler);

    }

    /**
     * Sets up peer Messaging service.
     */
    private void setupMessaging() {

        final DiscoveryService MessagingService = domainGroup.getDiscoveryService();
        MessagingService.remotePublish(domainGroup.getPeerAdvertisement());
        peerListener = new PeerListener(MessagingService, interval, getRuntimeInfo().getRuntimeId());

    }

    /*
     * Well known peer grroup.
     */
    @SuppressWarnings("serial")
    private static class Fabric3PeerGroupID extends net.jxta.impl.id.CBID.PeerGroupID {
        public Fabric3PeerGroupID(UUID uuid) {
            super(uuid);
        }
    }

}
