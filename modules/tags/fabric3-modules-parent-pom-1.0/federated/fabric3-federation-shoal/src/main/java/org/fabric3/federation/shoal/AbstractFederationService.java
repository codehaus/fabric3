/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.federation.shoal;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.ee.cms.core.GroupManagementService;
import com.sun.enterprise.ee.cms.impl.common.GMSConfigConstants;
import com.sun.enterprise.ee.cms.logging.GMSLogDomain;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Service;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.services.event.EventService;
import org.fabric3.spi.services.event.Fabric3EventListener;
import org.fabric3.spi.services.event.JoinDomain;
import org.fabric3.spi.services.event.RuntimeStop;
import org.fabric3.spi.topology.RuntimeService;

/**
 * Base implementation of the FederationService based on Shoal.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
@Service(interfaces = {RuntimeService.class, FederationService.class})
public abstract class AbstractFederationService implements RuntimeService, FederationService {
    // configuration properties
    protected String zoneName = "default.zone";
    protected String domainName = "domain";
    protected String multicastAddress;
    protected String multicastPort;
    protected String fdTimeout;
    protected String fdMaxRetries;
    protected String mergeMaxInterval;
    protected String mergeMinInterval;
    protected String vsTimeout;
    protected String pingTimeout;
    protected String runtimeName;

    protected EventService eventService;
    protected FederationServiceMonitor monitor;

    protected GroupManagementService domainGMS;

    protected Map<String, FederationCallback> callbacks = new HashMap<String, FederationCallback>();
    private Level logLevel = Level.WARNING;

    @Property (required = false)
    public void setMulticastAddress(String multicastAddress) {
        this.multicastAddress = multicastAddress;
    }

    @Property (required = false)
    public void setMulticastPort(String multicastPort) {
        this.multicastPort = multicastPort;
    }

    @Property (required = false)
    public void setFdTimeout(String fdTimeout) {
        this.fdTimeout = fdTimeout;
    }

    @Property (required = false)
    public void setFdMaxRetries(String fdMaxRetries) {
        this.fdMaxRetries = fdMaxRetries;
    }

    @Property (required = false)
    public void setMergeMaxInterval(String mergeMaxInterval) {
        this.mergeMaxInterval = mergeMaxInterval;
    }

    @Property (required = false)
    public void setMergeMinInterval(String mergeMinInterval) {
        this.mergeMinInterval = mergeMinInterval;
    }

    @Property (required = false)
    public void setVsTimeout(String vsTimeout) {
        this.vsTimeout = vsTimeout;
    }

    @Property (required = false)
    public void setPingTimeout(String pingTimeout) {
        this.pingTimeout = pingTimeout;
    }

    @Property (required = false)
    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    @Property (required = false)
    public void setRuntimeName(String runtimeName) {
        this.runtimeName = runtimeName;
    }

    @Property (required = false)
    public void setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * Constructor
     *
     * @param eventService the runtime event service
     * @param info         the host runtime information
     * @param monitor      the monitor for controller events
     */
    public AbstractFederationService(EventService eventService, HostInfo info, FederationServiceMonitor monitor) {
        this.eventService = eventService;
        this.monitor = monitor;
        domainName = info.getDomain().getAuthority();
        File outputDir = new File(info.getTempDir(), "shoal");
        // set output to the tmp dir
        System.setProperty("JXTA_HOME", outputDir.getPath());
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

    public String getRuntimeName() {
        return runtimeName;
    }

    public GroupManagementService getDomainGMS() {
        return domainGMS;
    }

    public void registerCallback(String serviceName, FederationCallback callback) {
        callbacks.put(serviceName, callback);
    }

    /**
     * Callback to start federation communications.
     *
     * @param properties Shoal startup properties
     */
    protected abstract void onStartCommunications(Properties properties);

    /**
     * Callback to shutdown communications.
     */
    protected abstract void onStopCommunications();

    /**
     * Initializes the network configuration properties.
     *
     * @return the configuration properties
     */
    protected Properties initializeProperties() {
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

    /**
     * Redirects logging to the F3 monitor framework.
     */
    private void initializeLogger() {
        Logger logger = GMSLogDomain.getLogger(GMSLogDomain.GMS_LOGGER);
        logger.setUseParentHandlers(false);
        logger.setLevel(logLevel);
        logger.addHandler(new MonitorLogHandler(monitor));
    }

    /**
     * Listener for when the runtime enters the join domain bootstrap phase.
     */
    private class JoinDomainEventListener implements Fabric3EventListener<JoinDomain> {

        public void onEvent(JoinDomain event) {
            onStartCommunications(initializeProperties());
        }
    }

    /**
     * Listener for when the runtime shuts down.
     */
    private class RuntimeStopEventListener implements Fabric3EventListener<RuntimeStop> {

        public void onEvent(RuntimeStop event) {
            onStopCommunications();
        }
    }

}