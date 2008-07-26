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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.xml.namespace.QName;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.metadata.ResolutionGroup;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.surefire.report.BriefConsoleReporter;
import org.apache.maven.surefire.report.BriefFileReporter;
import org.apache.maven.surefire.report.Reporter;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.report.ReporterManager;
import org.apache.maven.surefire.report.XMLReporter;
import org.apache.maven.surefire.suite.SurefireTestSuite;
import org.apache.maven.surefire.testset.TestSetFailedException;

import org.fabric3.api.annotation.logging.Severe;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ValidationException;
import org.fabric3.host.domain.AssemblyException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.jmx.agent.Agent;
import org.fabric3.jmx.agent.DefaultAgent;
import org.fabric3.maven.runtime.ContextStartException;
import org.fabric3.maven.runtime.MavenCoordinator;
import org.fabric3.maven.runtime.MavenEmbeddedRuntime;
import org.fabric3.monitor.MonitorFactory;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.Constants;
import org.fabric3.spi.classloader.MultiParentClassLoader;

/**
 * Run integration tests on a SCA composite using an embedded Fabric3 runtime.
 *
 * @version $Rev$ $Date$
 * @goal test
 * @phase integration-test
 */
public class Fabric3ITestMojo extends AbstractMojo {

    public static final QName IMPLEMENTATION_JUNIT = new QName(Constants.FABRIC3_NS, "junit");
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
     * @parameter expression="0.5"
     */
    public String runtimeVersion;

    /**
     * Set of runtime extension artifacts that should be deployed to the runtime.
     *
     * @parameter
     */
    public Dependency[] extensions;

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
     *
     * @parameter expression="${component.org.fabric3.itest.ArtifactHelper}"
     * @required
     * @readonly
     */
    public ArtifactHelper artifactHelper;

    /**
     *
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
        
        artifactHelper.setLocalRepository(localRepository);
        artifactHelper.setProject(project);
        
        Log log = getLog();
        if (!testScdl.exists()) {
            log.info("No itest SCDL found, skipping integration tests");
            return;
        }
        if (skip) {
            log.info("Skipping integration tests by user request.");
            return;
        }
        MonitorFactory monitorFactory = new MavenMonitorFactory(log, "f3");

        URL testScdlURL;
        try {
            testScdlURL = testScdl.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }

        Set<Artifact> runtimeArtifacts = artifactHelper.calculateRuntimeArtifacts(runtimeVersion);
        Set<Artifact> hostArtifacts = artifactHelper.calculateHostArtifacts(runtimeArtifacts, shared);
        Set<Artifact> dependencies = artifactHelper.calculateDependencies();
        Set<URL> moduleDependencies = artifactHelper.calculateModuleDependencies(dependencies, hostArtifacts);
        
        ClassLoader parentClassLoader = getClass().getClassLoader();
        ClassLoader hostClassLoader = createHostClassLoader(parentClassLoader, hostArtifacts);
        ClassLoader bootClassLoader = createBootClassLoader(hostClassLoader, runtimeArtifacts);

        Thread.currentThread().setContextClassLoader(bootClassLoader);
        if (systemScdl == null) {
            systemScdl = bootClassLoader.getResource("META-INF/fabric3/embeddedMaven.composite");
        }
        if (intentsLocation == null) {
            intentsLocation = bootClassLoader.getResource("META-INF/fabric3/intents.xml");
        }

        log.info("Starting Embedded Fabric3 Runtime ...");
        // FIXME this should probably be an isolated classloader
        MavenEmbeddedRuntime runtime;
        MavenCoordinator coordinator;
        try {
            runtime = createRuntime(bootClassLoader, hostClassLoader, moduleDependencies, monitorFactory);
            coordinator = createHostComponent(MavenCoordinator.class, coordinatorImpl, bootClassLoader);
        } catch (Exception e) {
            throw new MojoExecutionException("Error creating fabric3 runtime", e);
        }
        try {
            coordinator.setIntentsLocation(intentsLocation);
            extensionHelper.processExtensions(coordinator, extensions, userExtensions, userExtensionsArchives);
            bootRuntime(coordinator, runtime, bootClassLoader, hostClassLoader);
        } catch (InitializationException e) {
            throw new MojoExecutionException("Error initializing Fabric3 Runtime", e);
        } catch (Exception e) {
            throw new MojoExecutionException("Error starting Fabric3 Runtime", e);
        }

        try {
            executeTests(log, testScdlURL, runtime);
        } finally {
            try {
                shutdownRuntime(coordinator);
            } catch (Exception e) {
                // ignore
            }
            log.info("Stopping Fabric3 Runtime ...");
        }
    }

    private void bootRuntime(RuntimeLifecycleCoordinator<MavenEmbeddedRuntime, Bootstrapper> coordinator,
                             MavenEmbeddedRuntime runtime,
                             ClassLoader bootClassLoader,
                             ClassLoader testClassLoader)
            throws Exception {
        ScdlBootstrapper bootstrapper = createHostComponent(ScdlBootstrapper.class, bootstrapperImpl, bootClassLoader);
        bootstrapper.setScdlLocation(systemScdl);
        URL systemConfig = getSystemConfig();
        bootstrapper.setSystemConfig(systemConfig);
        coordinator.bootPrimordial(runtime, bootstrapper, bootClassLoader, testClassLoader);
        coordinator.initialize();
        Future<Void> future = coordinator.joinDomain(-1);
        future.get();
        future = coordinator.recover();
        future.get();
        future = coordinator.start();
        future.get();
    }

    private void executeTests(Log log, URL testScdlURL, MavenEmbeddedRuntime runtime) throws MojoExecutionException, MojoFailureException {
        SurefireTestSuite testSuite;
        log.info("Deploying test composite from " + testScdl);
        try {
            if (compositeName == null) {
                testSuite = createTestSuite(runtime, testScdlURL);
            } else {
                testSuite = createTestSuite(runtime);
            }
        } catch (MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            // trap any other exception
            throw new MojoExecutionException("Error deploying test composite: " + testScdl, e);
        }
        log.info("Executing tests...");

        boolean success = runSurefire(testSuite);
        if (!success) {
            String msg = "There were test failures";
            throw new MojoFailureException(msg);
        }
    }

    private void shutdownRuntime(RuntimeLifecycleCoordinator<MavenEmbeddedRuntime, Bootstrapper> coordinator)
            throws ShutdownException, InterruptedException, ExecutionException {
        Future<Void> future = coordinator.shutdown();
        future.get();
    }

    private URL getSystemConfig() throws MalformedURLException, MojoExecutionException {
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

        return systemConfig.exists() ? systemConfig.toURL() : null;
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

    public boolean runSurefire(SurefireTestSuite testSuite) throws MojoExecutionException {
        
        try {
            Properties status = new Properties();
            boolean success = run(testSuite, status);
            getLog().debug("Test results: " + status);
            return success;
        } catch (ReporterException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (TestSetFailedException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        
    }

    public boolean run(SurefireTestSuite suite, Properties status) throws ReporterException, TestSetFailedException {
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
    }

    protected MavenEmbeddedRuntime createRuntime(ClassLoader bootClassLoader,
                                                 ClassLoader hostClassLoader,
                                                 Set<URL> moduleDependencies,
                                                 MonitorFactory monitorFactory)
            throws Exception {
        Properties hostProperties = properties != null ? properties : System.getProperties();
        MavenHostInfoImpl hostInfo = new MavenHostInfoImpl(URI.create(testDomain), hostProperties, moduleDependencies);

        MavenEmbeddedRuntime runtime = createHostComponent(MavenEmbeddedRuntime.class, runtimeImpl, bootClassLoader);
        runtime.setMonitorFactory(monitorFactory);
        runtime.setHostInfo(hostInfo);
        runtime.setHostClassLoader(hostClassLoader);

        runtime.setJMXDomain(managementDomain);

        // TODO Add better host JMX support from the next release
        agent = new DefaultAgent();
        runtime.setMBeanServer(agent.getMBeanServer());

        return runtime;

    }

    protected <T> T createHostComponent(Class<T> type, String impl, ClassLoader cl) throws Exception {
        Class<?> implClass = cl.loadClass(impl);
        return type.cast(implClass.newInstance());

    }

    protected SurefireTestSuite createTestSuite(MavenEmbeddedRuntime runtime, URL testScdlURL)
            throws DeploymentException, ContributionException, ContextStartException, MojoExecutionException {
        URI domain = URI.create(testDomain);
        Composite composite;
        try {
            composite = runtime.activate(getBuildDirectoryUrl(), testScdlURL);
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
        runtime.startContext(domain);
        return createTestSuite(runtime, composite, domain);
    }

    protected SurefireTestSuite createTestSuite(MavenEmbeddedRuntime runtime)
            throws ContributionException, DeploymentException, ContextStartException, MojoExecutionException {
        URI domain = URI.create(testDomain);
        QName qName = new QName(compositeNamespace, compositeName);
        try {
            Composite composite;
            composite = runtime.activate(getBuildDirectoryUrl(), qName);
            runtime.startContext(domain);
            return createTestSuite(runtime, composite, domain);
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

    protected SurefireTestSuite createTestSuite(MavenEmbeddedRuntime runtime,
                                                Composite composite,
                                                URI uriBase) throws MojoExecutionException {
        SCATestSuite suite = new SCATestSuite();

        Map<String, ComponentDefinition<? extends Implementation<?>>> components = composite.getComponents();
        for (Map.Entry<String, ComponentDefinition<? extends Implementation<?>>> entry : components.entrySet()) {
            String name = entry.getKey();
            ComponentDefinition<? extends Implementation<?>> definition = entry.getValue();
            Implementation<?> implementation = definition.getImplementation();
            if (IMPLEMENTATION_JUNIT.equals(implementation.getType())) {
                SCATestSet testSet = createTestSet(runtime, name, uriBase, definition);
                suite.add(testSet);
            }
        }
        return suite;
    }

    protected void reportContributionErrors(ValidationException cause) {
        StringBuilder b = new StringBuilder("\n\n");
        b.append("-------------------------------------------------------\n");
        b.append("CONTRIBUTION ERRORS\n");
        b.append("-------------------------------------------------------\n\n");
        b.append(cause.getMessage());
        getLog().error(b);
    }

    protected void reportDeploymentErrors(AssemblyException cause) {
        StringBuilder b = new StringBuilder("\n\n");
        b.append("-------------------------------------------------------\n");
        b.append("DEPLOYMENT ERRORS\n");
        b.append("-------------------------------------------------------\n\n");
        b.append(cause.getMessage());
        getLog().error(b);
    }

    protected SCATestSet createTestSet(MavenEmbeddedRuntime runtime,
                                       String name,
                                       URI contextId,
                                       ComponentDefinition<?> definition) throws MojoExecutionException {
        Implementation<?> impl = definition.getImplementation();
        PojoComponentType componentType = (PojoComponentType) impl.getComponentType();
        Map<String, ServiceDefinition> services = componentType.getServices();
        ServiceDefinition testService = services.get("testService");
        if (testService == null) {
            throw new MojoExecutionException("No testService defined on component: " + definition.getName());
        }
        List<? extends Operation<?>> operations = testService.getServiceContract().getOperations();
        return new SCATestSet(runtime, name, contextId, operations);
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

    private URL getBuildDirectoryUrl() {
        try {
            return buildDirectory.toURI().toURL();
        } catch (MalformedURLException e) {
            // this should not happen
            throw new AssertionError();
        }
    }

    public interface MojoMonitor {
        @Severe
        void runError(Exception e);
    }

}
