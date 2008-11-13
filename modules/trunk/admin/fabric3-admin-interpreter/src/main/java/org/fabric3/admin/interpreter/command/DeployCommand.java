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
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.fabric3.admin.api.CommunicationException;
import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandException;
import org.fabric3.management.contribution.ContributionManagementException;
import org.fabric3.management.contribution.DuplicateContributionManagementException;
import org.fabric3.management.contribution.InvalidContributionException;
import org.fabric3.management.domain.DeploymentManagementException;
import org.fabric3.management.domain.InvalidDeploymentException;

/**
 * @version $Revision$ $Date$
 */
public class DeployCommand implements Command {
    private DomainController controller;
    private URI contributionUri;
    private String username;
    private String password;
    private String planName;
    private URL planFile;

    public DeployCommand(DomainController controller) {
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

    public void setPlanName(String plan) {
        this.planName = plan;
    }

    public void setPlanFile(URL planFile) {
        this.planFile = planFile;
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
            if (planName != null) {
                return deployByName(out);
            } else if (planFile != null) {
                return deployByFile(out);
            } else {
                return deployNoPlan(out);
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

    private boolean deployByName(PrintStream out) {
        try {
            controller.deploy(contributionUri, planName);
            out.println("Deployed " + contributionUri);
            return true;
        } catch (CommunicationException e) {
            out.println("ERROR: Error connecting to domain controller");
            e.printStackTrace(out);
        } catch (InvalidDeploymentException e) {
            out.println("The following deployment errors were reported:");
            for (String desc : e.getErrors()) {
                out.println("ERROR: " + desc);
            }
        } catch (DeploymentManagementException e) {
            out.println("ERROR: Error deploying contribution");
            out.println("       " + e.getMessage());
        }
        return false;
    }

    private boolean deployByFile(PrintStream out) {
        URI planContributionUri = CommandHelper.parseContributionName(planFile);
        try {
            // store and install plan
            controller.store(planFile, planContributionUri);
            controller.install(planContributionUri);
            String installedPlanName = parsePlanName();
            controller.deploy(contributionUri, installedPlanName);
            out.println("Deployed " + contributionUri);
            return true;
        } catch (InvalidDeploymentException e) {
            out.println("The following deployment errors were reported:");
            for (String desc : e.getErrors()) {
                out.println("ERROR: " + desc);
            }
        } catch (DeploymentManagementException e) {
            out.println("ERROR: Error deploying contribution");
            out.println("       " + e.getMessage());
            revertPlan(planContributionUri, out);
        } catch (CommunicationException e) {
            out.println("ERROR: Error connecting to domain controller");
            e.printStackTrace(out);
        } catch (InvalidContributionException e) {
            out.println("The following errors were found in the deployment plan:\n");
            CommandHelper.printErrors(out, e);
            revertPlan(planContributionUri, out);

        } catch (DuplicateContributionManagementException e) {
            out.println("ERROR: Deployment plan already exists");
        } catch (ContributionManagementException e) {
            out.println("ERROR: There was a problem installing the deployment plan: " + planFile);
            out.println("       " + e.getMessage());
            revertPlan(planContributionUri, out);
        } catch (IOException e) {
            out.println("ERROR: Unable to read deployment plan: " + planFile);
            e.printStackTrace(out);
        } catch (ParserConfigurationException e) {
            out.println("ERROR: Unable to read deployment plan: " + planFile);
            e.printStackTrace(out);
        } catch (SAXException e) {
            out.println("ERROR: Unable to read deployment plan: " + planFile);
            e.printStackTrace(out);
        }
        return false;
    }

    private boolean deployNoPlan(PrintStream out) {
        try {
            controller.deploy(contributionUri);
            out.println("Deployed " + contributionUri);
            return true;
        } catch (InvalidDeploymentException e) {
            out.println("The following deployment errors were reported:");
            for (String desc : e.getErrors()) {
                out.println("ERROR: " + desc);
            }
        } catch (DeploymentManagementException e) {
            out.println("ERROR: Error deploying contribution");
            out.println("       " + e.getMessage());
        } catch (CommunicationException e) {
            out.println("ERROR: Error connecting to domain controller");
            e.printStackTrace(out);
        }
        return false;
    }

    private String parsePlanName() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        DocumentBuilder b = f.newDocumentBuilder();
        Document d = b.parse(planFile.openStream());
        return d.getDocumentElement().getAttribute("name");
    }

    private void revertPlan(URI planContributionUri, PrintStream out) {
        // remove the plan from the persistent store
        try {
            controller.uninstall(planContributionUri);
            controller.remove(planContributionUri);
        } catch (CommunicationException ex) {
            out.println("ERROR: Error connecting to domain controller");
            ex.printStackTrace(out);
        } catch (ContributionManagementException ex) {
            out.println("ERROR: Error reverting deployment plan");
            out.println("       " + ex.getMessage());
        }
    }


}