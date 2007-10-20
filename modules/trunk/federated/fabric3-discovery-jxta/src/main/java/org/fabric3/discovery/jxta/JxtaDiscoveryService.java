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
package org.fabric3.discovery.jxta;

import static net.jxta.discovery.DiscoveryService.ADV;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import net.jxta.document.AdvertisementFactory;
import net.jxta.peergroup.PeerGroup;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.jxta.JxtaService;
import org.fabric3.spi.model.topology.RuntimeInfo;
import org.fabric3.spi.services.discovery.DiscoveryService;
import org.fabric3.spi.services.runtime.RuntimeInfoService;
import org.fabric3.spi.services.work.WorkScheduler;
import org.fabric3.spi.util.TwosTuple;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

/**
 * JXTA implementation of the discovery service.
 * <p/>
 * <p/>
 * The implementation uses the JXTA PDP to broadcast advertisements on the current node and receive advertisements from
 * the nodes participating in the same domain. Requests for advertisements from remote nodes and publishing
 * advertisements for the current node are performed in a different thread using the work scheduler. This is done every
 * 2 seconds by default, however, this can be configured through the <code>interval</code> property. </p>
 * <p/>
 * <p/>
 * A remote node from which no advertisment has been received for the 10 seconds by default is expelled from the current
 * domain view. However, this can be overridden using the property <code>expirationThreshold</code>. </p>
 * <p/>
 * <p/>
 * The advertisements include serialized information on the <code>RuntimeInfo</code> of the node broadcasting the
 * advertisement. Currently, serialization is performed using xstream and transported using the description attribute of
 * the peer discovery advertisement. However, it is worth investigating using custom advertisements. </p>
 * <p/>
 * <p/>
 * The discovery service is injected with the <code>AdvertisementService</code>. Services within local nodes should use
 * this service as a mediator for locally advertising their features. These features are then broadcasted to the wider
 * domain by the discovery service. </p>
 *
 * @version $Revsion$ $Date$
 */
public class JxtaDiscoveryService implements DiscoveryService {

    // Polling interval
    private long interval = 2000L;

    // Expiration threshold
    private long expirationThreshold = 10000L;

    // JXTA Service
    private JxtaService jxtaService;

    // Work scheduler
    private WorkScheduler workScheduler;

    // Host info
    private HostInfo hostInfo;

    // Runtime info service
    private RuntimeInfoService runtimeInfoService;

    // Jxta discovery service
    private net.jxta.discovery.DiscoveryService discoveryService;

    // Publisher of advertismenets
    private Publisher publisher;

    // Participating runtimes
    private Map<String, TwosTuple<RuntimeInfo, Long>> participatingRuntimes =
            new ConcurrentHashMap<String, TwosTuple<RuntimeInfo, Long>>();

    /**
     * @see org.fabric3.spi.services.discovery.DiscoveryService#getParticipatingRuntimes()
     */
    public Set<RuntimeInfo> getParticipatingRuntimes() {
        Set<RuntimeInfo> ret = new HashSet<RuntimeInfo>();
        for (TwosTuple<RuntimeInfo, Long> tuple : participatingRuntimes.values()) {
            ret.add(tuple.getFirst());
        }
        return ret;
    }

    /**
     * @see org.fabric3.spi.services.discovery.DiscoveryService#getRuntimeInfo(java.lang.String)
     */
    public RuntimeInfo getRuntimeInfo(String runtimeId) {
        return participatingRuntimes.get(runtimeId).getFirst();
    }

    /**
     * Sets the interval in which discovery messages and advertisements are sent.
     *
     * @param interval Polling interval.
     */
    @Property
    public void setInterval(long interval) {
        this.interval = interval;
    }

    /**
     * Sets the expiration threshold after which the runtime expelled.
     *
     * @param expirationThreshold Polling interval.
     */
    @Property
    public void setExpirationThreshold(long expirationThreshold) {
        this.expirationThreshold = expirationThreshold;
    }

    /**
     * Injects the host info.
     *
     * @param hostInfo info to be injected in.
     */
    @Reference
    public void setHostInfo(HostInfo hostInfo) {
        this.hostInfo = hostInfo;
    }

    /**
     * Injects the JXTA service.
     *
     * @param jxtaService JXTA service to be injected in.
     */
    @Reference
    public void setJxtaService(JxtaService jxtaService) {
        this.jxtaService = jxtaService;
    }

    /**
     * Injects the runtime info service.
     *
     * @param runtimeInfoService Runtime info service to be injected in.
     */
    @Reference
    public void setRuntimeInfoService(RuntimeInfoService runtimeInfoService) {
        this.runtimeInfoService = runtimeInfoService;
    }

    /**
     * Injects the work scheduler.
     *
     * @param workScheduler Work scheduler to be injected in.
     */
    @Reference
    public void setWorkScheduler(WorkScheduler workScheduler) {
        this.workScheduler = workScheduler;
    }

    /**
     * Starts the service.
     *
     * @param timeout the time to wait to join the domain
     */
    public void joinDomain(long timeout) {

        assert workScheduler != null;
        assert jxtaService != null;
        assert hostInfo != null;

        PeerGroup peerGroup = jxtaService.getDomainGroup();

        discoveryService = peerGroup.getDiscoveryService();

        publisher = new Publisher();
        workScheduler.scheduleWork(publisher);

    }

    /**
     * Stops the service.
     */
    @Destroy
    public void stop() {
        publisher.live.set(false);
    }

    /*
     * Notifier sending information about the current node.
     *
     */
    private class Publisher implements Runnable {

        private AtomicBoolean live = new AtomicBoolean(true);

        /*
         * Waits for the defined interval and sends advertisements for the
         * current node and discovery requests for the other nodes in the
         * domain.
         */
        public void run() {

            while (live.get()) {

                try {

                    Thread.sleep(interval);

                    requestRemoteAdvertisements();

                    publishAdvertisement();

                    checkAdvertisementResponses();

                    expireInacticeRuntimes();

                } catch (InterruptedException ex) {
                    return;
                } catch (IOException e) {
                    // TODO Notify the monitor
                }
            }

        }



    }

    /*
     * Request advertisements from remote nodes.
     */
    private void requestRemoteAdvertisements() {
        discoveryService.getRemoteAdvertisements(null, ADV, null, null, 5);
    }

    /*
     * Publishes advertisements about the current node.
     */
    private void publishAdvertisement() throws IOException {

        String type = PresenceAdvertisement.getAdvertisementType();
        PresenceAdvertisement presenceAdv = (PresenceAdvertisement) AdvertisementFactory.newAdvertisement(type);

        RuntimeInfo runtimeInfo = runtimeInfoService.getRuntimeInfo();
        presenceAdv.setRuntimeInfo(runtimeInfo);

        discoveryService.publish(presenceAdv, 10000, 10000);

    }

    /*
     * Checks responses to remote advertisement requests in local cache.
     */
    private void checkAdvertisementResponses() throws IOException {

        Enumeration en = discoveryService.getLocalAdvertisements(ADV, null, null);

        while (en != null && en.hasMoreElements()) {

            Object object = en.nextElement();
            if (object instanceof PresenceAdvertisement) {

                PresenceAdvertisement presenceAdv1 = (PresenceAdvertisement) object;
                RuntimeInfo runtimeInfo1 = presenceAdv1.getRuntimeInfo();

                if (runtimeInfo1.getId().equals(hostInfo.getRuntimeId())) {
                    continue;
                }

                participatingRuntimes.put(runtimeInfo1.getId(),
                                          new TwosTuple<RuntimeInfo, Long>(runtimeInfo1,
                                                                           System.currentTimeMillis()));
            }

        }

    }

    /*
     * Expires inactive runtimes.
     */
    private void expireInacticeRuntimes() {

        for (TwosTuple<RuntimeInfo, Long> tuple : participatingRuntimes.values()) {
            if (System.currentTimeMillis() - tuple.getSecond() > expirationThreshold) {
                participatingRuntimes.remove(tuple.getFirst().getId());
            } else {
                System.err.println(hostInfo.getRuntimeId() + ":" + tuple.getFirst().getId());
            }

        }

    }

}
