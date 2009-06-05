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

import java.io.PrintStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandException;

/**
 * @version $Revision$ $Date$
 */
public class ProvisionCommand implements Command {
    private DeployCommand deployCommand;
    private InstallCommand installCommand;
    private DomainController controller;

    public ProvisionCommand(DomainController controller) {
        this.controller = controller;
        installCommand = new InstallCommand(controller);
        deployCommand = new DeployCommand(controller);
    }

    public void setUsername(String username) {
        installCommand.setUsername(username);
    }

    public void setPassword(String password) {
        installCommand.setPassword(password);
    }

    public void setContribution(URL contribution) {
        installCommand.setContribution(contribution);
        URI contributionUri = CommandHelper.parseContributionName(contribution);
        installCommand.setContributionUri(contributionUri);
        deployCommand.setContributionUri(contributionUri);
    }

    public void setContributionUri(URI uri) {
        installCommand.setContributionUri(uri);
        deployCommand.setContributionUri(uri);
    }

    public void setPlanFile(URL planFile) {
        deployCommand.setPlanFile(planFile);
    }

    public void setPlanName(String name) {
        deployCommand.setPlanName(name);
    }

    public boolean execute(PrintStream out) throws CommandException {
        boolean disconnected = !controller.isConnected();
        try {
            return installCommand.execute(out) && deployCommand.execute(out);
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