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
package org.fabric3.spi.generator;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.spi.command.Command;

/**
 * A collection of commands sent to a zone. Extension-related commands are segregated so they may be deserialized and executed prior to other
 * commands.
 *
 * @version $Revision$ $Date$
 */
public class ZoneCommands {

    private List<Command> extensionCommands = new ArrayList<Command>();
    private List<Command> commands = new ArrayList<Command>();

    public void addExtensionCommand(Command command) {
        extensionCommands.add(command);
    }

    public void addExtensionCommands(List<Command> command) {
        extensionCommands.addAll(command);
    }

    public List<Command> getExtensionCommands() {
        return extensionCommands;
    }

    public void addCommand(Command command) {
        commands.add(command);
    }

    public void addCommands(List<Command> command) {
        commands.addAll(command);
    }

    public List<Command> getCommands() {
        return commands;
    }
}
