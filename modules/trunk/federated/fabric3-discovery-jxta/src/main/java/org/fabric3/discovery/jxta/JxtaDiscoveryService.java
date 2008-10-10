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
package org.fabric3.discovery.jxta;

import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static net.jxta.discovery.DiscoveryService.ADV;
import net.jxta.document.AdvertisementFactory;
import net.jxta.peergroup.PeerGroup;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.host.work.DefaultPausableWork;
import org.fabric3.host.work.WorkScheduler;
import org.fabric3.jxta.JxtaService;
import org.fabric3.spi.model.topology.RuntimeInfo;
import org.fabric3.spi.services.discovery.DiscoveryService;
import org.fabric3.spi.services.runtime.RuntimeInfoService;

/**
 * JXTA implementation of the discovery service. <p/> <p/> The implementation uses the JXTA PDP to broadcast advertisements on the current node and
 * receive advertisements from the nodes participating in the same domain. Requests for advertisements from remote nodes and publishing advertisements
 * for the current node are performed in a different thread using the work scheduler. This is done every 2 seconds by default, however, this can be
 * configured through the <code>interval</code> property. </p> <p/> <p/> A remote node from which no advertisment has been received for the 10 seconds
 * by default is expelled from the current domain view. However, this can be overridden using the property <code>expirationThreshold</code>. </p> <p/>
 * <p/> The advertisements include serialized information on the <code>RuntimeInfo</code> of the node broadcasting the advertisement. Currently,
 * serialization is performed using xstream and transported using the description attribute of the peer discovery advertisement. However, it is worth
 * investigating using custom advertisements. </p> <p/> <p/> The discovery service is injected with the <code>AdvertisementService</code>. Services
 * within local nodes should use this service as a mediator for locally advertising their features. These features are then broadcasted to the wider
 * domain by the discovery service. </p>
 *
 * @version $Revsion$ $Date$
 */
@EagerInit
public class JxtaDiscoveryService implements DiscoveryService {
    private DiscoveryMonitor monitor;

    // Polling interval
    private long interval = 2000L;

    // Expiration threshold
    private long expirationThreshold = 1000000L;

    // JXTA Service
    private JxtaService jxtaService;

    // Work scheduler
    private WorkScheduler workScheduler;

    // Runtime info service
    private RuntimeInfoService runtimeInfoService;

    // Jxta discovery service
    private net.jxta.discovery.DiscoveryService discoveryService;

    // Publisher of advertismenets
    private Publisher publisher;

    // Participating runtimes
    private Map<URI, TwosTuple<RuntimeInfo, Long>> participatingRuntimes =
            new ConcurrentHashMap<URI, TwosTuple<RuntimeInfo, Long>>();

    public Set<RuntimeInfo> getParticipatingRuntimes() {
        Set<RuntimeInfo> ret = new HashSet<RuntimeInfo>();
        for (TwosTuple<RuntimeInfo, Long> tuple : participatingRuntimes.values()) {
            ret.add(tuple.getFirst());
        }
        return ret;
    }

    public RuntimeInfo getRuntimeInfo(URI runtimeId) {
        TwosTuple<RuntimeInfo, Long> pair = participatingRuntimes.get(runtimeId);
        if (pair == null) {
            return null;
        }
        return pair.getFirst();
    }

    @Monitor
    public void setMonitor(DiscoveryMonitor monitor) {
        this.monitor = monitor;
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
        publisher.stop();
    }

    /*
    * Notifier sending information about the current node.
    *
    */
    private class Publisher extends DefaultPausableWork {
    	
    	private Publisher() {
    		super(true);
    	}

        /*
         * Waits for the defined interval and sends advertisements for the
         * current node and discovery requests for the other nodes in the
         * domain.
         */
        public void execute() {

            try {

                Thread.sleep(interval);

                requestRemoteAdvertisements();

                publishAdvertisement();

                checkAdvertisementResponses();

                expireInacticeRuntimes();

            } catch (InterruptedException ex) {
                return;
            } catch (IOException e) {
                e.printStackTrace();
                // TODO Notify the monitor
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

                if (runtimeInfo1.getId().equals(runtimeInfoService.getCurrentRuntimeId())) {
                    continue;
                }

                long now = System.currentTimeMillis();
                TwosTuple<RuntimeInfo, Long> tuple = new TwosTuple<RuntimeInfo, Long>(runtimeInfo1, now);
                URI id = runtimeInfo1.getId();
                if (!participatingRuntimes.containsKey(id)) {
                    monitor.discovered(id.toString());
                }
                participatingRuntimes.put(id, tuple);
            }

        }

    }

    /*
     * Expires inactive runtimes.
     */
    private void expireInacticeRuntimes() {

        for (TwosTuple<RuntimeInfo, Long> tuple : participatingRuntimes.values()) {
            if (System.currentTimeMillis() - tuple.getSecond() > expirationThreshold) {
                URI id = tuple.getFirst().getId();
                participatingRuntimes.remove(id);
                monitor.expired(id.toString());
            }

        }

    }

}
