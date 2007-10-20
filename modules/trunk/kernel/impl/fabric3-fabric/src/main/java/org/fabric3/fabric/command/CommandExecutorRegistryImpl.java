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

import java.util.HashMap;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.CommandExecutor;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.command.ExecutionException;

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
            throw new ExecutorNotFoundException("No registered executor for command", clazz.getName());
        }
        executor.execute(command);
    }
}
