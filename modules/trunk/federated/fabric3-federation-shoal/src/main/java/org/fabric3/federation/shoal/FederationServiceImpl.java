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
package org.fabric3.federation.shoal;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.ee.cms.core.GMSConstants;
import com.sun.enterprise.ee.cms.core.GMSException;
import com.sun.enterprise.ee.cms.core.GMSFactory;
import com.sun.enterprise.ee.cms.core.GroupManagementService;
import static com.sun.enterprise.ee.cms.core.GroupManagementService.MemberType.CORE;
import com.sun.enterprise.ee.cms.impl.client.FailureNotificationActionFactoryImpl;
import com.sun.enterprise.ee.cms.impl.client.JoinNotificationActionFactoryImpl;
import com.sun.enterprise.ee.cms.impl.client.JoinedAndReadyNotificationActionFactoryImpl;
import com.sun.enterprise.ee.cms.impl.client.PlannedShutdownActionFactoryImpl;
import com.sun.enterprise.ee.cms.impl.common.GMSConfigConstants;
import com.sun.enterprise.ee.cms.logging.GMSLogDomain;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.services.event.EventService;
import org.fabric3.spi.services.event.Fabric3EventListener;
import org.fabric3.spi.services.event.JoinDomain;
import org.fabric3.spi.services.event.RuntimeStop;

/**
 * Manages federated communications using Shoal.
 *
 * @version $Revision$ $Date$
 */

public class FederationServiceImpl implements FederationService {
    // configuration properties
    private String zoneName = "zone";
    private String domainName = "domain";
    private String multicastAddress;
    private String multicastPort;
    private String fdTimeout;
    private String fdMaxRetries;
    private String mergeMaxInterval;
    private String mergeMinInterval;
    private String vsTimeout;
    private String pingTimeout;
    private String runtimeName;

    private EventService eventService;
    private FederationServiceMonitor monitor;
    private boolean enableDomain;

    private GroupManagementService domainGMS;
    private GroupManagementService zoneGMS;
    private Map<String, FederationCallback> zoneCallbacks = new HashMap<String, FederationCallback>();
    private Map<String, FederationCallback> domainCallbacks = new HashMap<String, FederationCallback>();

    @Property
    public void setMulticastAddress(String multicastAddress) {
        this.multicastAddress = multicastAddress;
    }

    @Property
    public void setMulticastPort(String multicastPort) {
        this.multicastPort = multicastPort;
    }

    @Property
    public void setFdTimeout(String fdTimeout) {
        this.fdTimeout = fdTimeout;
    }

    @Property
    public void setFdMaxRetries(String fdMaxRetries) {
        this.fdMaxRetries = fdMaxRetries;
    }

    @Property
    public void setMergeMaxInterval(String mergeMaxInterval) {
        this.mergeMaxInterval = mergeMaxInterval;
    }

    @Property
    public void setMergeMinInterval(String mergeMinInterval) {
        this.mergeMinInterval = mergeMinInterval;
    }

    @Property
    public void setVsTimeout(String vsTimeout) {
        this.vsTimeout = vsTimeout;
    }

    @Property
    public void setPingTimeout(String pingTimeout) {
        this.pingTimeout = pingTimeout;
    }

    @Property
    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    @Property
    public void setRuntimeName(String runtimeName) {
        this.runtimeName = runtimeName;
    }

    @Property
    public void setEnableDomain(boolean enableDomain) {
        this.enableDomain = enableDomain;
    }


    /**
     * Constructor
     *
     * @param eventService the runtime event service
     * @param info         the host runtime information
     * @param monitor      the monitor for controller events
     */
    public FederationServiceImpl(EventService eventService, HostInfo info, FederationServiceMonitor monitor) {
        this.eventService = eventService;
        this.monitor = monitor;
        domainName = info.getDomain().getAuthority();
    }

    @Init
    public void init() {
        if (runtimeName == null) {
            runtimeName = "Fabric3Runtime-" + UUID.randomUUID().toString();
        }
        initializeLogger();
        // setup runtime notifications
        eventService.subscribe(JoinDomain.class, new JoinDomainEventListener());
        eventService.subscribe(RuntimeStop.class, new RuntimeStopEventListener());

    }

    public String getDomainName() {
        return domainName;
    }

    public String getZoneName() {
        return zoneName;
    }

    public String getRuntimeName() {
        return runtimeName;
    }

    public GroupManagementService getDomainGMS() {
        return domainGMS;
    }

    public GroupManagementService getZoneGMS() {
        return zoneGMS;
    }

    public void registerDomainCallback(String serviceName, FederationCallback callback) {
        domainCallbacks.put(serviceName, callback);
    }

    public void registerZoneCallback(String serviceName, FederationCallback callback) {
        zoneCallbacks.put(serviceName, callback);
    }

    /**
     * Callback when the runtime joins the domain.
     */
    void onJoinDomain() {
        try {
            initializeGMS();
            if (enableDomain) {
                domainGMS.join();
                for (FederationCallback callback : domainCallbacks.values()) {
                    callback.afterJoin();
                }
                domainGMS.reportJoinedAndReadyState(domainName);
                monitor.joined(domainName);
            }
            zoneGMS.join();
            for (FederationCallback callback : zoneCallbacks.values()) {
                callback.afterJoin();
            }
            zoneGMS.reportJoinedAndReadyState(zoneName);
            monitor.joined(zoneName);
        } catch (GMSException e) {
            monitor.onException("An error was raised joining the group", domainName, e);
        } catch (FederationCallbackException e) {
            monitor.onException("An error was raised joining the group", domainName, e);
        }
    }

    /**
     * Callback when the runtime shuts down.
     */
    void onStopRuntime() {
        try {
            if (domainGMS != null) {
                domainGMS.shutdown(GMSConstants.shutdownType.INSTANCE_SHUTDOWN);
                for (FederationCallback callback : domainCallbacks.values()) {
                    callback.onLeave();
                }
                monitor.exited(domainName);
            }
            if (zoneGMS != null) {
                zoneGMS.shutdown(GMSConstants.shutdownType.INSTANCE_SHUTDOWN);
                for (FederationCallback callback : zoneCallbacks.values()) {
                    callback.onLeave();
                }
                monitor.exited(zoneName);
            }
        } catch (FederationCallbackException e) {
            monitor.onException("An error was raised joining the group", domainName, e);
        }
    }

    /**
     * Redirects logging to the F3 monitor framework.
     */
    private void initializeLogger() {
        Logger logger = GMSLogDomain.getLogger(GMSLogDomain.GMS_LOGGER);
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        logger.addHandler(new MonitorLogHandler(monitor));
    }

    private void initializeGMS() {
        Properties properties = initializeProperties();

        // join the zone group first
        zoneGMS = (GroupManagementService) GMSFactory.startGMSModule(runtimeName, zoneName, CORE, properties);
        initializeZoneCallbacks(zoneGMS);
        if (enableDomain) {
            // join the domain group after the zone group has joined
            domainGMS = (GroupManagementService) GMSFactory.startGMSModule(runtimeName, domainName, CORE, properties);
            initializeDomainCallbacks(domainGMS);
        }
    }

    /**
     * Initializes the network configuration properties.
     *
     * @return the configuration properties
     */
    private Properties initializeProperties() {
        Properties properties = new Properties();
        if (multicastAddress != null) {
            properties.put(GMSConfigConstants.MULTICAST_ADDRESS, multicastAddress);
        }
        if (multicastPort != null) {
            properties.put(GMSConfigConstants.MULTICAST_PORT, multicastPort);
        }
        if (fdTimeout != null) {
            properties.put(GMSConfigConstants.FD_TIMEOUT, fdTimeout);
        }
        if (fdMaxRetries != null) {
            properties.put(GMSConfigConstants.FD_MAX_RETRIES, fdMaxRetries);
        }
        if (mergeMaxInterval != null) {
            properties.put(GMSConfigConstants.MERGE_MAX_INTERVAL, mergeMaxInterval);
        }
        if (mergeMinInterval != null) {
            properties.put(GMSConfigConstants.MERGE_MIN_INTERVAL, mergeMinInterval);
        }
        if (vsTimeout != null) {
            properties.put(GMSConfigConstants.VS_TIMEOUT, vsTimeout);
        }
        if (pingTimeout != null) {
            properties.put(GMSConfigConstants.PING_TIMEOUT, pingTimeout);
        }
        return properties;
    }


    private void initializeDomainCallbacks(GroupManagementService gms) {
        SignalBroadcaster broadcaster = new SignalBroadcaster(domainCallbacks.values(), monitor);
        gms.addActionFactory(new PlannedShutdownActionFactoryImpl(broadcaster));
        gms.addActionFactory(new JoinNotificationActionFactoryImpl(broadcaster));
        gms.addActionFactory(new FailureNotificationActionFactoryImpl(broadcaster));
        gms.addActionFactory(new JoinedAndReadyNotificationActionFactoryImpl(broadcaster));
        for (Map.Entry<String, FederationCallback> entry : domainCallbacks.entrySet()) {
            String serviceName = entry.getKey();
            // TODO handle failure recovery
            // DispatchingFailureRecoveryActionFactory factory =new DispatchingFailureRecoveryActionFactory(serviceName, entry.getValue(), monitor)
            // gms.addActionFactory(serviceName, factory, serviceName));
            DispatchingMessageActionFactory factory = new DispatchingMessageActionFactory(serviceName, entry.getValue(), monitor);
            gms.addActionFactory(factory, serviceName);
        }
    }

    private void initializeZoneCallbacks(GroupManagementService gms) {
        SignalBroadcaster broadcaster = new SignalBroadcaster(zoneCallbacks.values(), monitor);
        gms.addActionFactory(new PlannedShutdownActionFactoryImpl(broadcaster));
        gms.addActionFactory(new FailureNotificationActionFactoryImpl(broadcaster));
        gms.addActionFactory(new JoinedAndReadyNotificationActionFactoryImpl(broadcaster));
        for (Map.Entry<String, FederationCallback> entry : zoneCallbacks.entrySet()) {
            String serviceName = entry.getKey();
            // TODO handle failure recovery
            // DispatchingFailureRecoveryActionFactory factory =new DispatchingFailureRecoveryActionFactory(serviceName, entry.getValue(), monitor)
            // gms.addActionFactory(serviceName, factory, serviceName));
            DispatchingMessageActionFactory factory = new DispatchingMessageActionFactory(serviceName, entry.getValue(), monitor);
            gms.addActionFactory(factory, serviceName);
        }
    }

    /**
     * Listener for when the runtime enters the join domain bootstrap phase.
     */
    private class JoinDomainEventListener implements Fabric3EventListener<JoinDomain> {

        public void onEvent(JoinDomain event) {
            onJoinDomain();
        }
    }

    /**
     * Listener for when the runtime shuts down.
     */
    private class RuntimeStopEventListener implements Fabric3EventListener<RuntimeStop> {

        public void onEvent(RuntimeStop event) {
            onStopRuntime();
        }
    }

}