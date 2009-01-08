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
 */
package org.fabric3.test;

import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.fabric3.fabric.runtime.DefaultCoordinator;
import org.fabric3.fabric.runtime.bootstrap.ScdlBootstrapperImpl;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.host.runtime.StartException;
import org.fabric3.maven.MavenEmbeddedRuntime;
import org.fabric3.maven.MavenHostInfo;
import org.fabric3.maven.runtime.MavenEmbeddedRuntimeImpl;
import org.fabric3.test.contribution.MavenContributionScanner;
import org.fabric3.test.contribution.MavenContributionScannerImpl;
import org.fabric3.test.contribution.ScanResult;
import org.fabric3.test.monitor.MavenMonitorFactory;
import org.xml.sax.InputSource;

/**
 * Fabric3 Mojo for testing SCA services.
 * 
 * @goal test
 * @phase integration-test
 * @execute phase="integration-test"
 *
 */
public class Fabric3TestMojo extends AbstractMojo {

    /**
     * POM
     *
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    protected MavenProject mavenProject;

    /**
     * Contributes scanned contributions and run the tests.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        
        MavenContributionScanner scanner = new MavenContributionScannerImpl();
        ScanResult scanResult = scanner.scan(mavenProject);        
        logContributions(scanResult);
        
        MavenEmbeddedRuntime runtime = new MavenEmbeddedRuntimeImpl();
        MonitorFactory monitorFactory = new MavenMonitorFactory(getLog(), "f3");
        runtime.setMonitorFactory(monitorFactory);
        
        BootConfiguration<MavenEmbeddedRuntime, ScdlBootstrapper> bootConfiguration = new BootConfiguration<MavenEmbeddedRuntime, ScdlBootstrapper>();
        bootConfiguration.setExtensions(scanResult.getExtensionContributions());
        bootConfiguration.setRuntime(runtime);
        
        ScdlBootstrapper bootstrapper = new ScdlBootstrapperImpl();
        URL systemScdl = getClass().getClassLoader().getResource("META-INF/fabric3/embeddedMaven.composite");
        bootstrapper.setScdlLocation(systemScdl);
        bootConfiguration.setBootstrapper(bootstrapper);
        
        RuntimeLifecycleCoordinator<MavenEmbeddedRuntime, ScdlBootstrapper> coordinator = new DefaultCoordinator<MavenEmbeddedRuntime, ScdlBootstrapper>();
        coordinator.setConfiguration(bootConfiguration);
        
        boot(coordinator);
        getLog().info("Fabric3 test runtime booted");

    }

    /*
     * Boots the runtime, this starts the system SCDL and contributes the extensions.
     */
    private void boot(RuntimeLifecycleCoordinator<?, ?> coordinator) throws MojoExecutionException {
        
        try {
            coordinator.bootPrimordial();
            coordinator.initialize();
            Future<Void> future = coordinator.recover();
            future.get();
            future = coordinator.joinDomain(-1);
            future.get();
            future = coordinator.start();
            future.get();
        } catch (InitializationException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (ExecutionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (StartException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        
    }

    /*
     * Logs the contributions.
     */
    private void logContributions(ScanResult scanResult) {
        
        getLog().info("Test contribution:" + scanResult.getTestContribution().getLocation());
        getLog().info("Number of extension contributions: " + scanResult.getExtensionContributions().size());
        for (ContributionSource extensionContribution : scanResult.getExtensionContributions()) {
            getLog().info(extensionContribution.getLocation().toExternalForm());
        }
        getLog().info("Number of user contributions: " + scanResult.getUserContributions().size());
        for (ContributionSource userContribution : scanResult.getUserContributions()) {
            getLog().info(userContribution.getLocation().toExternalForm());
        }
        
    }

}
