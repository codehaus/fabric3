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
import java.util.Set;

import org.fabric3.admin.api.CommunicationException;
import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandException;
import org.fabric3.management.contribution.ContributionInfo;

/**
 * @version $Revision$ $Date$
 */
public class StatCommand implements Command {
    private DomainController controller;
    private String username;
    private String password;

    public StatCommand(DomainController controller) {
        this.controller = controller;
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
        try {
            if (!controller.isConnected()) {
                controller.connect();
            }
            Set<ContributionInfo> infos = controller.stat();
            if (infos.isEmpty()) {
                out.println("No contributions");
            } else {
                out.println("Contributions:\n");
                for (ContributionInfo info : infos) {
                    out.printf("%-65s %s \n", info.getUri(), info.getState());
                }
            }
            return true;
        } catch (IOException e) {
            out.println("ERROR: Unable to connect to the domain controller");
            e.printStackTrace(out);
        } catch (CommunicationException e) {
            throw new CommandException(e);
        }
        return false;
    }

}