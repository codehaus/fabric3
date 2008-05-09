package org.fabric3.fabric.executor;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.builder.Connector;
import org.fabric3.fabric.command.WireDetachCommand;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
@EagerInit
public class WireDetachCommandExecutor implements CommandExecutor<WireDetachCommand> {


    private CommandExecutorRegistry commandExecutorRegistry;
    private final Connector connector;

    @Constructor
    public WireDetachCommandExecutor(@Reference CommandExecutorRegistry commandExecutorRegistry,
                                     @Reference Connector connector) {
        this.commandExecutorRegistry = commandExecutorRegistry;
        this.connector = connector;
    }

    @Init
    public void init() {
        commandExecutorRegistry.register(WireDetachCommand.class, this);
    }

    public void execute(WireDetachCommand command) throws ExecutionException {
/*

        for (PhysicalWireDefinition physicalWireDefinition : command.getPhysicalWireDefinitions()) {
                //call disconnect
            connector.disconnect(physicalWireDefinition);
        }
*/

    }
}
