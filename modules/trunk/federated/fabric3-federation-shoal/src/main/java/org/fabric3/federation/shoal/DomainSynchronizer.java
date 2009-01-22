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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.sun.enterprise.ee.cms.core.GMSException;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.federation.command.PaticipantSyncCommand;
import org.fabric3.federation.command.ZoneSyncCommand;
import org.fabric3.federation.event.RuntimeSynchronized;
import org.fabric3.spi.classloader.MultiClassLoaderObjectOutputStream;
import org.fabric3.spi.services.event.EventService;
import org.fabric3.spi.services.event.Fabric3Event;
import org.fabric3.spi.services.event.Fabric3EventListener;
import org.fabric3.spi.services.event.RuntimeStart;
import org.fabric3.spi.topology.RuntimeService;
import org.fabric3.spi.topology.ZoneManager;

/**
 * Responsible for synchronizing a participant or zone manager with the domain. If the node is a participant, a synchronization request will be sent
 * to the zone manager. If the node is a zone manager, the synchronization will be sent directly to the domain controller.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class DomainSynchronizer implements Runnable, Fabric3EventListener {
    private FederationService federationService;
    private ZoneManager zoneManager;
    private EventService eventService;
    private RuntimeService runtimeService;
    private DomainSynchronizerMonitor monitor;
    private ScheduledExecutorService executor;

    public DomainSynchronizer(@Reference FederationService federationService,
                              @Reference ZoneManager zoneManager,
                              @Reference EventService eventService,
                              @Reference RuntimeService runtimeService,
                              @Monitor DomainSynchronizerMonitor monitor) {
        this.federationService = federationService;
        this.zoneManager = zoneManager;
        this.eventService = eventService;
        this.runtimeService = runtimeService;
        this.monitor = monitor;
    }

    @Init
    public void init() {
        eventService.subscribe(RuntimeSynchronized.class, this);
        eventService.subscribe(RuntimeStart.class, this);
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    @Destroy
    public void destroy() {
        executor.shutdownNow();
    }

    public void run() {
        if (zoneManager.isZoneManager() && !runtimeService.isComponentHost()) {
            // the zone manager does not host components, do not send out a sync request
            return;
        }
        monitor.synchronizing();
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        MultiClassLoaderObjectOutputStream stream;
        try {
            stream = new MultiClassLoaderObjectOutputStream(bas);
            String name = federationService.getRuntimeName();
            if (zoneManager.isZoneManager()) {
                ZoneSyncCommand command = new ZoneSyncCommand(name, name);
                stream.writeObject(command);
                stream.close();
                // XCV FIXME avoid sending to all runtimes in the zone
                federationService.getDomainGMS().getGroupHandle().sendMessage(FederationConstants.DOMAIN_MANAGER, bas.toByteArray());
            } else {
                PaticipantSyncCommand command = new PaticipantSyncCommand(name);
                stream.writeObject(command);
                stream.close();
                // XCV FIXME avoid sending to all runtimes in the zone
                federationService.getZoneGMS().getGroupHandle().sendMessage(FederationConstants.ZONE_MANAGER, bas.toByteArray());
            }
        } catch (IOException e) {
            monitor.error(e);
        } catch (GMSException e) {
            monitor.error(e);
        }
    }

    public void onEvent(Fabric3Event event) {
        if (event instanceof RuntimeSynchronized) {
            executor.shutdownNow();
        } else if (event instanceof RuntimeStart) {
            executor.scheduleWithFixedDelay(this, 3000, 3000, TimeUnit.MILLISECONDS);
        }
    }
}
