/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.itest;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import org.fabric3.api.annotation.logging.Severe;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.host.runtime.StartException;
import org.fabric3.jmx.agent.Agent;
import org.fabric3.jmx.agent.DefaultAgent;
import org.fabric3.maven.runtime.MavenEmbeddedRuntime;
import org.fabric3.monitor.MonitorFactory;
import org.fabric3.spi.classloader.MultiParentClassLoader;

/**
 * Run integration tests on a SCA composite using an embedded Fabric3 runtime.
 *
 * @version $Rev$ $Date$
 * @goal test
 * @phase integration-test
 */
public class Fabric3ITestMojo extends AbstractMojo {
    private static final String SYSTEM_CONFIG_XML_FILE = "systemConfig.xml";
    private static final String DEFAULT_SYSTEM_CONFIG_DIR = "test-classes" + File.separator + "META-INF" + File.separator;

    /**
     * POM
     *
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    protected MavenProject project;

    /**
     * Optional parameter for management domain.
     *
     * @parameter
     */
    public String managementDomain = "itest-host";

    /**
     * The optional target namespace of the composite to activate.
     *
     * @parameter
     */
    public String compositeNamespace;

    /**
     * The local name of the composite to activate, which may be null if testScdl is defined.
     *
     * @parameter
     */
    public String compositeName;

    /**
     * The location if the SCDL that defines the test harness composite. The source for this would normally be placed in the test/resources directory
     * and be copied by the resource plugin; this allows property substitution if required.
     *
     * @parameter expression="${project.build.testOutputDirectory}/itest.composite"
     */
    public File testScdl;

    /**
     * test composite .
     *
     * @parameter expression="${project.build.directory}"
     */
    public File buildDirectory;

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
     * The directory containing generated test classes of the project being tested.
     *
     * @parameter expression="${project.build.testOutputDirectory}"
     * @required
     */
    public File testClassesDirectory;

    /**
     * The SCA domain in which to deploy the test components.
     *
     * @parameter expression="fabric3://./domain"
     * @required
     */
    public String testDomain;

    /**
     * The location of the SCDL that configures the Fabric3 runtime. This allows the default runtime configuration supplied in this plugin to be
     * overridden.
     *
     * @parameter
     */
    public URL systemScdl;

    /**
     * Class name for the implementation of the runtime to use.
     *
     * @parameter expression="org.fabric3.maven.runtime.impl.MavenEmbeddedRuntimeImpl"
     */
    public String runtimeImpl;

    /**
     * Class name for the implementation of the bootstrapper to use.
     *
     * @parameter expression="org.fabric3.fabric.runtime.bootstrap.ScdlBootstrapperImpl"
     */
    public String bootstrapperImpl;

    /**
     * Class name for the implementation of the coordinator to use.
     *
     * @parameter expression="org.fabric3.maven.runtime.impl.MavenCoordinatorImpl"
     */
    public String coordinatorImpl;

    /**
     * The location of the default intents file for the Fabric3 runtime.
     *
     * @parameter
     */
    public URL intentsLocation;

    /**
     * The version of the runtime to use.
     *
     * @parameter expression="0.6"
     */
    public String runtimeVersion;

    /**
     * Set of runtime extension artifacts that should be deployed to the runtime.
     *
     * @parameter
     */
    public Dependency[] extensions;

    /**
     * Set of runtime extension artifacts that should be deployed to the runtime expressed as feature sets.
     *
     * @parameter
     */
    public Dependency[] features;

    /**
     * Set of user extension artifacts that should be deployed to the runtime.
     *
     * @parameter
     */
    public Dependency[] userExtensions;

    /**
     * Set of user extension artifacts that are not Maven artifacts.
     *
     * @parameter
     */
    public File[] userExtensionsArchives;

    /**
     * Libraries available to application and runtime.
     *
     * @parameter
     */
    public Dependency[] shared;

    /**
     * Properties passed to the runtime throught the HostInfo interface.
     *
     * @parameter
     */
    public Properties properties;

    /**
     * @parameter expression="${project.testClasspathElements}"
     * @required
     * @readonly
     */
    public List<String> testClassPath;

    /**
     * Location of the local repository.
     *
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    public ArtifactRepository localRepository;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @parameter expression="${component.org.apache.maven.artifact.factory.ArtifactFactory}"
     * @required
     * @readonly
     */
    public ArtifactFactory artifactFactory;

    /**
     * @parameter expression="${component.org.fabric3.itest.ArtifactHelper}"
     * @required
     * @readonly
     */
    public ArtifactHelper artifactHelper;

    /**
     * @parameter expression="${component.org.fabric3.itest.ExtensionHelper}"
     * @required
     * @readonly
     */
    public ExtensionHelper extensionHelper;

    /**
     * The sub-directory of the project's output directory which contains the systemConfig.xml file. Users are limited to specifying the (relative)
     * directory name in this param - the file name is fixed. The fixed name is not required by the itest environment but using it retains the
     * relationship between the test config file and WEB-INF/systemConfig.xml which contains the same information for the deployed composite
     *
     * @parameter
     */
    public String systemConfigDir;

    /**
     * Build output directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    protected File outputDirectory;

    // JMX management agent
    private Agent agent;


    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (!testScdl.exists()) {
            getLog().info("No itest SCDL found, skipping integration tests");
            return;
        }
        if (skip) {
            getLog().info("Skipping integration tests by user request.");
            return;
        }

        artifactHelper.setLocalRepository(localRepository);
        artifactHelper.setProject(project);

        Set<Artifact> runtimeArtifacts = artifactHelper.calculateRuntimeArtifacts(runtimeVersion);
        Set<Artifact> hostArtifacts = artifactHelper.calculateHostArtifacts(runtimeArtifacts, shared);
        Set<Artifact> dependencies = artifactHelper.calculateDependencies();
        Set<URL> moduleDependencies = artifactHelper.calculateModuleDependencies(dependencies, hostArtifacts);

        ClassLoader parentClassLoader = getClass().getClassLoader();
        ClassLoader hostClassLoader = createHostClassLoader(parentClassLoader, hostArtifacts);
        ClassLoader bootClassLoader = createBootClassLoader(hostClassLoader, runtimeArtifacts);

        Thread.currentThread().setContextClassLoader(bootClassLoader);

        MavenEmbeddedRuntime runtime = createRuntime(bootClassLoader, hostClassLoader, moduleDependencies);
        BootConfiguration<MavenEmbeddedRuntime, ScdlBootstrapper> configuration =
                createBootConfiguration(runtime, bootClassLoader, hostClassLoader);
        RuntimeLifecycleCoordinator<MavenEmbeddedRuntime, ScdlBootstrapper> coordinator =
                instantiate(RuntimeLifecycleCoordinator.class, coordinatorImpl, bootClassLoader);
        coordinator.setConfiguration(configuration);
        bootRuntime(coordinator);
        try {
            TestRunner runner = new TestRunner(testDomain,
                                               compositeNamespace,
                                               compositeName,
                                               testScdl,
                                               reportsDirectory,
                                               trimStackTrace,
                                               buildDirectory,
                                               getLog());
            runner.executeTests(runtime);
        } finally {
            try {
                shutdownRuntime(coordinator);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private void bootRuntime(RuntimeLifecycleCoordinator<MavenEmbeddedRuntime, ScdlBootstrapper> coordinator) throws MojoExecutionException {
        try {
            getLog().info("Starting Embedded Fabric3 Runtime ...");
            coordinator.bootPrimordial();
            coordinator.initialize();
            Future<Void> future = coordinator.joinDomain(-1);
            future.get();
            future = coordinator.recover();
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

    private void shutdownRuntime(RuntimeLifecycleCoordinator<MavenEmbeddedRuntime, ScdlBootstrapper> coordinator)
            throws ShutdownException, InterruptedException, ExecutionException {
        getLog().info("Stopping Fabric3 Runtime ...");
        Future<Void> future = coordinator.shutdown();
        future.get();
    }

    private BootConfiguration<MavenEmbeddedRuntime, ScdlBootstrapper> createBootConfiguration(MavenEmbeddedRuntime runtime,
                                                                                              ClassLoader bootClassLoader,
                                                                                              ClassLoader appClassLoader)
            throws MojoExecutionException {

        BootConfiguration<MavenEmbeddedRuntime, ScdlBootstrapper> configuration = new BootConfiguration<MavenEmbeddedRuntime, ScdlBootstrapper>();
        configuration.setAppClassLoader(appClassLoader);
        configuration.setBootClassLoader(bootClassLoader);

        // create the runtime bootrapper
        ScdlBootstrapper bootstrapper = createBootstrapper(bootClassLoader);
        configuration.setBootstrapper(bootstrapper);

        // add the boot libraries to export as contributions. This is necessary so extension contributions can import them
        List<String> bootExports = new ArrayList<String>();
        bootExports.add("META-INF/maven/org.codehaus.fabric3/fabric3-spi/pom.xml");
        bootExports.add("META-INF/maven/org.codehaus.fabric3/fabric3-pojo/pom.xml");
        bootExports.add("META-INF/maven/org.codehaus.fabric3/fabric3-java/pom.xml");
        configuration.setBootLibraryExports(bootExports);

        // process extensions
        extensionHelper.processExtensions(configuration, extensions, features, userExtensions, userExtensionsArchives);

        // process the baseline intents
        try {
            if (intentsLocation == null) {
                intentsLocation = bootClassLoader.getResource("META-INF/fabric3/intents.xml");
            }
            URI uri = intentsLocation.toURI();
            ContributionSource source = new FileContributionSource(uri, intentsLocation, -1, new byte[0]);
            configuration.setIntents(source);
        } catch (URISyntaxException e) {
            // should not happen
            throw new IllegalArgumentException(e);
        }
        configuration.setRuntime(runtime);
        return configuration;
    }

    private ScdlBootstrapper createBootstrapper(ClassLoader bootClassLoader) throws MojoExecutionException {
        ScdlBootstrapper bootstrapper = instantiate(ScdlBootstrapper.class, bootstrapperImpl, bootClassLoader);
        if (systemScdl == null) {
            systemScdl = bootClassLoader.getResource("META-INF/fabric3/embeddedMaven.composite");
        }
        bootstrapper.setScdlLocation(systemScdl);
        URL systemConfig = getSystemConfig();
        bootstrapper.setSystemConfig(systemConfig);
        return bootstrapper;
    }

    private MavenEmbeddedRuntime createRuntime(ClassLoader bootClassLoader, ClassLoader hostClassLoader, Set<URL> moduleDependencies) {
        MonitorFactory monitorFactory = new MavenMonitorFactory(getLog(), "f3");
        MavenEmbeddedRuntime runtime = instantiate(MavenEmbeddedRuntime.class, runtimeImpl, bootClassLoader);
        runtime.setMonitorFactory(monitorFactory);
        runtime.setHostClassLoader(hostClassLoader);

        Properties hostProperties = properties != null ? properties : System.getProperties();
        MavenHostInfoImpl hostInfo = new MavenHostInfoImpl(URI.create(testDomain), hostProperties, moduleDependencies);
        runtime.setHostInfo(hostInfo);

        runtime.setJMXDomain(managementDomain);

        // TODO Add better host JMX support from the next release
        agent = new DefaultAgent();
        runtime.setMBeanServer(agent.getMBeanServer());

        return runtime;
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

    private ClassLoader createBootClassLoader(ClassLoader parent, Set<Artifact> artifacts)
            throws MojoExecutionException {
        URL[] urls = new URL[artifacts.size()];
        int i = 0;
        for (Artifact artifact : artifacts) {
            File file = artifact.getFile();
            assert file != null;
            try {
                urls[i++] = file.toURI().toURL();
            } catch (MalformedURLException e) {
                // toURI should have made this valid
                throw new AssertionError(e);
            }
        }

        Log log = getLog();
        if (log.isDebugEnabled()) {
            log.debug("Fabric3 extension classpath:");
            for (URL url : urls) {
                log.debug("  " + url);
            }
        }
        return new MultiParentClassLoader(URI.create("itestBoot"), urls, parent);
    }

    /**
     * Creates the host classloader based on the given set of artifacts.
     *
     * @param parent        the parent classloader
     * @param hostArtifacts the  artifacts
     * @return the host classloader
     */
    private ClassLoader createHostClassLoader(ClassLoader parent, Set<Artifact> hostArtifacts) {
        List<URL> urls = new ArrayList<URL>(hostArtifacts.size());
        for (Artifact artifact : hostArtifacts) {
            try {
                File pathElement = artifact.getFile();
                URL url = pathElement.toURI().toURL();
                getLog().debug("Adding artifact URL: " + url);
                urls.add(url);
            } catch (MalformedURLException e) {
                // toURI should have encoded the URL
                throw new AssertionError(e);
            }

        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
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

        Log log = getLog();
        if (log.isDebugEnabled()) {
            log.debug("Using system config information from: " + systemConfig.getAbsolutePath());
        }

        try {
            return systemConfig.exists() ? systemConfig.toURL() : null;
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("Invalid system configuration: " + systemConfig, e);
        }
    }

    public interface MojoMonitor {
        @Severe
        void runError(Exception e);
    }

}
