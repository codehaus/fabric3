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
import org.fabric3.federation.event.RuntimeSynchronized;
import org.fabric3.spi.classloader.MultiClassLoaderObjectOutputStream;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.services.event.EventService;
import org.fabric3.spi.topology.MessageException;
import org.fabric3.spi.topology.RuntimeInstance;
import org.fabric3.spi.topology.RuntimeService;
import org.fabric3.spi.topology.ZoneManager;

/**
 * Processes a ZoneDeploymentCommand. This may result in routing the command locally, to an individual runtime, or to  all runtimes in a zone
 * depending on the correlation semantics.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class ZoneDeploymentCommandExecutor implements CommandExecutor<ZoneDeploymentCommand> {
    private ZoneManager zoneManager;
    private CommandExecutorRegistry executorRegistry;
    private RuntimeService runtimeService;
    private EventService eventService;
    private ZoneDeploymentCommandExecutorMonitor monitor;
    private boolean domainSynchronized;

    public ZoneDeploymentCommandExecutor(@Reference ZoneManager zoneManager,
                                         @Reference CommandExecutorRegistry executorRegistry,
                                         @Reference RuntimeService runtimeService,
                                         @Reference EventService eventService,
                                         @Monitor ZoneDeploymentCommandExecutorMonitor monitor) {
        this.zoneManager = zoneManager;
        this.executorRegistry = executorRegistry;
        this.runtimeService = runtimeService;
        this.eventService = eventService;
        this.monitor = monitor;
    }

    @Init
    public void init() {
        executorRegistry.register(ZoneDeploymentCommand.class, this);
    }

    public void execute(ZoneDeploymentCommand command) throws ExecutionException {
        String correlationId = command.getCorrelationId();
        if (correlationId != null) {
            // the command is destined to a specific runtime
            routeToRuntime(command);
        } else {
            // route the command to all runtimes in the zone
            routeToZone(command);
        }
        domainSynchronized = true;
    }

    private void routeToRuntime(ZoneDeploymentCommand command) throws ExecutionException {
        String correlationId = command.getCorrelationId();
        String runtimeName = runtimeService.getRuntimeName();
        // route the command to a specific runtime
        if (correlationId.equals(runtimeName)) {
            if ((domainSynchronized && command.isSynchronization())) {
                // the zone is already synchronized, ignore as this may be a duplicate
                return;
            }
            routeLocally(command);
        } else {
            String id = command.getId();
            boolean routed = false;
            for (RuntimeInstance runtime : zoneManager.getRuntimes()) {
                String target = runtime.getName();
                if (target.equals(correlationId)) {
                    // deploy to the runtime
                    try {
                        byte[] serialized = serializeRuntimeCommand(command);
                        zoneManager.sendMessage(target, serialized);
                    } catch (IOException e) {
                        throw new ExecutionException(e);
                    } catch (MessageException e) {
                        throw new ExecutionException(e);
                    }
                    monitor.routed(target, id);
                    routed = true;
                }
            }
            if (!routed) {
                throw new NoTargetRuntimeException("Runtime " + runtimeName + " not found for deployment command: " + id);
            }
        }

    }

    private void routeToZone(ZoneDeploymentCommand command) throws ExecutionException {
        String runtimeName = runtimeService.getRuntimeName();
        // route the command to all runtimes in the zone
        for (RuntimeInstance runtime : zoneManager.getRuntimes()) {
            String target = runtime.getName();
            if (runtimeName.equals(target)) {
                routeLocally(command);
            } else {
                try {
                    // deploy to the runtime
                    byte[] serialized = serializeRuntimeCommand(command);
                    zoneManager.sendMessage(target, serialized);
                } catch (IOException e) {
                    throw new ExecutionException(e);
                } catch (MessageException e) {
                    throw new ExecutionException(e);
                }
                String id = command.getId();
                monitor.routed(target, id);
            }
        }

    }

    private void routeLocally(ZoneDeploymentCommand command) throws ExecutionException {
        String id = command.getId();
        Set<Command> commands = command.getCommands();
        // command destined for this runtime
        String runtimeName = runtimeService.getRuntimeName();
        monitor.routed(runtimeName, id);
        for (Command cmd : commands) {
            executorRegistry.execute(cmd);
        }
        eventService.publish(new RuntimeSynchronized());
    }

    private byte[] serializeRuntimeCommand(ZoneDeploymentCommand command) throws IOException {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        MultiClassLoaderObjectOutputStream stream = new MultiClassLoaderObjectOutputStream(bas);
        String id = command.getId();
        Set<Command> commands = command.getCommands();
        boolean synchronization = command.isSynchronization();
        RuntimeDeploymentCommand runtimeCommand = new RuntimeDeploymentCommand(id, commands, synchronization);
        stream.writeObject(runtimeCommand);
        stream.close();
        return bas.toByteArray();
    }

}
