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
import java.net.URI;
import java.net.URL;

import org.fabric3.admin.api.CommunicationException;
import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandException;
import org.fabric3.management.contribution.ContributionManagementException;
import org.fabric3.management.contribution.DuplicateContributionManagementException;

/**
 * @version $Revision$ $Date$
 */
public class InstallProfileCommand implements Command {
    private DomainController controller;
    private URL profile;
    private URI profileUri;
    private String username;
    private String password;

    public InstallProfileCommand(DomainController controller) {
        this.controller = controller;
    }

    public URL getProfile() {
        return profile;
    }

    public void setProfile(URL profile) {
        this.profile = profile;
    }

    public URI getProfileUri() {
        return profileUri;
    }

    public void setProfileUri(URI uri) {
        this.profileUri = uri;
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
        boolean disconnected = !controller.isConnected();
        try {
            if (username != null) {
                controller.setUsername(username);
            }
            if (password != null) {
                controller.setPassword(password);
            }
            if (disconnected) {
                controller.connect();
            }
            if (profileUri == null) {
                profileUri = CommandHelper.parseContributionName(profile);
            }
            controller.storeProfile(profile, profileUri);
            controller.installProfile(profileUri);
            out.println("Installed " + profileUri);
            return true;
        } catch (DuplicateContributionManagementException e) {
            out.println("ERROR: A profile with that name already exists");
        } catch (CommunicationException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                out.println("ERROR: File not found:" + e.getMessage());
                return false;
            }
            throw new CommandException(e);
        } catch (IOException e) {
            out.println("ERROR: Unable to connect to the domain controller");
            e.printStackTrace(out);
        } catch (ContributionManagementException e) {
            out.println("ERROR: Error installing contribution");
            out.println("       " + e.getMessage());
        } finally {
            if (disconnected && controller.isConnected()) {
                try {
                    controller.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }


}