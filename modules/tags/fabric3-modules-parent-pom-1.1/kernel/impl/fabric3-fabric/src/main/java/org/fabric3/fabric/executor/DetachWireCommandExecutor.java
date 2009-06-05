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

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.builder.Connector;
import org.fabric3.fabric.command.DetachWireCommand;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;

@EagerInit
public class DetachWireCommandExecutor implements CommandExecutor<DetachWireCommand> {


    private CommandExecutorRegistry commandExecutorRegistry;
    private final Connector connector;

    @Constructor
    public DetachWireCommandExecutor(@Reference CommandExecutorRegistry registry,
                                     @Reference Connector connector) {
        this.commandExecutorRegistry = registry;
        this.connector = connector;
    }

    @Init
    public void init() {
        commandExecutorRegistry.register(DetachWireCommand.class, this);
    }

    public void execute(DetachWireCommand command) throws ExecutionException {
        try {
            connector.disconnect(command.getPhysicalWireDefinition());
        } catch (BuilderException be) {
            throw new AssertionError(be);
        }
    }
}
