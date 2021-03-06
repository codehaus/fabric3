/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.generator.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.fabric.command.StartContextCommand;
import org.fabric3.host.Names;
import org.fabric3.spi.command.CompensatableCommand;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalState;

/**
 * Generates commands to start deployable contexts in a zone.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class StartContextCommandGeneratorImpl implements StartContextCommandGenerator {

    public Map<String, List<CompensatableCommand>> generate(List<LogicalComponent<?>> components, boolean incremental) throws GenerationException {
        Map<String, List<CompensatableCommand>> commands = new HashMap<String, List<CompensatableCommand>>();
        for (LogicalComponent<?> component : components) {
            if (component.getState() == LogicalState.NEW || !incremental) {
                QName deployable = component.getDeployable();
                // only log application composite deployments
                boolean log = !component.getUri().toString().startsWith(Names.RUNTIME_NAME);
                StartContextCommand command = new StartContextCommand(deployable, log);
                String zone = component.getZone();
                List<CompensatableCommand> list = getCommands(zone, commands);
                if (!list.contains(command)) {
                    list.add(command);
                }
            }
        }
        return commands;
    }

    /**
     * Returns the list of commands by zone, creating one if necessary.
     *
     * @param zone          the zone
     * @param startCommands the list of commands mapped by zone
     * @return the list of commands for a zone
     */
    private List<CompensatableCommand> getCommands(String zone, Map<String, List<CompensatableCommand>> startCommands) {
        List<CompensatableCommand> list = startCommands.get(zone);
        if (list == null) {
            list = new ArrayList<CompensatableCommand>();
            startCommands.put(zone, list);
        }
        return list;
    }

}
