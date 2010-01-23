/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.federation.executor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.federation.command.DeploymentCommand;
import org.fabric3.federation.command.RuntimeUpdateCommand;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.spi.classloader.MultiClassLoaderObjectOutputStream;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.generator.CommandMap;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.Generator;
import org.fabric3.spi.generator.ZoneCommands;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Processes a {@link RuntimeUpdateCommand} on the controller by regenerating deployment commands for the current state of the zone.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ControllerRuntimeUpdateCommandExecutor implements CommandExecutor<RuntimeUpdateCommand> {
    private CommandExecutorRegistry executorRegistry;
    private RuntimeUpdateMonitor monitor;
    private LogicalComponentManager lcm;
    private Generator generator;

    public ControllerRuntimeUpdateCommandExecutor(@Reference(name = "lcm") LogicalComponentManager lcm,
                                                  @Reference(name = "generator") Generator generator,
                                                  @Reference CommandExecutorRegistry executorRegistry,
                                                  @Monitor RuntimeUpdateMonitor monitor) {
        this.lcm = lcm;
        this.generator = generator;
        this.executorRegistry = executorRegistry;
        this.monitor = monitor;
    }

    @Init
    public void init() {
        executorRegistry.register(RuntimeUpdateCommand.class, this);
    }

    public void execute(RuntimeUpdateCommand command) throws ExecutionException {
        try {
            monitor.updateRequest(command.getRuntimeName());
            ZoneCommands commands = regenerate(command.getZoneName());
            List<Command> extensionCommands = commands.getExtensionCommands();
            byte[] serializedExtensionCommands = serialize((Serializable) extensionCommands);
            List<Command> zoneCommands = commands.getCommands();
            byte[] serializedCommands = serialize((Serializable) zoneCommands);
            command.setResponse(new DeploymentCommand(serializedExtensionCommands, serializedCommands));
        } catch (DeploymentException e) {
            throw new ExecutionException(e);
        }
    }

    private ZoneCommands regenerate(String zoneId) throws DeploymentException {
        LogicalCompositeComponent domain = lcm.getRootComponent();
        Collection<LogicalComponent<?>> components = domain.getComponents();
        try {
            CommandMap commandMap = generator.generate(components, false);
            return commandMap.getZoneCommands(zoneId);
        } catch (GenerationException e) {
            throw new DeploymentException(e);
        }
    }

    private byte[] serialize(Serializable serializable) throws ExecutionException {
        try {
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            MultiClassLoaderObjectOutputStream stream = new MultiClassLoaderObjectOutputStream(bas);
            stream.writeObject(serializable);
            return bas.toByteArray();
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
    }
}