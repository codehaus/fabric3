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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.surefire.report.BriefConsoleReporter;
import org.apache.maven.surefire.report.BriefFileReporter;
import org.apache.maven.surefire.report.Reporter;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.report.ReporterManager;
import org.apache.maven.surefire.report.XMLReporter;
import org.apache.maven.surefire.suite.SurefireTestSuite;
import org.apache.maven.surefire.testset.TestSetFailedException;

import org.fabric3.api.annotation.LogLevel;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.junit.ImplementationJUnit;
import org.fabric3.maven.runtime.MavenCoordinator;
import org.fabric3.maven.runtime.MavenEmbeddedRuntime;
import org.fabric3.pojo.scdl.JavaMappedService;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Operation;
import org.fabric3.spi.deployer.CompositeClassLoader;

/**
 * Integration-tests an SCA composite by running it in local copy of Fabric3 and calling JUnit-based test components to
 * exercise it.
 *
 * @version $Rev$ $Date$
 * @goal test
 * @phase integration-test
 */
public class Fabric3ITestMojo extends AbstractMojo {
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
     * The location if the SCDL that defines the test harness composite. The source for this would normally be placed in
     * the test/resources directory and be copied by the resource plugin; this allows property substitution if
     * required.
     *
     * @parameter expression="${project.build.testOutputDirectory}/itest.composite"
     */
    public File testScdl;

    /**
     * The location of the SCDL that configures the Fabric3 runtime. This allows the default runtime configuration
     * supplied in this plugin to be overridden.
     *
     * @parameter
     */
    public URL systemScdl;

    /**
     * Class name for the implementation of the runtime to use.
     * @parameter expression="org.fabric3.maven.runtime.impl.MavenEmbeddedRuntimeImpl"
     */
    public String runtimeImpl;

    /**
     * Class name for the implementation of the bootstrapper to use.
     * @parameter expression="org.fabric3.fabric.runtime.ScdlBootstrapperImpl"
     */
    public String bootstrapperImpl;

    /**
     * Class name for the implementation of the coordinator to use.
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
     * @parameter expression="0.4-SNAPSHOT"
     */
    public String runtimeVersion;

    /**
     * Set of extension artifacts that should be deployed to the runtime.
     *
     * @parameter
     */
    public Dependency[] contributions;

    /**
     * Set of extension artifacts that should be deployed to the runtime.
     *
     * @parameter
     */
    public Dependency[] extensions;

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
     * Used to look up Artifacts in the remote repository.
     *
     * @parameter expression="${component.org.apache.maven.artifact.resolver.ArtifactResolver}"
     * @required
     * @readonly
     */
    public ArtifactResolver resolver;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @parameter expression="${component.org.apache.maven.artifact.metadata.ArtifactMetadataSource}"
     * @required
     * @readonly
     */
    public ArtifactMetadataSource metadataSource;

    /**
     * Location of the local repository.
     *
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    public ArtifactRepository localRepository;

    /**
     * List of Remote Repositories used by the resolver
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    public List remoteRepositories;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @parameter expression="${component.org.apache.maven.artifact.factory.ArtifactFactory}"
     * @required
     * @readonly
     */
    public ArtifactFactory artifactFactory;

    @SuppressWarnings({"ThrowFromFinallyBlock"})
    public void execute() throws MojoExecutionException, MojoFailureException {
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
        MojoMonitor monitor = monitorFactory.getMonitor(MojoMonitor.class);

        URL testScdlURL;
        try {
            testScdlURL = testScdl.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new AssertionError();
        }

        ClassLoader hostClassLoader = createHostClassLoader(getClass().getClassLoader(), runtimeVersion, extensions);
        Thread.currentThread().setContextClassLoader(hostClassLoader);
        if (systemScdl == null) {
            systemScdl = hostClassLoader.getResource("META-INF/fabric3/embeddedMaven.composite");
        }
        if (intentsLocation == null) {
            intentsLocation = hostClassLoader.getResource("META-INF/fabric3/intents.xml");
        }

        List<URI> contributionURIs = resolveDependencies(contributions);


        log.info("Starting Embedded Fabric3 Runtime ...");
        // FIXME this should probably be an isolated classloader
        ClassLoader testClassLoader = createTestClassLoader(hostClassLoader);
        MavenEmbeddedRuntime runtime;
        MavenCoordinator coordinator;
        try {
            runtime = createRuntime(hostClassLoader, monitorFactory);
            coordinator = createHostComponent(MavenCoordinator.class, coordinatorImpl, hostClassLoader);
        } catch (Exception e) {
            throw new MojoExecutionException("Error creating fabric3 runtime", e);
        }
        try {
            coordinator.setExtensions(contributionURIs);
            coordinator.setIntentsLocation(intentsLocation);
            bootRuntime(coordinator, runtime, hostClassLoader, testClassLoader);
        } catch (InitializationException e) {
            monitor.runError(e);
            throw new MojoExecutionException("Error initializing Fabric3 Runtime", e);
        } catch (Exception e) {
            monitor.runError(e);
            throw new MojoExecutionException("Error starting Fabric3 Runtime", e);
        }

        try {
            SurefireTestSuite testSuite;
            log.info("Deploying test SCDL from " + testScdl);
            try {
                testSuite = createTestSuite(runtime, testScdlURL, testClassLoader);
            } catch (Exception e) {
                monitor.runError(e);
                throw new MojoExecutionException("Error deploying test component " + testScdl, e);
            }
            log.info("Executing tests...");

            boolean success = runSurefire(testSuite);
            if (!success) {
                String msg = "There were test failures";
                throw new MojoFailureException(msg);
            }
        } finally {
            log.info("Stopping Fabric3 Runtime ...");
            try {
                shutdownRuntime(coordinator);
            } catch (Exception e) {
                monitor.runError(e);
            }
        }
    }

    private List<URI> resolveDependencies(Dependency[] dependencies) {
        if (dependencies == null) {
            return Collections.emptyList();
        }
        List<URI> uris = new ArrayList<URI>(dependencies.length);
        for (Dependency dependency : dependencies) {
            uris.add(dependency.getURI());
        }
        return uris;
    }

    private void shutdownRuntime(RuntimeLifecycleCoordinator<MavenEmbeddedRuntime, Bootstrapper> coordinator)
            throws ShutdownException, InterruptedException, ExecutionException {
        Future<Void> future = coordinator.shutdown();
        future.get();
    }

    private void bootRuntime(RuntimeLifecycleCoordinator<MavenEmbeddedRuntime, Bootstrapper> coordinator,
                             MavenEmbeddedRuntime runtime,
                             ClassLoader hostClassLoader,
                             ClassLoader testClassLoader)
            throws Exception {
        ScdlBootstrapper bootstrapper = createHostComponent(ScdlBootstrapper.class, bootstrapperImpl, hostClassLoader);
        bootstrapper.setScdlLocation(systemScdl);
        coordinator.bootPrimordial(runtime, bootstrapper, hostClassLoader, testClassLoader);
        coordinator.initialize();
        Future<Void> future = coordinator.joinDomain(-1);
        future.get();
        future = coordinator.recover();
        future.get();
        future = coordinator.start();
        future.get();
    }

    protected ClassLoader createHostClassLoader(ClassLoader parent, String runtimeVersion, Dependency[] extensions)
            throws MojoExecutionException {
        Set<Artifact> artifacts = new HashSet<Artifact>();
        // add in the runtime
        Set<Exclusion> exclusions = Collections.emptySet();
        addArtifacts(artifacts, new Dependency("org.codehaus.fabric3", "fabric3-maven-host", runtimeVersion, exclusions));

        // add in the common extensions
        if (extensions != null) {
            for (Dependency extension : extensions) {
                addArtifacts(artifacts, extension);
            }
        }
        
        URL[] urls = new URL[artifacts.size()];
        int i = 0;
        for (Artifact artifact : artifacts) {
            File file = artifact.getFile();
            assert file != null;
            try {
                urls[i++] = file.toURI().toURL();
            } catch (MalformedURLException e) {
                // toURI should have made this valid
                throw new AssertionError();
            }
        }

        Log log = getLog();
        if (log.isDebugEnabled()) {
            log.debug("Fabric3 extension classpath:");
            for (URL url : urls) {
                log.debug("  " + url);
            }
        }

        return new CompositeClassLoader(URI.create("itestHost"), urls, parent);
    }

    private void addArtifacts(Set<Artifact> artifacts, Dependency extension) throws MojoExecutionException {
        final Set<Exclusion> exclusions = extension.getExclusions();

        Artifact artifact = extension.getArtifact(artifactFactory);
        try {
            resolver.resolve(artifact, remoteRepositories, localRepository);
            ResolutionGroup resolutionGroup = metadataSource.retrieve(artifact,
                                                                      localRepository,
                                                                      remoteRepositories);
            ArtifactFilter filter = new ArtifactFilter() {

                public boolean include(Artifact artifact) {
                    String groupId = artifact.getGroupId();
                    String artifactId = artifact.getArtifactId();

                    for (Exclusion exclusion : exclusions) {
                        if (artifactId.equals(exclusion.getArtifactId()) && groupId.equals(exclusion.getGroupId())) {
                            return false;
                        }
                    }
                    return true;
                }

            };
            ArtifactResolutionResult result = resolver.resolveTransitively(resolutionGroup.getArtifacts(),
                                                                           artifact,
                                                                           Collections.emptyMap(),
                                                                           localRepository,
                                                                           remoteRepositories,
                                                                           metadataSource,
                                                                           filter);
            @SuppressWarnings("unchecked")
            Set<Artifact> resolvedArtifacts = result.getArtifacts();
            artifacts.add(artifact);
            artifacts.addAll(resolvedArtifacts);
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (ArtifactMetadataRetrievalException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
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

    protected MavenEmbeddedRuntime createRuntime(ClassLoader hostClassLoader, MonitorFactory monitorFactory) throws Exception {
        Properties hostProperties = properties != null ? properties : System.getProperties();
        MavenHostInfoImpl hostInfo = new MavenHostInfoImpl(URI.create(testDomain), hostProperties);

        MavenEmbeddedRuntime runtime = createHostComponent(MavenEmbeddedRuntime.class, runtimeImpl, hostClassLoader);
        runtime.setMonitorFactory(monitorFactory);
        runtime.setHostInfo(hostInfo);
        runtime.setHostClassLoader(hostClassLoader);
        return runtime;
    }

    protected <T> T createHostComponent(Class<T> type, String impl, ClassLoader cl) throws Exception {
        Class<?> implClass = cl.loadClass(impl);
        return type.cast(implClass.newInstance());

    }

    public ClassLoader createTestClassLoader(ClassLoader parent) {
        URL[] urls = new URL[testClassPath.size()];
        int idx = 0;
        for (String s : testClassPath) {
            File pathElement = new File(s);
            try {
                URL url = pathElement.toURI().toURL();
                getLog().debug("Adding application URL: " + url);
                urls[idx++] = url;
            } catch (MalformedURLException e) {
                // toURI should have encoded the URL
                throw new AssertionError();
            }

        }
        return new URLClassLoader(urls, parent);
    }

    protected SurefireTestSuite createTestSuite(MavenEmbeddedRuntime runtime,
                                              URL testScdlURL,
                                              ClassLoader testClassLoader) throws Exception {
        // XML loading is externalized for the Mojo...this should be cleaned up to use the DSL when
        // it becomes available
        URI domain = URI.create(testDomain);

        Composite composite = runtime.load(testClassLoader, testScdlURL);
        runtime.deploy(composite);
        runtime.startContext(domain);
        return createTestSuite(runtime, composite, domain);
    }

    protected SurefireTestSuite createTestSuite(MavenEmbeddedRuntime runtime,
                                                Composite composite,
                                                URI uriBase) throws MojoExecutionException {
        SCATestSuite suite = new SCATestSuite();

        Map<String, ComponentDefinition<? extends Implementation<?>>> components = composite.getComponents();
        for (Map.Entry<String, ComponentDefinition<? extends Implementation<?>>> entry : components.entrySet()) {
            String name = entry.getKey();
            ComponentDefinition<? extends Implementation<?>> junitDefinition = entry.getValue();
            Implementation<?> implementation = junitDefinition.getImplementation();
            if (ImplementationJUnit.class.isAssignableFrom(implementation.getClass())) {
                SCATestSet testSet = createTestSet(runtime, name, uriBase, junitDefinition);
                suite.add(testSet);
            }
        }
        return suite;
    }

    protected SCATestSet createTestSet(MavenEmbeddedRuntime runtime,
                                       String name,
                                       URI contextId,
                                       ComponentDefinition definition) throws MojoExecutionException {
        ImplementationJUnit impl = (ImplementationJUnit) definition.getImplementation();
        PojoComponentType componentType = impl.getComponentType();
        Map services = componentType.getServices();
        JavaMappedService testService = (JavaMappedService) services.get("testService");
        if (testService == null) {
            throw new MojoExecutionException("No testService defined on component: " + definition.getName());
        }
        List<? extends Operation<?>> operations = testService.getServiceContract().getOperations();
        return new SCATestSet(runtime, name, contextId, operations);
    }

    public interface MojoMonitor {
        @LogLevel("SEVERE")
        void runError(Exception e);
    }

}
