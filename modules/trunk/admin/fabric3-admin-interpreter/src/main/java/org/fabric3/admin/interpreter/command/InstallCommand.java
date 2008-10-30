/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.admin.interpreter.command;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import org.fabric3.admin.api.CommunicationException;
import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.api.DuplicateContributionException;
import org.fabric3.admin.api.ContributionException;
import org.fabric3.admin.api.InvalidContributionException;
import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandException;

/**
 * @version $Revision$ $Date$
 */
public class InstallCommand implements Command {
    private DomainController controller;
    private URL contribution;
    private String contributionName;
    private String username;
    private String password;

    public InstallCommand(DomainController controller) {
        this.controller = controller;
    }

    public URL getContribution() {
        return contribution;
    }

    public void setContribution(URL contribution) {
        this.contribution = contribution;
    }

    public String getContributionName() {
        return contributionName;
    }

    public void setContributionName(String contributionName) {
        this.contributionName = contributionName;
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

    public void execute(PrintStream out) throws CommandException {
        try {
            if (username != null) {
                controller.setUsername(username);
            }
            if (password != null) {
                controller.setPassword(password);
            }
            if (!controller.isConnected()) {
                controller.connect();
            }
            if (contributionName == null) {
                contributionName = CommandHelper.parseContributionName(contribution);
            }
            controller.install(contribution, contributionName);
            out.println("Installed " + contributionName);
        } catch (InvalidContributionException e) {
            out.println("The contribution contained errors:");
            for (String desc : e.getErrors()) {
                out.println("ERROR: " + desc);
            }
        } catch (DuplicateContributionException e) {
            out.println("ERROR: A contribution with that name is already installed");
        } catch (CommunicationException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                out.println("ERROR: File not found:" + e.getMessage());
                return;
            }
            throw new CommandException(e);
        } catch (IOException e) {
            out.println("ERROR: Unable to connect to the doman controller");
            e.printStackTrace(out);
        } catch (ContributionException e) {
            out.println("ERROR: Error installing contribution");
            e.printStackTrace(out);
        }
    }


}
