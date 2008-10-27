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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.fabric3.admin.api.AdministrationException;
import org.fabric3.admin.api.DomainController;
import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.CommandException;

/**
 * @version $Revision$ $Date$
 */
public class DeployCommand implements Command {
    private DomainController controller;
    private String contributionName;
    private String username;
    private String password;
    private String planName;
    private URL planFile;

    public DeployCommand(DomainController controller) {
        this.controller = controller;
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

    public void setPlanName(String plan) {
        this.planName = plan;
    }

    public void setPlanFile(URL planFile) {
        this.planFile = planFile;
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
            if (planName != null) {
                controller.deploy(contributionName, planName);
            } else if (planFile != null) {
                String planContributionName = CommandHelper.parseContributionName(planFile);
                controller.install(planFile, planContributionName); // install plan
                String installedPlanName = parsePlanName();
                controller.deploy(contributionName, installedPlanName);
            } else {
                controller.deploy(contributionName);
            }
            out.println("Deployed " + contributionName);
        } catch (AdministrationException e) {
            throw new CommandException(e);
        } catch (IOException e) {
            out.println("ERROR: Error deploying");
            e.printStackTrace(out);
        } catch (ParserConfigurationException e) {
            out.println("ERROR: Invalid deployment plan");
            e.printStackTrace(out);
        } catch (SAXException e) {
            out.println("ERROR: Invalid deployment plan");
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