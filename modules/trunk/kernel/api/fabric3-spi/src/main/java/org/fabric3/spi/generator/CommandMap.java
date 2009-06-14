/*
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.spi.command.Command;

/**
 * Contains commands mapped by the zone to which they are to be applied.
 *
 * @version $Revision$ $Date$
 */
public class CommandMap {
    private String id;
    private String correlationId;
    private boolean synchornization;

    private Map<String, ZoneCommands> commands = new HashMap<String, ZoneCommands>();

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
        ZoneCommands cmds = getZoneCommands(zone);
        cmds.addCommand(command);
    }

    public void addExtensionCommand(String zone, Command command) {
        ZoneCommands cmds = getZoneCommands(zone);
        cmds.addExtensionCommand(command);
    }

    public void addExtensionCommands(String zone, List<Command> commands) {
        ZoneCommands cmds = getZoneCommands(zone);
        cmds.addExtensionCommands(commands);
    }

    public void addCommands(String zone, List<Command> commands) {
        ZoneCommands cmds = getZoneCommands(zone);
        cmds.addCommands(commands);
    }

    public Set<String> getZones() {
        return commands.keySet();
    }

    public ZoneCommands getZoneCommands(String zone) {
        ZoneCommands cmds = commands.get(zone);
        if (cmds == null) {
            cmds = new ZoneCommands();
            commands.put(zone, cmds);
        }
        return cmds;
    }

    public Map<String, List<Command>> getCommands() {
        Map<String, List<Command>> ret = new HashMap<String, List<Command>>();
        for (Map.Entry<String, ZoneCommands> entry : commands.entrySet()) {
            ret.put(entry.getKey(), entry.getValue().getCommands());
        }
        return ret;
    }

}
