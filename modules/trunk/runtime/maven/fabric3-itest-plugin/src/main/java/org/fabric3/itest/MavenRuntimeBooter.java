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
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.fabric3.featureset.FeatureSet;
import org.fabric3.host.Names;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.host.runtime.StartException;
import org.fabric3.jmx.agent.Agent;
import org.fabric3.jmx.agent.DefaultAgent;
import org.fabric3.maven.MavenEmbeddedRuntime;
import org.xml.sax.InputSource;

/**
 * @version $Revision$ $Date$
 */
public class MavenRuntimeBooter {
    private static final String SYSTEM_CONFIG_XML_FILE = "systemConfig.xml";
    private static final String DEFAULT_SYSTEM_CONFIG_DIR = "test-classes" + File.separator + "META-INF" + File.separator;
    private static final String RUNTIME_IMPL = "org.fabric3.maven.runtime.MavenEmbeddedRuntimeImpl";
    private static final String BOOTSTRAPPER_IMPL = "org.fabric3.fabric.runtime.bootstrap.ScdlBootstrapperImpl";
    private static final String COORDINATOR_IMPL = "org.fabric3.fabric.runtime.DefaultCoordinator";
    private static final String DOMAIN = "fabric3://domain";

    // configuration elements
    private URL systemScdl;
    private URL intentsLocation;
    private String managementDomain;
    private Properties properties;
    private File outputDirectory;
    private String systemConfigDir;
    private String systemConfig;
    private ClassLoader bootClassLoader;
    private ClassLoader hostClassLoader;
    private Set<URL> moduleDependencies;
    private org.apache.maven.model.Dependency[] extensions;
    private List<FeatureSet> featureSets;
    private Log log;
    private List<URL> policyUrls;


    private RuntimeLifecycleCoordinator<MavenEmbeddedRuntime, ScdlBootstrapper> coordinator;
    private MavenEmbeddedRuntime runtime;
    private ExtensionHelper extensionHelper;

    public MavenRuntimeBooter(MavenBootConfiguration configuration) {
        systemScdl = configuration.getSystemScdl();
        intentsLocation = configuration.getIntentsLocation();
        managementDomain = configuration.getManagementDomain();
        properties = configuration.getProperties();
        outputDirectory = configuration.getOutputDirectory();
        systemConfigDir = configuration.getSystemConfigDir();
        systemConfig = configuration.getSystemConfig();
        bootClassLoader = configuration.getBootClassLoader();
        hostClassLoader = configuration.getHostClassLoader();
        moduleDependencies = configuration.getModuleDependencies();
        extensions = configuration.getExtensions();
        featureSets = configuration.getFeatureSets();
        log = configuration.getLog();
        extensionHelper = configuration.getExtensionHelper();
        policyUrls = configuration.getPolicyUrls();
    }

    @SuppressWarnings({"unchecked"})
    public MavenEmbeddedRuntime boot() throws MojoExecutionException {
        runtime = createRuntime();
        BootConfiguration<MavenEmbeddedRuntime, ScdlBootstrapper> configuration = createBootConfiguration();
        coordinator = instantiate(RuntimeLifecycleCoordinator.class, COORDINATOR_IMPL, bootClassLoader);
        coordinator.setConfiguration(configuration);
        bootRuntime();
        return runtime;
    }

    private MavenEmbeddedRuntime createRuntime() {
        MonitorFactory monitorFactory = new MavenMonitorFactory(log, "f3");
        MavenEmbeddedRuntime runtime = instantiate(MavenEmbeddedRuntime.class, RUNTIME_IMPL, bootClassLoader);
        runtime.setMonitorFactory(monitorFactory);
        runtime.setHostClassLoader(hostClassLoader);

        Properties hostProperties = properties != null ? properties : System.getProperties();
        MavenHostInfoImpl hostInfo = new MavenHostInfoImpl(URI.create(DOMAIN), hostProperties, moduleDependencies);
        runtime.setHostInfo(hostInfo);

        runtime.setJmxSubDomain(managementDomain);

        // TODO Add better host JMX support from the next release
        Agent agent = new DefaultAgent();
        runtime.setMBeanServer(agent.getMBeanServer());

        return runtime;
    }

    private BootConfiguration<MavenEmbeddedRuntime, ScdlBootstrapper> createBootConfiguration() throws MojoExecutionException {

        BootConfiguration<MavenEmbeddedRuntime, ScdlBootstrapper> configuration = new BootConfiguration<MavenEmbeddedRuntime, ScdlBootstrapper>();
        configuration.setBootClassLoader(bootClassLoader);

        // create the runtime bootrapper
        ScdlBootstrapper bootstrapper = createBootstrapper(bootClassLoader);
        configuration.setBootstrapper(bootstrapper);

        // add the boot libraries to export as contributions. This is necessary so extension contributions can import them
        List<URL> bootExports = new ArrayList<URL>();
        bootExports.add(bootClassLoader.getResource("META-INF/maven/org.codehaus.fabric3/fabric3-maven-api/pom.xml"));
        bootExports.add(bootClassLoader.getResource("META-INF/maven/org.codehaus.fabric3/fabric3-spi/pom.xml"));
        bootExports.add(bootClassLoader.getResource("META-INF/maven/org.codehaus.fabric3/fabric3-test-spi/pom.xml"));
        bootExports.add(bootClassLoader.getResource("META-INF/maven/org.codehaus.fabric3/fabric3-pojo/pom.xml"));
        configuration.setBootLibraryExports(bootExports);

        // process extensions
        extensionHelper.processExtensions(configuration, extensions, featureSets);

        // process the baseline intents
        if (intentsLocation == null) {
            intentsLocation = bootClassLoader.getResource("META-INF/fabric3/intents.xml");
        }
        ContributionSource source = new FileContributionSource(Names.CORE_INTENTS_CONTRIBUTION, intentsLocation, -1, new byte[0]);
        configuration.setIntents(source);
        configuration.setRuntime(runtime);
        
        List<ContributionSource> policyContributions = new LinkedList<ContributionSource>();
        for (URL policyUrl : policyUrls) {
            policyContributions.add(new FileContributionSource(Names.USER_POLICY_CONTRIBUTION, policyUrl, -1, new byte[0]));
        }
        configuration.setPolicies(policyContributions);
        
        return configuration;
    }

    private ScdlBootstrapper createBootstrapper(ClassLoader bootClassLoader) throws MojoExecutionException {
        ScdlBootstrapper bootstrapper = instantiate(ScdlBootstrapper.class, BOOTSTRAPPER_IMPL, bootClassLoader);
        if (systemScdl == null) {
            systemScdl = bootClassLoader.getResource("META-INF/fabric3/embeddedMaven.composite");
        }
        bootstrapper.setScdlLocation(systemScdl);
        if (systemConfig != null) {
            Reader reader = new StringReader(systemConfig);
            InputSource source = new InputSource(reader);
            bootstrapper.setSystemConfig(source);
        } else {
            URL systemConfig = getSystemConfig();
            bootstrapper.setSystemConfig(systemConfig);
        }
        return bootstrapper;
    }

    private void bootRuntime() throws MojoExecutionException {
        try {
            log.info("Starting Embedded Fabric3 Runtime ...");
            coordinator.bootPrimordial();
            coordinator.initialize();
            Future<Void> future = coordinator.recover();
            future.get();
            future = coordinator.joinDomain(-1);
            future.get();
            future = coordinator.start();
            future.get();
        } catch (StartException e) {
            throw new MojoExecutionException("Error booting Fabric3 runtime", e);
        } catch (ExecutionException e) {
            throw new MojoExecutionException("Error booting Fabric3 runtime", e);
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Error booting Fabric3 runtime", e);
        } catch (InitializationException e) {
            throw new MojoExecutionException("Error booting Fabric3 runtime", e);
        }
    }

    public void shutdown() throws ShutdownException, InterruptedException, ExecutionException {
        log.info("Stopping Fabric3 Runtime ...");
        Future<Void> future = coordinator.shutdown();
        future.get();
    }

    private <T> T instantiate(Class<T> type, String impl, ClassLoader cl) {
        try {
            Class<?> implClass = cl.loadClass(impl);
            return type.cast(implClass.newInstance());
        } catch (ClassNotFoundException e) {
            // programming errror
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            // programming errror
            throw new AssertionError(e);
        } catch (InstantiationException e) {
            // programming errror
            throw new AssertionError(e);
        }
    }

    private URL getSystemConfig() throws MojoExecutionException {
        File systemConfig = new File(outputDirectory, DEFAULT_SYSTEM_CONFIG_DIR + SYSTEM_CONFIG_XML_FILE);
        if (systemConfigDir != null) {
            systemConfig = new File(outputDirectory, systemConfigDir + File.separator + SYSTEM_CONFIG_XML_FILE);
            if (!systemConfig.exists()) {
                //The user has explicitly attempted to configure the system config location but the information is incorrect
                throw new MojoExecutionException("Failed to find the system config information in: " + systemConfig.getAbsolutePath());
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Using system config information from: " + systemConfig.getAbsolutePath());
        }

        try {
            return systemConfig.exists() ? systemConfig.toURL() : null;
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Invalid system configuration: " + systemConfig, e);
        }
    }


}
