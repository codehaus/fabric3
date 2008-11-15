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
package org.fabric3.itest;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ValidationException;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.domain.AssemblyException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.maven.runtime.ContextStartException;
import org.fabric3.maven.runtime.MavenEmbeddedRuntime;

/**
 * Deploys a test composite.
 *
 * @version $Revision$ $Date$
 */
public class TestDeployer {
    private Log log;
    private File testScdl;
    private String compositeNamespace;
    private String compositeName;
    private File buildDirectory;

    public TestDeployer(File testScdl, File buildDirectory, Log log) {
        this.log = log;
        this.testScdl = testScdl;
        this.buildDirectory = buildDirectory;
    }

    public TestDeployer(String compositeNamespace, String compositeName, File buildDirectory, Log log) {
        this.log = log;
        this.compositeNamespace = compositeNamespace;
        this.compositeName = compositeName;
        this.buildDirectory = buildDirectory;
    }

    public void deploy(MavenEmbeddedRuntime runtime) throws MojoExecutionException {
        try {
            if (compositeName == null) {
                URL testScdlURL = testScdl.toURI().toURL();
                deployTestComposite(runtime, testScdlURL);
            } else {
                deployTestComposite(runtime);
            }
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        } catch (MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            // trap any other exception
            throw new MojoExecutionException("Error deploying test composite: " + testScdl, e);
        }
    }

    private void deployTestComposite(MavenEmbeddedRuntime runtime, URL testScdlURL)
            throws DeploymentException, ContributionException, ContextStartException, MojoExecutionException {
        try {
            log.info("Deploying test composite from " + testScdl);
            URL buildDirUrl = getBuildDirectoryUrl();
            List<Deployable> deployables = runtime.deploy(buildDirUrl, testScdlURL);
            for (Deployable deployable : deployables) {
                runtime.startContext(deployable.getName());
            }
        } catch (ValidationException e) {
            // print out the validaiton errors
            reportContributionErrors(e);
            String msg = "Contribution errors were found";
            throw new MojoExecutionException(msg);
        } catch (AssemblyException e) {
            reportDeploymentErrors(e);
            String msg = "Deployment errors were found";
            throw new MojoExecutionException(msg);
        }
    }

    private void deployTestComposite(MavenEmbeddedRuntime runtime)
            throws ContributionException, DeploymentException, ContextStartException, MojoExecutionException {
        try {
            QName qName = new QName(compositeNamespace, compositeName);
            log.info("Deploying test composite " + qName);
            URL buildDirUrl = getBuildDirectoryUrl();
            runtime.deploy(buildDirUrl, qName);
            runtime.startContext(qName);
        } catch (ValidationException e) {
            // print out the validation errors
            reportContributionErrors(e);
            String msg = "Contribution errors were found";
            throw new MojoExecutionException(msg);
        } catch (AssemblyException e) {
            reportDeploymentErrors(e);
            String msg = "Deployment errors were found";
            throw new MojoExecutionException(msg);
        }
    }

    private void reportContributionErrors(ValidationException cause) {
        StringBuilder b = new StringBuilder("\n\n");
        b.append("-------------------------------------------------------\n");
        b.append("CONTRIBUTION ERRORS\n");
        b.append("-------------------------------------------------------\n\n");
        b.append(cause.getMessage());
        log.error(b);
    }

    private void reportDeploymentErrors(AssemblyException cause) {
        StringBuilder b = new StringBuilder("\n\n");
        b.append("-------------------------------------------------------\n");
        b.append("DEPLOYMENT ERRORS\n");
        b.append("-------------------------------------------------------\n\n");
        b.append(cause.getMessage());
        log.error(b);
    }

    private URL getBuildDirectoryUrl() {
        try {
            return buildDirectory.toURI().toURL();
        } catch (MalformedURLException e) {
            // this should not happen
            throw new AssertionError();
        }
    }


}
