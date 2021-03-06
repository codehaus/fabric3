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
package org.fabric3.fabric.executor;

import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.StopContextCommand;
import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;

@EagerInit
public class StopContextCommandExecutor implements CommandExecutor<StopContextCommand> {

    private ScopeContainer container;
    private CommandExecutorRegistry commandExecutorRegistry;


    public StopContextCommandExecutor(@Reference CommandExecutorRegistry commandExecutorRegistry, @Reference ScopeRegistry scopeRegistry) {
        this.commandExecutorRegistry = commandExecutorRegistry;
        this.container = scopeRegistry.getScopeContainer(Scope.COMPOSITE);
    }

    @Init
    public void init() {
        commandExecutorRegistry.register(StopContextCommand.class, this);
    }

    public void execute(StopContextCommand command) throws ExecutionException {
        QName deployable = command.getDeployable();
        WorkContext workContext = new WorkContext();
        CallFrame frame = new CallFrame(deployable);
        workContext.addCallFrame(frame);
        container.stopContext(workContext);
    }

}

