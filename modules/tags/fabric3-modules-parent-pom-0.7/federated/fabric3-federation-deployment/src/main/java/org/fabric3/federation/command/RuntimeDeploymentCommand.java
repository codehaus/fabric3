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
package org.fabric3.federation.command;

import java.util.Set;

import org.fabric3.spi.command.AbstractCommand;
import org.fabric3.spi.command.Command;

/**
 * @version $Revision$ $Date$
 */
public class RuntimeDeploymentCommand extends AbstractCommand {
    private static final long serialVersionUID = -3864446712541806877L;

    private String id;
    private Set<Command> commands;

    public RuntimeDeploymentCommand(String id, Set<Command> commands) {
        super(0);
        this.id = id;
        this.commands = commands;
    }

    public Set<Command> getCommands() {
        return commands;
    }

    public String getId() {
        return id;
    }
}