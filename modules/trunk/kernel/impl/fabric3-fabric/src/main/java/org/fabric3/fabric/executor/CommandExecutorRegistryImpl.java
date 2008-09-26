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

import java.util.HashMap;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.command.Command;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;

/**
 * Default implementation of the CommandExecutorRegistry
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class CommandExecutorRegistryImpl implements CommandExecutorRegistry {
    private Map<Class<? extends Command>, CommandExecutor<?>> executors =
            new HashMap<Class<? extends Command>, CommandExecutor<?>>();

    public <T extends Command> void register(Class<T> type, CommandExecutor<T> executor) {
        executors.put(type, executor);
    }

    @SuppressWarnings({"unchecked"})
    public <T extends Command> void execute(T command) throws ExecutionException {
        Class<? extends Command> clazz = command.getClass();
        CommandExecutor<T> executor = (CommandExecutor<T>) executors.get(clazz);
        if (executor == null) {
            throw new ExecutorNotFoundException("No registered executor for command: " + clazz.getName(), clazz.getName());
        }
        executor.execute(command);
    }
}
