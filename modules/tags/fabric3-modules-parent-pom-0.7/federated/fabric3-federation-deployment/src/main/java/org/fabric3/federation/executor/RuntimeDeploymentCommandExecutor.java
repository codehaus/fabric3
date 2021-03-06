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

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.federation.command.RuntimeDeploymentCommand;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;

/**
 * Executes a RuntimeDeploymentCommand on a runtime.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class RuntimeDeploymentCommandExecutor implements CommandExecutor<RuntimeDeploymentCommand> {
    private CommandExecutorRegistry executorRegistry;
    private RuntimeDeploymentCommandExecutorMonitor monitor;

    public RuntimeDeploymentCommandExecutor(@Reference CommandExecutorRegistry executorRegistry,
                                            @Monitor RuntimeDeploymentCommandExecutorMonitor monitor) {
        this.executorRegistry = executorRegistry;
        this.monitor = monitor;
    }

    @Init
    public void init() {
        executorRegistry.register(RuntimeDeploymentCommand.class, this);
    }

    public void execute(RuntimeDeploymentCommand command) throws ExecutionException {
        String id = command.getId();
        monitor.receivedDeploymentCommand(id);
        for (Command cmd : command.getCommands()) {
            executorRegistry.execute(cmd);
        }
        monitor.receivedDeploymentCommand(id);
    }
}