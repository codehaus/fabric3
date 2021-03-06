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
*/
package org.fabric3.fabric.command;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.fabric3.spi.command.CompensatableCommand;

/**
 * Contains commands for attaching and detaching wires for a component.
 *
 * @version $Rev$ $Date$
 */
public class ConnectionCommand implements CompensatableCommand {
    private static final long serialVersionUID = -2313380946362271104L;
    private URI componentUri;
    private List<AttachWireCommand> attachCommands;
    private List<DetachWireCommand> detachCommands;

    public ConnectionCommand(URI componentUri) {
        this();
        this.componentUri = componentUri;
    }

    protected ConnectionCommand() {
        attachCommands = new ArrayList<AttachWireCommand>();
        detachCommands = new ArrayList<DetachWireCommand>();
    }

    public ConnectionCommand getCompensatingCommand() {
        ConnectionCommand compensating = new ConnectionCommand(componentUri);
        if (!attachCommands.isEmpty()){
            ListIterator<AttachWireCommand> iter = attachCommands.listIterator(attachCommands.size());
            while(iter.hasPrevious()){
                AttachWireCommand command = iter.previous();
                DetachWireCommand compensatingWireCommand = command.getCompensatingCommand();
                compensating.add(compensatingWireCommand);
            }
        }
        if (!detachCommands.isEmpty()){
            ListIterator<DetachWireCommand> iter = detachCommands.listIterator(detachCommands.size());
            while(iter.hasPrevious()){
                DetachWireCommand command = iter.previous();
                AttachWireCommand compensatingWireCommand = command.getCompensatingCommand();
                compensating.add(compensatingWireCommand);
            }
        }

        return compensating;
    }

    public URI getComponentUri() {
        return componentUri;
    }

    public List<AttachWireCommand> getAttachCommands() {
        return attachCommands;
    }

    public List<DetachWireCommand> getDetachCommands() {
        return detachCommands;
    }

    public void add(AttachWireCommand command) {
        attachCommands.add(command);
    }

    public void add(DetachWireCommand command) {
        detachCommands.add(command);
    }

}
