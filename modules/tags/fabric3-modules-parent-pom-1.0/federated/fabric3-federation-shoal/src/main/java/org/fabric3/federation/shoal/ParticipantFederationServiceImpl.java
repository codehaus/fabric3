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

import java.util.Map;
import java.util.Properties;

import com.sun.enterprise.ee.cms.core.GMSConstants;
import com.sun.enterprise.ee.cms.core.GMSException;
import com.sun.enterprise.ee.cms.core.GMSFactory;
import com.sun.enterprise.ee.cms.core.GroupManagementService;
import static com.sun.enterprise.ee.cms.core.GroupManagementService.MemberType.CORE;
import com.sun.enterprise.ee.cms.impl.client.FailureNotificationActionFactoryImpl;
import com.sun.enterprise.ee.cms.impl.client.JoinNotificationActionFactoryImpl;
import com.sun.enterprise.ee.cms.impl.client.JoinedAndReadyNotificationActionFactoryImpl;
import com.sun.enterprise.ee.cms.impl.client.PlannedShutdownActionFactoryImpl;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.services.event.EventService;
import org.fabric3.spi.topology.RuntimeService;

/**
 * Manages federated communications using Shoal.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
@Service(interfaces = {RuntimeService.class, ParticipantFederationService.class})
public class ParticipantFederationServiceImpl extends AbstractFederationService implements RuntimeService, ParticipantFederationService {
    private GroupManagementService zoneGMS;

    /**
     * Constructor
     *
     * @param eventService the runtime event service
     * @param info         the host runtime information
     * @param monitor      the monitor for controller events
     */
    public ParticipantFederationServiceImpl(@Reference EventService eventService,
                                            @Reference HostInfo info,
                                            @Monitor FederationServiceMonitor monitor) {
        super(eventService, info, monitor);
    }

    public GroupManagementService getZoneGMS() {
        return zoneGMS;
    }

    public String getZoneName() {
        return zoneName;
    }

    public synchronized void enableDomainCommunications() {
        try {
            initializeDomainCommunications(initializeProperties());
            for (FederationCallback callback : callbacks.values()) {
                callback.afterJoin();
            }
        } catch (GMSException e) {
            monitor.onException("An error was raised joining the group", domainName, e);
        } catch (FederationCallbackException e) {
            monitor.onException("An error was raised joining the group", domainName, e);
        }
    }

    protected synchronized void onStartCommunications(Properties properties) {
        try {
            zoneGMS = (GroupManagementService) GMSFactory.startGMSModule(runtimeName, zoneName, CORE, properties);
            initializeCallbacks(zoneGMS);
            zoneGMS.join();
            zoneGMS.reportJoinedAndReadyState(zoneName);
            monitor.joined(zoneName, runtimeName);
            initializeDomainCommunications(properties);
            for (FederationCallback callback : callbacks.values()) {
                callback.afterJoin();
            }
        } catch (GMSException e) {
            monitor.onException("An error was raised joining the group", domainName, e);
        } catch (FederationCallbackException e) {
            monitor.onException("An error was raised joining the group", domainName, e);
        }
    }

    protected synchronized void onStopCommunications() {
        try {
            if (zoneGMS != null) {
                zoneGMS.shutdown(GMSConstants.shutdownType.INSTANCE_SHUTDOWN);
                for (FederationCallback callback : callbacks.values()) {
                    callback.afterJoin();
                }
                monitor.exited(zoneName);
            }
        } catch (FederationCallbackException e) {
            monitor.onException("An error was raised joining the group", domainName, e);
        }
    }

    private synchronized void initializeDomainCommunications(Properties properties) throws GMSException {
        if (domainGMS != null) {
            return;
        }
        // join the domain group after the zone group has joined
        if (zoneGMS.getGroupHandle().isGroupLeader()) {
            domainGMS = (GroupManagementService) GMSFactory.startGMSModule(zoneName, domainName, CORE, properties);
            monitor.joined(domainName, zoneName);
            initializeCallbacks(domainGMS);
            domainGMS.join();
            domainGMS.reportJoinedAndReadyState(domainName);
        }
    }

    /**
     * Initializes callbacks for a GMS. Note that currently the same callbacks are registered for the domain and zone GMS instances. However, in the
     * future this will likely change as the domain GMS may need to handle recovery cases. Hence, this method is not included as part of
     * AbstractFederationService.
     *
     * @param gms the GMS to initialize.
     */
    private void initializeCallbacks(GroupManagementService gms) {
        SignalBroadcaster broadcaster = new SignalBroadcaster(callbacks.values(), monitor);
        gms.addActionFactory(new PlannedShutdownActionFactoryImpl(broadcaster));
        gms.addActionFactory(new JoinNotificationActionFactoryImpl(broadcaster));
        gms.addActionFactory(new FailureNotificationActionFactoryImpl(broadcaster));
        gms.addActionFactory(new JoinedAndReadyNotificationActionFactoryImpl(broadcaster));
        for (Map.Entry<String, FederationCallback> entry : callbacks.entrySet()) {
            String serviceName = entry.getKey();
            // TODO handle failure recovery
            // DispatchingFailureRecoveryActionFactory factory =new DispatchingFailureRecoveryActionFactory(serviceName, entry.getValue(), monitor)
            // gms.addActionFactory(serviceName, factory, serviceName));
            DispatchingMessageActionFactory factory = new DispatchingMessageActionFactory(serviceName, entry.getValue(), monitor);
            gms.addActionFactory(factory, serviceName);
        }
    }

}