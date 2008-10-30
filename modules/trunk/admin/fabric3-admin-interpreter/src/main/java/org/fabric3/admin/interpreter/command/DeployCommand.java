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
import org.fabric3.admin.api.ContributionException;
import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.api.DuplicateContributionException;
import org.fabric3.admin.api.InvalidContributionException;
import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandException;

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

    public void execute(PrintStream out) throws CommandException {
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
        if (planName != null) {
            deployByName(out);
        } else if (planFile != null) {
            deployByFile(out);
        } else {
            deployNoPlan(out);
        }
    }

    private void deployByName(PrintStream out) {
        try {
            controller.deploy(contributionUri, planName);
            out.println("Deployed " + contributionUri);
        } catch (CommunicationException e) {
            out.println("ERROR: Error connecting to domain controller");
            e.printStackTrace(out);
        }
    }

    private void deployByFile(PrintStream out) {
        URI planContributionUri = CommandHelper.parseContributionName(planFile);
        try {
            controller.install(planFile, planContributionUri); // install plan
            String installedPlanName = parsePlanName();
            controller.deploy(contributionUri, installedPlanName);
            out.println("Deployed " + contributionUri);
        } catch (CommunicationException e) {
            out.println("ERROR: Error connecting to domain controller");
            e.printStackTrace(out);
        } catch (InvalidContributionException e) {
            out.println("The following errors were found in the deployment plan:");
            for (String desc : e.getErrors()) {
                out.println("ERROR: " + desc);
            }
            // remove the plan from the persistent store
            try {
                controller.remove(planContributionUri);
            } catch (CommunicationException ex) {
                out.println("ERROR: Error connecting to domain controller");
                e.printStackTrace(out);
            } catch (ContributionException ex) {
                out.println("ERROR: Error reverting deployment plan");
                e.printStackTrace(out);
            }
        } catch (DuplicateContributionException e) {
            out.println("ERROR: Deployment plan already exists: " + planFile);
            e.printStackTrace(out);
        } catch (ContributionException e) {
            out.println("ERROR: There was a problem installing the deployment plan: " + planFile);
            e.printStackTrace(out);
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
    }

    private void deployNoPlan(PrintStream out) {
        try {
            controller.deploy(contributionUri);
            out.println("Deployed " + contributionUri);
        } catch (CommunicationException e) {
            out.println("ERROR: Error connecting to domain controller");
            e.printStackTrace(out);
        }
    }

    private String parsePlanName() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        DocumentBuilder b = f.newDocumentBuilder();
        Document d = b.parse(planFile.openStream());
        return d.getDocumentElement().getAttribute("name");
    }

}