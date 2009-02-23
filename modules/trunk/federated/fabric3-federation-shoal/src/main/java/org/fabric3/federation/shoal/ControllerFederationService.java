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
 * Manages federated communications using Shoal on a controller instance.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
@Service(interfaces = {RuntimeService.class, FederationService.class})
public class ControllerFederationService extends AbstractFederationService implements RuntimeService, FederationService {

    /**
     * Constructor
     *
     * @param eventService the runtime event service
     * @param info         the host runtime information
     * @param monitor      the monitor for controller events
     */
    public ControllerFederationService(@Reference EventService eventService, @Reference HostInfo info, @Monitor FederationServiceMonitor monitor) {
        super(eventService, info, monitor);
    }

    protected synchronized void onStartCommunications(Properties properties) {
        try {
            domainGMS = (GroupManagementService) GMSFactory.startGMSModule(runtimeName, domainName, CORE, properties);
            monitor.joined(domainName, runtimeName);
            initializeCallbacks(domainGMS);
            domainGMS.join();
            domainGMS.reportJoinedAndReadyState(domainName);
            for (FederationCallback callback : callbacks.values()) {
                callback.afterJoin();
            }
        } catch (GMSException e) {
            monitor.onException("An error was raised joining the group", domainName, e);
        } catch (FederationCallbackException e) {
            monitor.onException("An error was raised joining the group", domainName, e);
        }
    }

    /**
     * Callback when the runtime shuts down.
     */
    protected synchronized void onStopCommunications() {
        try {
            if (domainGMS != null) {
                domainGMS.shutdown(GMSConstants.shutdownType.INSTANCE_SHUTDOWN);
                for (FederationCallback callback : callbacks.values()) {
                    callback.onLeave();
                }
                monitor.exited(domainName);
            }
        } catch (FederationCallbackException e) {
            monitor.onException("An error was raised joining the group", domainName, e);
        }
    }


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