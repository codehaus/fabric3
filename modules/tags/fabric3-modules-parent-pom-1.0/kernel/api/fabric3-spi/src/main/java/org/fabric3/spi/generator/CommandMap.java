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
package org.fabric3.spi.generator;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.fabric3.spi.command.Command;

/**
 * Contains a set of comamnds mapped by the zone to which they are to be applied.
 *
 * @version $Revision$ $Date$
 */
public class CommandMap {
    private String id;
    private String correlationId;
    private boolean synchornization;

    private Map<String, Set<Command>> commands = new HashMap<String, Set<Command>>();

    public CommandMap(String id) {
        this.id = id;
    }

    public CommandMap(String id, String correlationId, boolean synchornization) {
        this.id = id;
        this.correlationId = correlationId;
        this.synchornization = synchornization;
    }

    public String getId() {
        return id;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public boolean isSynchornization() {
        return synchornization;
    }

    public void addCommand(String zone, Command command) {
        Set<Command> cmds = getCommandsForRuntimeInternal(zone);
        cmds.add(command);
    }

    public void addCommands(String zone, Set<Command> commandList) {
        Set<Command> cmds = getCommandsForRuntimeInternal(zone);
        cmds.addAll(commandList);
    }

    public Set<String> getZones() {
        return commands.keySet();
    }

    public Set<Command> getCommandsForZone(String zone) {
        Set<Command> cmds = getCommandsForRuntimeInternal(zone);
        return new LinkedHashSet<Command>(cmds);
    }

    private Set<Command> getCommandsForRuntimeInternal(String zone) {
        Set<Command> cmds = commands.get(zone);
        if (cmds == null) {
            cmds = new LinkedHashSet<Command>();
            commands.put(zone, cmds);
        }
        return cmds;
    }

}
