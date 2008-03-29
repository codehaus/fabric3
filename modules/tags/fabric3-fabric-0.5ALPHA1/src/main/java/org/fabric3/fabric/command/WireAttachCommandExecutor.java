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
package org.fabric3.fabric.command;

import org.fabric3.fabric.builder.Connector;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.command.CommandExecutor;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.command.ExecutionException;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * Eagerly initializes a component on a service node.
 *
 * @version $Rev: 2878 $ $Date: 2008-02-23 18:42:09 +0000 (Sat, 23 Feb 2008) $
 */
@EagerInit
public class WireAttachCommandExecutor implements CommandExecutor<WireAttachCommand> {
    
    private CommandExecutorRegistry commandExecutorRegistry;
    private final Connector connector;

    @Constructor
    public WireAttachCommandExecutor(@Reference CommandExecutorRegistry commandExecutorRegistry,
                                     @Reference Connector connector) {
        this.commandExecutorRegistry = commandExecutorRegistry;
        this.connector = connector;
    }

    public WireAttachCommandExecutor(Connector connector) {
        this.connector = connector;
    }

    @Init
    public void init() {
        commandExecutorRegistry.register(WireAttachCommand.class, this);
    }

    public void execute(WireAttachCommand command) throws ExecutionException {
        
        for (PhysicalWireDefinition physicalWireDefinition : command.getPhysicalWireDefinitions()) {
            try {
                connector.connect(physicalWireDefinition);
            } catch (BuilderException e) {
                throw new ExecutionException(e.getMessage(), e);
            }
        }
        
    }
}
