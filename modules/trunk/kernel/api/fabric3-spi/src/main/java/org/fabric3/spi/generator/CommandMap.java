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
package org.fabric3.spi.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private Map<String, List<Command>> commands = new HashMap<String, List<Command>>();

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
        List<Command> cmds = getCommandsForZone(zone);
        cmds.add(command);
    }

    public void addCommands(String zone, List<Command> commandList) {
        List<Command> cmds = getCommandsForZone(zone);
        cmds.addAll(commandList);
    }

    public Set<String> getZones() {
        return commands.keySet();
    }

    public List<Command> getCommandsForZone(String zone) {
        List<Command> cmds = commands.get(zone);
        if (cmds == null) {
            cmds = new ArrayList<Command>();
            commands.put(zone, cmds);
        }
        return cmds;
    }

    public Map<String, List<Command>> getCommands() {
        return commands;
    }

}
