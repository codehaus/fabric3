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

import java.util.List;

import org.fabric3.spi.command.Command;

/**
 * Aggregates a set of commands for deploying components to a zone.
 *
 * @version $Revision$ $Date$
 */
public class ZoneDeploymentCommand implements Command {
    private static final long serialVersionUID = 8673100303949676875L;

    private String id;
    private List<Command> commands;
    private String correlationId;
    private boolean synchronization;

    /**
     * Constructor.
     *
     * @param id              the unique command id
     * @param commands        the set of commands used to deploy components
     * @param correlationId   the correlation id used to associate the deployment command with an originating request
     * @param synchronization true if this command was in response to a runtime request to synchronize with the domain
     */
    public ZoneDeploymentCommand(String id, List<Command> commands, String correlationId, boolean synchronization) {
        this.id = id;
        this.commands = commands;
        this.correlationId = correlationId;
        this.synchronization = synchronization;
    }

    /**
     * The unique command id.
     *
     * @return the unique command id
     */
    public String getId() {
        return id;
    }

    /**
     * The correlation id used to associate the deployment command with an originating request.
     *
     * @return the id or null if the command is not correlated with a request
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Returns true if this command was in response to a runtime request to synchronize with the domain.
     *
     * @return true if this command was in response to a runtime request to synchronize with the domain
     */
    public boolean isSynchronization() {
        return synchronization;
    }

    /**
     * Returns the list of commands used to deploy components.
     *
     * @return the list of commands used to deploy components
     */
    public List<Command> getCommands() {
        return commands;
    }

}
