/*
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
package org.fabric3.spi.generator;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.fabric3.spi.command.Command;

/**
 *
 * @version $Revision$ $Date$
 */
public class CommandMap {
    private Map<URI, Set<Command>> commands = new HashMap<URI, Set<Command>>();
    
    public void addCommand(URI runtimeId, Command command) {
        Set<Command> cmds = getCommandsForRuntimeInternal(runtimeId);
        cmds.add(command);
    }
    
    public void addCommands(URI runtimeId, Set<Command> commandList) {
        Set<Command> cmds = getCommandsForRuntimeInternal(runtimeId);
        cmds.addAll(commandList);
    }
    
    public Set<URI> getRuntimeIds() {
        return Collections.unmodifiableSet(commands.keySet());
    }
    
    public Set<Command> getCommandsForRuntime(URI runtimeId) {
        Set<Command> cmds = getCommandsForRuntimeInternal(runtimeId);
        return new LinkedHashSet<Command>(cmds);
    }

    private  Set<Command> getCommandsForRuntimeInternal(URI runtimeId) {
        Set<Command> cmds = commands.get(runtimeId);
        if (cmds == null) {
            cmds = new LinkedHashSet<Command>();
            commands.put(runtimeId, cmds);
        }
        return cmds;
    }

}
