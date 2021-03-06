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
package org.fabric3.federation.executor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.federation.command.RuntimeDeploymentCommand;
import org.fabric3.federation.command.ZoneDeploymentCommand;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.topology.MessageException;
import org.fabric3.spi.topology.RuntimeInstance;
import org.fabric3.spi.topology.RuntimeService;
import org.fabric3.spi.topology.ZoneManager;
import org.fabric3.spi.classloader.MultiClassLoaderObjectOutputStream;

/**
 * Processes a ZoneDeploymentCommand and sends a corresponding RuntimeDeploymentCommand to all runtimes in a zone.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class ZoneDeploymentCommandExecutor implements CommandExecutor<ZoneDeploymentCommand> {
    private ZoneManager zoneManager;
    private CommandExecutorRegistry executorRegistry;
    private RuntimeService runtimeService;
    private ZoneDeploymentCommandExecutorMonitor monitor;

    public ZoneDeploymentCommandExecutor(@Reference ZoneManager zoneManager,
                                         @Reference CommandExecutorRegistry executorRegistry,
                                         @Reference RuntimeService runtimeService,
                                         @Monitor ZoneDeploymentCommandExecutorMonitor monitor) {
        this.zoneManager = zoneManager;
        this.executorRegistry = executorRegistry;
        this.runtimeService = runtimeService;
        this.monitor = monitor;
    }

    @Init
    public void init() {
        executorRegistry.register(ZoneDeploymentCommand.class, this);
    }

    public void execute(ZoneDeploymentCommand command) throws ExecutionException {
        try {
            String id = command.getId();
            monitor.receivedDeploymentCommand(id);
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            MultiClassLoaderObjectOutputStream stream = new MultiClassLoaderObjectOutputStream(bas);
            Set<Command> commands = command.getCommands();
            RuntimeDeploymentCommand runtimeCommand = new RuntimeDeploymentCommand(id, commands);
            stream.writeObject(runtimeCommand);
            stream.close();
            byte[] serialized = bas.toByteArray();
            String runtimeName = runtimeService.getRuntimeName();
            if (zoneManager.getRuntimes().size() == 1 && !runtimeService.isComponentHost()) {
                throw new NoTargetRuntimeException("No deployment runtime found. Note the zone manager is configured not to host components.");
            }
            for (RuntimeInstance runtime : zoneManager.getRuntimes()) {
                String target = runtime.getName();
                if (runtimeName.equals(target)) {
                    // deploy locally if this runtime host components
                    if (runtimeService.isComponentHost()) {
                        monitor.routed(target, id);
                        for (Command cmd : commands) {
                            executorRegistry.execute(cmd);
                        }
                    }
                } else {
                    // deploy to the runtime
                    zoneManager.sendMessage(target, serialized);
                    monitor.routed(target, id);
                }
            }
        } catch (IOException e) {
            throw new ExecutionException(e);
        } catch (MessageException e) {
            throw new ExecutionException(e);
        }
    }
}
