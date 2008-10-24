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
import java.net.URL;

import org.fabric3.admin.api.AdministrationException;
import org.fabric3.admin.api.ContributionAlreadyInstalledException;
import org.fabric3.admin.api.DomainController;
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
                parseContributionName();
            }
            controller.install(contribution, contributionName);
            out.println("Installed " + contributionName);

        } catch (InvalidContributionException e) {
            out.println("ERROR: The contribution contained errors:");
            for (String desc : e.getDescriptions()) {
                out.println("   " + desc);
            }
        } catch (ContributionAlreadyInstalledException e) {
            out.println("ERROR: A contribution with that name is alread installed");
        } catch (AdministrationException e) {
            throw new CommandException(e);
        } catch (IOException e) {
            out.println("ERROR: Unable to connect to the doman controller");
            e.printStackTrace(out);
        }
    }

    private void parseContributionName() {
        String path = contribution.getPath();
        int pos = path.lastIndexOf('/');
        if (pos < 0) {
            contributionName = path;
        } else if (pos == path.length() - 1) {
            String substr = path.substring(0, pos);
            pos = substr.lastIndexOf('/');
            if (pos < 0) {
                contributionName = substr;
            } else {
                contributionName = path.substring(pos + 1, path.length() - 1);
            }
        } else {
            contributionName = path.substring(pos + 1);
        }
    }
}
