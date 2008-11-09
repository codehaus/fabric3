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
package org.fabric3.admin.interpreter.command;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;

import org.fabric3.admin.api.CommunicationException;
import org.fabric3.admin.api.DeploymentException;
import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandException;

/**
 * @version $Revision$ $Date$
 */
public class UndeployCommand implements Command {
    private DomainController controller;
    private URI contributionUri;
    private String username;
    private String password;

    public UndeployCommand(DomainController controller) {
        this.controller = controller;
    }

    public URI getContributionUri() {
        return contributionUri;
    }

    public void setContributionUri(URI uri) {
        this.contributionUri = uri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean execute(PrintStream out) throws CommandException {
        if (username != null) {
            controller.setUsername(username);
        }
        if (password != null) {
            controller.setPassword(password);
        }
        if (!controller.isConnected()) {
            try {
                controller.connect();
            } catch (IOException e) {
                out.println("ERROR: Error connecting to domain controller");
                e.printStackTrace(out);
            }
        }
        try {
            controller.undeploy(contributionUri);
            return true;
        } catch (CommunicationException ex) {
            out.println("ERROR: Error connecting to domain controller");
            ex.printStackTrace(out);
        } catch (DeploymentException ex) {
            out.println("ERROR: Error undeploying contribution");
            out.println("       " + ex.getMessage());
        }
        return false;
    }


}