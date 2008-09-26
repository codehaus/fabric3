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
 * --- Original Apache License ---
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.fabric.executor;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.builder.Connector;
import org.fabric3.fabric.command.AttachWireCommand;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

/**
 * Eagerly initializes a component on a service node.
 *
 * @version $Rev: 2878 $ $Date: 2008-02-23 18:42:09 +0000 (Sat, 23 Feb 2008) $
 */
@EagerInit
public class AttachWireCommandExecutor implements CommandExecutor<AttachWireCommand> {

    private CommandExecutorRegistry commandExecutorRegistry;
    private final Connector connector;

    @Constructor
    public AttachWireCommandExecutor(@Reference CommandExecutorRegistry commandExecutorRegistry,
                                     @Reference Connector connector) {
        this.commandExecutorRegistry = commandExecutorRegistry;
        this.connector = connector;
    }

    public AttachWireCommandExecutor(Connector connector) {
        this.connector = connector;
    }

    @Init
    public void init() {
        commandExecutorRegistry.register(AttachWireCommand.class, this);
    }

    public void execute(AttachWireCommand command) throws ExecutionException {

        for (PhysicalWireDefinition physicalWireDefinition : command.getPhysicalWireDefinitions()) {
            try {
                connector.connect(physicalWireDefinition);
            } catch (BuilderException e) {
                throw new ExecutionException(e.getMessage(), e);
            }
        }

    }
}
