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
 */
package org.fabric3.fabric.generator.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.fabric.command.StopContextCommand;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalState;

/**
 * Generates a map of commands keyed by zone to stop deployment contexts during an undeploy operation.
 *
 * @version $Rev: 4150 $ $Date: 2008-05-09 12:33:01 -0700 (Fri, 09 May 2008) $
 */

public class StopContextCommandGeneratorImpl implements StopContextCommandGenerator {

    public Map<String, List<Command>> generate(List<LogicalComponent<?>> components) throws GenerationException {
        Map<String, List<Command>> commands = new HashMap<String, List<Command>>();
        for (LogicalComponent<?> component : components) {
            if (component.getState() == LogicalState.MARKED) {
                List<Command> list = getCommands(component.getZone(), commands);
                StopContextCommand command = new StopContextCommand(component.getDeployable());
                if (!list.contains(command)) {
                    list.add(command);
                }
            }
        }
        return commands;
    }

    private List<Command> getCommands(String zone, Map<String, List<Command>> commands) {
        List<Command> list = commands.get(zone);
        if (list == null) {
            list = new ArrayList<Command>();
            commands.put(zone, list);
        }
        return list;
    }
}
