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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.sun.enterprise.ee.cms.core.MessageSignal;
import com.sun.enterprise.ee.cms.core.Signal;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import static org.fabric3.federation.shoal.FederationConstants.RUNTIME_MANAGER;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.topology.RuntimeManager;
import org.fabric3.spi.classloader.MultiClassLoaderObjectInputStream;

/**
 * Handles communications between a zone participant and the ZoneManager. Specifically, executes commands received from the ZoneManager and updates
 * the zone distributed cache with runtime metadata.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class ShoalRuntimeManager implements RuntimeManager, FederationCallback {
//    private Map<QName, Serializable> runtimeMetdata = new HashMap<QName, Serializable>();
    private FederationService federationService;
    private CommandExecutorRegistry executorRegistry;
    private ClassLoaderRegistry classLoaderRegistry;

    /**
     * Constructor.
     *
     * @param federationService   the service responsible for managing domain runtime communications
     * @param executorRegistry    the command executor registry used to dispatch commands received from a zone manager
     * @param classLoaderRegistry the classloader registry used for deserializing command messages
     */
    public ShoalRuntimeManager(@Reference FederationService federationService,
                               @Reference CommandExecutorRegistry executorRegistry,
                               @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.federationService = federationService;
        this.executorRegistry = executorRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Init
    public void init() {
        federationService.registerZoneCallback(RUNTIME_MANAGER, this);
    }

    public void afterJoin() throws FederationCallbackException {

    }

    public void onLeave() {

    }

    public void onSignal(Signal signal) throws FederationCallbackException {
        if (signal instanceof MessageSignal) {
            executeCommand((MessageSignal) signal);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void executeCommand(MessageSignal signal) throws FederationCallbackException {
        MultiClassLoaderObjectInputStream ois = null;
        try {
            byte[] payload = signal.getMessage();
            InputStream stream = new ByteArrayInputStream(payload);
            // Deserialize the command. As command classes may be loaded in an extension classloader, use a MultiClassLoaderObjectInputStream
            // to deserialize classes in the appropriate classloader.
            ois = new MultiClassLoaderObjectInputStream(stream, classLoaderRegistry);
            Command command = (Command) ois.readObject();
            executorRegistry.execute(command);
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

}