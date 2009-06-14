/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.management.MBeanServerFactory;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.surefire.report.BriefConsoleReporter;
import org.apache.maven.surefire.report.BriefFileReporter;
import org.apache.maven.surefire.report.Reporter;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.report.ReporterManager;
import org.apache.maven.surefire.report.XMLReporter;
import org.apache.maven.surefire.suite.SurefireTestSuite;
import org.apache.maven.surefire.testset.TestSetFailedException;

import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.test.artifact.ArtifactHelper;
import org.fabric3.test.contribution.MavenContributionScanner;
import org.fabric3.test.contribution.MavenContributionScannerImpl;
import org.fabric3.test.contribution.ScanResult;
import org.fabric3.test.monitor.MavenMonitorFactory;
import org.fabric3.test.runtime.api.MavenRuntime;

/**
 * Fabric3 Mojo for testing SCA services.
 *
 * @goal test
 * @phase integration-test
 * @execute phase="integration-test"
 */
public class Fabric3TestMojo extends AbstractMojo {

    /**
     * Artifact helper for resolving dependencies.
     *
     * @parameter expression="${component.org.fabric3.test.artifact.ArtifactHelper}"
     * @required
     * @readonly
     */
    public ArtifactHelper artifactHelper;

    /**
     * Location of the local repository.
     *
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    public ArtifactRepository localRepository;

    /**
     * Maven project.
     *
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    protected MavenProject mavenProject;

    /**
     * The version of the runtime to use.
     *
     * @parameter expression="1.1-SNAPSHOT"
     */
    public String runtimeVersion;

    /**
     * Properties passed to the runtime throught the HostInfo interface.
     *
     * @parameter
     */
    public Properties properties;

    /**
     * Do not run if this is set to true. This usage is consistent with the surefire plugin.
     *
     * @parameter expression="${maven.test.skip}"
     */
    public boolean skip;

    /**
     * The directory where reports will be written.
     *
     * @parameter expression="${project.build.directory}/surefire-reports"
     */
    public File reportsDirectory;

    /**
     * Whether to trim the stack trace in the reports to just the lines within the test, or show the full trace.
     *
     * @parameter expression="${trimStackTrace}" default-value="true"
     */
    public boolean trimStackTrace;

    /**
     * Contributes scanned contributions and run the tests.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (skip) {
            return;
        }

        artifactHelper.setRepositories(localRepository, mavenProject.getRemoteArtifactRepositories());

        ClassLoader hostClassLoader = createHostClassLoader();
        ClassLoader bootClassLoader = createBootClassLoader(hostClassLoader);

        MavenContributionScanner scanner = new MavenContributionScannerImpl();
        ScanResult scanResult = scanner.scan(mavenProject);
        logContributions(scanResult);

        MavenRuntime runtime = createRuntime(bootClassLoader, hostClassLoader);

        getLog().info("Booting test runtime");
        runtime.start(properties, scanResult.getExtensionContributions());
        getLog().info("Fabric3 test runtime booted");

        getLog().info("Deloying user contributions");
        runtime.deploy(scanResult.getUserContributions());
        getLog().info("User contributions deployed");

        SurefireTestSuite surefireTestSuite = runtime.getTestSuite();
        getLog().info("Executing integration tests");
        runTests(surefireTestSuite);
        getLog().info("Executed integration tests");

    }

    /*
     * Logs the contributions.
     */
    private void logContributions(ScanResult scanResult) {

        getLog().info("Number of extension contributions: " + scanResult.getExtensionContributions().size());
        for (ContributionSource extensionContribution : scanResult.getExtensionContributions()) {
            getLog().info(extensionContribution.getLocation().toExternalForm());
        }
        getLog().info("Number of user contributions: " + scanResult.getUserContributions().size());
        for (ContributionSource userContribution : scanResult.getUserContributions()) {
            getLog().info(userContribution.getLocation().toExternalForm());
        }

    }

    /*
    * Create the host classloader.
    */
    private ClassLoader createHostClassLoader() throws MojoExecutionException {

        Set<URL> hostClasspath = artifactHelper.resolve("org.codehaus.fabric3", "fabric3-api", runtimeVersion, Artifact.SCOPE_RUNTIME, "jar");
        hostClasspath.addAll(artifactHelper.resolve("org.codehaus.fabric3", "fabric3-host-api", runtimeVersion, Artifact.SCOPE_RUNTIME, "jar"));
        hostClasspath.addAll(artifactHelper.resolve("javax.servlet", "servlet-api", "2.4", Artifact.SCOPE_RUNTIME, "jar"));

        return new URLClassLoader(hostClasspath.toArray(new URL[]{}), getClass().getClassLoader());

    }

    /*
    * Create the boot classloader.
    */
    private ClassLoader createBootClassLoader(ClassLoader hostClassLoader) throws MojoExecutionException {

        Set<URL> bootClassPath =
                artifactHelper.resolve("org.codehaus.fabric3", "fabric3-test-runtime", runtimeVersion, Artifact.SCOPE_RUNTIME, "jar");
        return new URLClassLoader(bootClassPath.toArray(new URL[]{}), hostClassLoader);

    }

    /*
    * Creates the runtime instance.
    */
    private MavenRuntime createRuntime(ClassLoader bootClassLoader, ClassLoader hostClassLoader) throws MojoExecutionException {

        try {

            Class<?> runtimeClass = bootClassLoader.loadClass("org.fabric3.test.runtime.MavenRuntimeImpl");
            MavenRuntime runtime = MavenRuntime.class.cast(runtimeClass.newInstance());

            MonitorFactory monitorFactory = new MavenMonitorFactory(getLog(), "f3");
            runtime.setMonitorFactory(monitorFactory);
            runtime.setHostClassLoader(hostClassLoader);

            runtime.setMBeanServer(MBeanServerFactory.createMBeanServer());

            return runtime;
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }

    /*
     * Runs the surefire tests.
     */
    private boolean runTests(SurefireTestSuite suite) throws MojoExecutionException {

        try {

            Properties status = new Properties();
            int totalTests = suite.getNumTests();

            List<Reporter> reports = new ArrayList<Reporter>();
            reports.add(new XMLReporter(reportsDirectory, trimStackTrace));
            reports.add(new BriefFileReporter(reportsDirectory, trimStackTrace));
            reports.add(new BriefConsoleReporter(trimStackTrace));
            ReporterManager reporterManager = new ReporterManager(reports);
            reporterManager.initResultsFromProperties(status);

            reporterManager.runStarting(totalTests);

            if (totalTests == 0) {
                reporterManager.writeMessage("There are no tests to run.");
            } else {
                suite.execute(reporterManager, null);
            }

            reporterManager.runCompleted();
            reporterManager.updateResultsProperties(status);

            return reporterManager.getNumErrors() == 0 && reporterManager.getNumFailures() == 0;

        } catch (ReporterException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (TestSetFailedException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }

}
