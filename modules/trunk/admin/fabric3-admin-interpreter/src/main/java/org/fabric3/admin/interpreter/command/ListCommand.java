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
import java.util.List;

import org.fabric3.admin.api.CommunicationException;
import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandException;
import org.fabric3.management.domain.ComponentInfo;
import org.fabric3.management.domain.InvalidPathException;

/**
 * @version $Revision$ $Date$
 */
public class ListCommand implements Command {
    private DomainController controller;
    private String path;
    private String username;
    private String password;

    public ListCommand(DomainController controller) {
        this.controller = controller;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean execute(PrintStream out) throws CommandException {
        if (username != null) {
            controller.setUsername(username);
        }
        if (password != null) {
            controller.setPassword(password);
        }
        boolean disconnected = !controller.isConnected();
        try {
            if (disconnected) {
                try {
                    controller.connect();
                } catch (IOException e) {
                    out.println("ERROR: Error connecting to domain controller");
                    e.printStackTrace(out);
                    return false;
                }
            }
            try {
                List<ComponentInfo> infos = controller.getDeployedComponents(path);
                if (infos.isEmpty()) {
                    out.println("No components found");
                    return true;
                }
                out.println("Deployed comoonents (" + path + "):");
                for (ComponentInfo info : infos) {
                    URI uri = info.getUri();
                    URI contributionUri = info.getContributionUri();
                    String zone = info.getZone();
                    out.println("   " + uri + " [Contribution: " + contributionUri + ", Zone: " + zone + "]");
                }
                return true;
            } catch (CommunicationException e) {
                out.println("ERROR: Error connecting to domain controller");
                e.printStackTrace(out);
                return false;
            } catch (InvalidPathException e) {
                out.println("Path was invalid: " + e.getMessage());
                return false;

            }
        } finally {
            if (disconnected && controller.isConnected()) {
                try {
                    controller.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}