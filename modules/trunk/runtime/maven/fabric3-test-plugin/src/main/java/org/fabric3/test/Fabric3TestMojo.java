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

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import org.fabric3.host.Names;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.host.runtime.StartException;
import org.fabric3.test.artifact.ArtifactHelper;
import org.fabric3.test.contribution.MavenContributionScanner;
import org.fabric3.test.contribution.MavenContributionScannerImpl;
import org.fabric3.test.contribution.ScanResult;
import org.fabric3.test.host.MavenHostInfo;
import org.fabric3.test.host.MavenHostInfoImpl;
import org.fabric3.test.monitor.MavenMonitorFactory;
import org.fabric3.test.runtime.MavenRuntime;
import org.fabric3.test.runtime.MavenRuntimeImpl;

/**
 * Fabric3 Mojo for testing SCA services.
 * 
 * @goal test
 * @phase integration-test
 * @execute phase="integration-test"
 *
 */
public class Fabric3TestMojo extends AbstractMojo {
    
    private static final URI DOMAIN_URI = URI.create("fabric3://domain");

    /**
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
     * POM
     *
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    protected MavenProject mavenProject;

    /**
     * The version of the runtime to use.
     *
     * @parameter expression="0.8-SNAPSHOT"
     */
    public String runtimeVersion;

    /**
     * Properties passed to the runtime throught the HostInfo interface.
     *
     * @parameter
     */
    public Properties properties;
    
    private ClassLoader createHostClassLoader() throws MojoExecutionException {
        
        Set<URL> hostClasspath = artifactHelper.resolve("org.codehaus.fabric3", "fabric3-api", runtimeVersion, Artifact.SCOPE_RUNTIME, "jar");
        hostClasspath.addAll(artifactHelper.resolve("org.codehaus.fabric3", "fabric3-host-api", runtimeVersion, Artifact.SCOPE_RUNTIME, "jar"));
        hostClasspath.addAll(artifactHelper.resolve("javax.servlet", "servlet-api", "2.4", Artifact.SCOPE_RUNTIME, "jar"));
        
        return new URLClassLoader(hostClasspath.toArray(new URL[] {}));
        
    }
    
    private ClassLoader createBootClassLoader(ClassLoader hostClassLoader) throws MojoExecutionException {
        
        Set<URL> hostClasspath = artifactHelper.resolve("org.codehaus.fabric3", "fabric3-fabric", runtimeVersion, Artifact.SCOPE_RUNTIME, "jar");
        hostClasspath.addAll(artifactHelper.resolve("org.codehaus.fabric3", "fabric3-policy", runtimeVersion, Artifact.SCOPE_RUNTIME, "jar"));
        hostClasspath.addAll(artifactHelper.resolve("org.codehaus.fabric3", "fabric3-jmx-agent", runtimeVersion, Artifact.SCOPE_RUNTIME, "jar"));
        hostClasspath.addAll(artifactHelper.resolve("org.codehaus.fabric3", "fabric3-thread-pool", runtimeVersion, Artifact.SCOPE_RUNTIME, "jar"));
        
        return new URLClassLoader(hostClasspath.toArray(new URL[] {}), hostClassLoader);
        
    }

    /**
     * Contributes scanned contributions and run the tests.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        
        artifactHelper.setRepositories(localRepository, mavenProject.getRemoteArtifactRepositories());
        
        // Boot classloader is the plugin classloader
        ClassLoader bootClassLoader = getClass().getClassLoader();
        
        MavenContributionScanner scanner = new MavenContributionScannerImpl();
        ScanResult scanResult = scanner.scan(mavenProject);        
        logContributions(scanResult);
        
        MavenRuntime runtime = null;
        MonitorFactory monitorFactory = new MavenMonitorFactory(getLog(), "f3");
        runtime.setMonitorFactory(monitorFactory);
        runtime.setHostClassLoader(null);
        
        MavenHostInfo mavenHostInfo = new MavenHostInfoImpl(DOMAIN_URI, properties);
        runtime.setHostInfo(mavenHostInfo);

        // TODO Add better host JMX support from the next release
        //Agent agent = new DefaultAgent();
        //runtime.setMBeanServer(agent.getMBeanServer());
        
        //BootConfiguration<MavenRuntime, ScdlBootstrapper> bootConfiguration = new BootConfiguration<MavenRuntime, ScdlBootstrapper>();
        BootConfiguration<MavenRuntime, ScdlBootstrapper> bootConfiguration = null;
        bootConfiguration.setExtensions(scanResult.getExtensionContributions());
        bootConfiguration.setRuntime(runtime);
        bootConfiguration.setBootClassLoader(bootClassLoader);
        
        URL intentsLocation = getClass().getClassLoader().getResource("/META-INF/fabric3/intents.xml");
        ContributionSource source = new FileContributionSource(Names.CORE_INTENTS_CONTRIBUTION, intentsLocation, -1, new byte[0]);
        bootConfiguration.setIntents(source);
        
        Map<String, String> exportedPackages = new HashMap<String, String>();
        exportedPackages.put("org.fabric3.spi.*", Names.VERSION);
        exportedPackages.put("org.fabric3.host.*", Names.VERSION);
        exportedPackages.put("org.fabric3.management.*", Names.VERSION);
        exportedPackages.put("org.fabric3.model.*", Names.VERSION);
        exportedPackages.put("org.fabric3.pojo.*", Names.VERSION);
        exportedPackages.put("org.fabric3.test.spi", Names.VERSION);
        exportedPackages.put("org.fabric3.maven", Names.VERSION);
        bootConfiguration.setExportedPackages(exportedPackages);
        
        //ScdlBootstrapper bootstrapper = new ScdlBootstrapperImpl();
        ScdlBootstrapper bootstrapper = null;
        URL systemScdl = getClass().getClassLoader().getResource("META-INF/fabric3/embeddedMaven.composite");
        bootstrapper.setScdlLocation(systemScdl);
        bootConfiguration.setBootstrapper(bootstrapper);
        
        //RuntimeLifecycleCoordinator<MavenRuntime, ScdlBootstrapper> coordinator = new DefaultCoordinator<MavenRuntime, ScdlBootstrapper>();
        RuntimeLifecycleCoordinator<MavenRuntime, ScdlBootstrapper> coordinator = null;
        coordinator.setConfiguration(bootConfiguration);
        
        boot(coordinator);
        getLog().info("Fabric3 test runtime booted");
        
        runtime.deploy(scanResult.getUserContributions());

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
