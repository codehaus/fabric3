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
package org.fabric3.fabric.executor;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.StopComponentCommand;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.services.componentmanager.RegistrationException;

@EagerInit
public class StopComponentCommandExecutor implements CommandExecutor<StopComponentCommand> {

    private final ComponentManager componentManager;
    private final CommandExecutorRegistry commandExecutorRegistry;

    public StopComponentCommandExecutor(@Reference ComponentManager componentManager,
                                        @Reference CommandExecutorRegistry commandExecutorRegistry) {
        this.componentManager = componentManager;
        this.commandExecutorRegistry = commandExecutorRegistry;
    }

    @Init
    public void init() {
        commandExecutorRegistry.register(StopComponentCommand.class, this);
    }

    public void execute(StopComponentCommand command) throws ExecutionException {
        URI uri = command.getUri();
        Component component = componentManager.getComponent(uri);
        component.stop();
        try {
            componentManager.unregister(component);
        } catch (RegistrationException re) {
            throw new ExecutionException("Unexpected exception unregistering component: " + uri, re);
        }
    }
}

