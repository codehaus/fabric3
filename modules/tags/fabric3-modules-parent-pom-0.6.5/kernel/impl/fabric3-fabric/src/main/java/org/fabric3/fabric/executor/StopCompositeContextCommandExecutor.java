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
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Init;

import org.fabric3.fabric.command.StopCompositeContextCommand;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.scdl.Scope;

@EagerInit
public class StopCompositeContextCommandExecutor implements CommandExecutor<StopCompositeContextCommand> {

    private ScopeContainer<URI> container;
    private CommandExecutorRegistry commandExecutorRegistry;


    public StopCompositeContextCommandExecutor(@Reference CommandExecutorRegistry commandExecutorRegistry,
                                                @Reference ScopeRegistry scopeRegistry) {
        this.commandExecutorRegistry = commandExecutorRegistry;
        this.container = scopeRegistry.getScopeContainer(Scope.COMPOSITE);
    }


    @Init
    public void init() {
        commandExecutorRegistry.register(StopCompositeContextCommand.class, this);
    }

    public void execute(StopCompositeContextCommand command) throws ExecutionException {
        URI groupId = command.getGroupId();
        WorkContext workContext = new WorkContext();
        CallFrame frame = new CallFrame(groupId);
        workContext.addCallFrame(frame);
        container.stopContext(workContext);

    }

}

