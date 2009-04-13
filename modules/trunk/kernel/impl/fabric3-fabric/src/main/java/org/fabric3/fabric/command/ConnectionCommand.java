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
package org.fabric3.fabric.command;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.spi.command.Command;

/**
 * Contains commands for attaching and detaching wires.
 * 
 * @version $Revision$ $Date$
 */
public class ConnectionCommand implements Command {
    private static final long serialVersionUID = -2313380946362271104L;
    private List<AttachWireCommand> attachCommands;
    private List<DetachWireCommand> detachCommands;

    public ConnectionCommand() {
        attachCommands = new ArrayList<AttachWireCommand>();
        detachCommands = new ArrayList<DetachWireCommand>();
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
