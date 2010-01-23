/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.federation.command;

import java.io.Serializable;

import org.fabric3.spi.command.ResponseCommand;

/**
 * Broadcast by the controller to deploy a composite to all members in a zone.  Aggregates the set of commands to deploy the composite as well as
 * required extensions.
 *
 * @version $Rev$ $Date$
 */
public class DeploymentCommand implements ResponseCommand {
    private static final long serialVersionUID = 8673100303949676875L;

    private byte[] extensionCommands;
    private byte[] commands;
    private Serializable response;

    /**
     * Constructor.
     *
     * @param extensionCommands the serialized commands that install extensions required to run the deployment
     * @param commands          the serialized commands to deploy a set of composites
     */
    public DeploymentCommand(byte[] extensionCommands, byte[] commands) {
        this.extensionCommands = extensionCommands;
        this.commands = commands;
    }

    /**
     * Returns the serialized commands that install extensions required to run the deployment.
     *
     * @return the serialized extension commands
     */
    public byte[] getExtensionCommands() {
        return extensionCommands;
    }

    /**
     * Returns the serialized composite deployment commands.
     *
     * @return the serialized composite deployment commands
     */
    public byte[] getCommands() {
        return commands;
    }

    public void setResponse(Serializable response) {
        this.response = response;
    }

    public Serializable getResponse() {
        return response;
    }
}