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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.federation.command.RuntimeDeploymentCommand;
import org.fabric3.federation.event.RuntimeSynchronized;
import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiClassLoaderObjectInputStream;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.services.event.EventService;

/**
 * A CommandExecutor that processes deployment commands on a participant node.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class RuntimeDeploymentCommandExecutor implements CommandExecutor<RuntimeDeploymentCommand> {
    private CommandExecutorRegistry executorRegistry;
    private EventService eventService;
    private ScopeRegistry scopeRegistry;
    private RuntimeDeploymentCommandExecutorMonitor monitor;
    // indicates whether the runtime has been synchronized with the domain
    private boolean domainSynchronized;
    private ClassLoaderRegistry classLoaderRegistry;

    public RuntimeDeploymentCommandExecutor(@Reference CommandExecutorRegistry executorRegistry,
                                            @Reference EventService eventService,
                                            @Reference ScopeRegistry scopeRegistry,
                                            @Reference ClassLoaderRegistry classLoaderRegistry,
                                            @Monitor RuntimeDeploymentCommandExecutorMonitor monitor) {
        this.executorRegistry = executorRegistry;
        this.eventService = eventService;
        this.scopeRegistry = scopeRegistry;
        this.monitor = monitor;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Init
    public void init() {
        executorRegistry.register(RuntimeDeploymentCommand.class, this);
    }

    public void execute(RuntimeDeploymentCommand command) throws ExecutionException {
        if (domainSynchronized && command.isSynchronization()) {
            // When a participant boots, it periodiclly issues synchronization requests to the zone manager until the first deployment command is
            // received. Since communications are asynchronous, it is possible multiple requests may be issued if a response is not received during
            // the elapsed time period. If this happens, only the first deployment command should be processed.
            return;
        }
        String id = command.getId();
        monitor.receivedDeploymentCommand(id);

        // execute the extension commands first before deserializing the other commands as they may contain extension-specific metadata classes
        byte[] serializedExtensionCommands = command.getExtensionCommands();
        List<Command> extensionCommands = deserialize(serializedExtensionCommands);
        for (Command cmd : extensionCommands) {
            executorRegistry.execute(cmd);
        }
        try {
            scopeRegistry.getScopeContainer(Scope.COMPOSITE).reinject();
        } catch (InstanceLifecycleException e) {
            throw new ExecutionException(e);
        }

        byte[] serializedCommands = command.getCommands();
        List<Command> commands = deserialize(serializedCommands);
        for (Command cmd : commands) {
            executorRegistry.execute(cmd);
        }
        try {
            scopeRegistry.getScopeContainer(Scope.COMPOSITE).reinject();
        } catch (InstanceLifecycleException e) {
            throw new ExecutionException(e);
        }

        eventService.publish(new RuntimeSynchronized());
        domainSynchronized = true;
    }

    @SuppressWarnings({"unchecked"})
    private List<Command> deserialize(byte[] commands) throws ExecutionException {
        MultiClassLoaderObjectInputStream ois = null;
        try {
            InputStream stream = new ByteArrayInputStream(commands);
            // Deserialize the command set. As command set classes may be loaded in an extension classloader, use a MultiClassLoaderObjectInputStream
            // to deserialize classes in the appropriate classloader.
            ois = new MultiClassLoaderObjectInputStream(stream, classLoaderRegistry);
            return (List<Command>) ois.readObject();
        } catch (IOException e) {
            throw new ExecutionException(e);
        } catch (ClassNotFoundException e) {
            throw new ExecutionException(e);
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