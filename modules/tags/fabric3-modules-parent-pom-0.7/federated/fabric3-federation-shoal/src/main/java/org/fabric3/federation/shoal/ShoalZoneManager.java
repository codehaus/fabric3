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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.enterprise.ee.cms.core.GMSException;
import com.sun.enterprise.ee.cms.core.MessageSignal;
import com.sun.enterprise.ee.cms.core.Signal;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import static org.fabric3.federation.shoal.FederationConstants.RUNTIME_MANAGER;
import static org.fabric3.federation.shoal.FederationConstants.ZONE_TRANSPORT_INFO;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.topology.MessageException;
import org.fabric3.spi.topology.RuntimeInstance;
import org.fabric3.spi.topology.ZoneManager;
import org.fabric3.spi.classloader.MultiClassLoaderObjectInputStream;

/**
 * Manages communications between a zone and the DomainManager. As communications are segmented between domain-wide messages and zone-specific
 * messages, communication with the DomainManager is done using one Shoal group while communications with zone participants is done using a separate
 * Shoal group.
 * <p/>
 * This implementation executes commands received from the DomainManager and syncronizes the domain-wide distributed cache with runtime metadata
 * cached at the zone level.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class ShoalZoneManager implements ZoneManager, FederationCallback {
    private FederationService federationService;
    private CommandExecutorRegistry executorRegistry;
    private ClassLoaderRegistry classLoaderRegistry;
    private ZoneManagerMonitor monitor;
    private boolean zoneManager;
    private Map<String, String> transportMetadata;

    /**
     * Constructor.
     *
     * @param federationService   the service responsible for managing domain runtime communications
     * @param executorRegistry    the command executor registry for handling commands contained in messages
     * @param classLoaderRegistry the classloader registry used during deserialization of message payloads. Payloads may contain types defined in
     *                            runtime extension classloaders.
     * @param monitor             the monitor to report runtime events to
     */
    public ShoalZoneManager(@Reference FederationService federationService,
                            @Reference CommandExecutorRegistry executorRegistry,
                            @Reference ClassLoaderRegistry classLoaderRegistry,
                            @Monitor ZoneManagerMonitor monitor) {
        this.federationService = federationService;
        this.executorRegistry = executorRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
        this.monitor = monitor;
    }

    /**
     * Property indicating if the current runtime is a zone manager. The default is false.
     *
     * @param zoneManager if the current runtime is a zone manager
     */
    @Property
    public void setZoneManager(boolean zoneManager) {
        this.zoneManager = zoneManager;
    }


    /**
     * Property to set the transport metadata. This may contain information such as the cluster HTTP address.
     *
     * @param transportMetadata the transport metadata keyed by type.
     */
    @Property
    public void setTransportMetadata(Map<String, String> transportMetadata) {
        this.transportMetadata = transportMetadata;
    }

    @Init
    public void init() {
        if (!zoneManager) {
            // the runtime is not a zone manager, don't initialize zone manager
            return;
        }
        federationService.registerZoneCallback(FederationConstants.ZONE_MANAGER, this);
        federationService.registerDomainCallback(FederationConstants.ZONE_MANAGER, this);
        monitor.enabled(federationService.getZoneName());
    }

    public List<RuntimeInstance> getRuntimes() {
        List<String> members = federationService.getZoneGMS().getGroupHandle().getCurrentCoreMembers();
        List<RuntimeInstance> runtimes = new ArrayList<RuntimeInstance>(members.size());
        for (String member : members) {
            runtimes.add(new RuntimeInstance(member));
        }
        return runtimes;
    }

    public void afterJoin() throws FederationCallbackException {
        updateZoneMetaData();
    }

    public void onLeave() throws FederationCallbackException {
    }

    public void sendMessage(String runtimeName, byte[] message) throws MessageException {
        try {
            federationService.getZoneGMS().getGroupHandle().sendMessage(runtimeName, RUNTIME_MANAGER, message);
        } catch (GMSException e) {
            throw new MessageException(e);
        }
    }

    public void onSignal(Signal signal) throws FederationCallbackException {
        if (federationService.getZoneName().equals(signal.getGroupName())) {
            handleZoneSignal(signal);
        } else if (federationService.getDomainName().equals(signal.getGroupName())) {
            handleDomainSignal(signal);
        }
    }

    private void handleZoneSignal(Signal signal) throws FederationCallbackException {

    }

    private void handleDomainSignal(Signal signal) throws FederationCallbackException {
        if (signal instanceof MessageSignal) {
            handleMessage((MessageSignal) signal);
        }
    }

    private void handleMessage(MessageSignal signal) throws FederationCallbackException {
        MultiClassLoaderObjectInputStream ois = null;
        try {
            byte[] payload = signal.getMessage();
            InputStream stream = new ByteArrayInputStream(payload);
            // Deserialize the command set. As command set classes may be loaded in an extension classloader, use a MultiClassLoaderObjectInputStream
            // to deserialize classes in the appropriate classloader.
            ois = new MultiClassLoaderObjectInputStream(stream, classLoaderRegistry);
            Object deserialized = ois.readObject();
            if (deserialized instanceof Command) {
                executorRegistry.execute((Command) deserialized);
            }
        } catch (ExecutionException e) {
            throw new FederationCallbackException(e);
        } catch (IOException e) {
            throw new FederationCallbackException(e);
        } catch (ClassNotFoundException e) {
            throw new FederationCallbackException(e);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                // ignore;
            }
        }
    }

    private void updateZoneMetaData() throws FederationCallbackException {
        String zoneName = federationService.getRuntimeName();
        try {
            federationService.getDomainGMS().updateMemberDetails(zoneName, ZONE_TRANSPORT_INFO, (Serializable) transportMetadata);
        } catch (GMSException e) {
            throw new FederationCallbackException(e);
        }
    }

}
