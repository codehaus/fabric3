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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.surefire.report.BriefConsoleReporter;
import org.apache.maven.surefire.report.BriefFileReporter;
import org.apache.maven.surefire.report.Reporter;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.report.ReporterManager;
import org.apache.maven.surefire.suite.SurefireTestSuite;
import org.apache.maven.surefire.testset.TestSetFailedException;

import org.fabric3.api.annotation.LogLevel;
import org.fabric3.fabric.loader.LoaderContextImpl;
import static org.fabric3.fabric.runtime.ComponentNames.COMPOSITE_LOADER_URI;
import org.fabric3.fabric.runtime.ScdlBootstrapperImpl;
import org.fabric3.host.Fabric3RuntimeException;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.host.runtime.StartException;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.itest.implementation.junit.ImplementationJUnit;
import org.fabric3.spi.deployer.CompositeClassLoader;
import org.fabric3.pojo.processor.JavaMappedService;
import org.fabric3.pojo.processor.PojoComponentType;
import org.fabric3.spi.loader.ComponentTypeLoader;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.CompositeComponentType;
import org.fabric3.spi.model.type.CompositeImplementation;
import org.fabric3.spi.model.type.Implementation;
import org.fabric3.spi.model.type.Operation;

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
     * The name of the component that will be implemented by the test harness composite.
     *
     * @parameter expression="testHarness"
     * @required
     */
    public String testComponentName;

    /**
     * The location if the SCDL that defines the test harness composite. The source for this would normally be placed in
     * the test/resources directory and be copied by the resource plugin; this allows property substitution if
     * required.
     *
     * @parameter expression="${project.build.testOutputDirectory}/itest.scdl"
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
     * Set of extension artifacts that should be deployed to the runtime.
     *
     * @parameter
     */
    public Dependency[] extensions;

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

        ClassLoader cl = createHostClassLoader(getClass().getClassLoader(), extensions);
        Thread.currentThread().setContextClassLoader(cl);
        if (systemScdl == null) {
            systemScdl = cl.getResource("META-INF/fabric3/embeddedMaven.scdl");
        }

        log.info("Starting Embedded Fabric3 Runtime ...");
        MavenEmbeddedRuntime runtime = createRuntime(cl);
        MojoMonitor monitor = runtime.getMonitorFactory().getMonitor(MojoMonitor.class);
        // FIXME this should probably be an isolated classloader
        ClassLoader testClassLoader = createTestClassLoader(getClass().getClassLoader());
        RuntimeLifecycleCoordinator<MavenEmbeddedRuntime, Bootstrapper> coordinator;
        try {
            ScdlBootstrapper bootstrapper = new ScdlBootstrapperImpl();
            bootstrapper.setScdlLocation(systemScdl);
            coordinator = new MavenCoordinator();
            coordinator.bootPrimordial(runtime, bootstrapper, cl, testClassLoader);
            coordinator.initialize();
            Future<Void> future = coordinator.joinDomain(-1);
            future.get();
            future = coordinator.recover();
            future.get();
            future = coordinator.start();
            future.get();
        } catch (InitializationException e) {
            monitor.runError(e);
            throw new MojoExecutionException("Error initializing Fabric3 Runtime", e);
        } catch (StartException e) {
            monitor.runError(e);
            throw new MojoExecutionException("Error starting Fabric3 Runtime", e);
        } catch (ExecutionException e) {
            monitor.runError(e);
            throw new MojoExecutionException("Error starting Fabric3 Runtime", e);
        } catch (InterruptedException e) {
            monitor.runError(e);
            throw new MojoExecutionException("Error starting Fabric3 Runtime", e);
        }

        try {
            SurefireTestSuite testSuite;
            log.info("Deploying test SCDL from " + testScdl);
            try {
                // XML loading is externalized for the Mojo...this should be cleaned up to use the DSL when
                // it becomes available
                URI domain = URI.create(testDomain);

                CompositeImplementation impl = new CompositeImplementation();
                
                ComponentDefinition<CompositeImplementation> definition =
                        new ComponentDefinition<CompositeImplementation>(testComponentName, impl);

                @SuppressWarnings("unchecked")
                ComponentTypeLoader<CompositeImplementation> loader =
                        runtime.getSystemComponent(ComponentTypeLoader.class, COMPOSITE_LOADER_URI);

                LoaderContext loaderContext = new LoaderContextImpl(testClassLoader, testScdl.toURI().toURL());
                loader.load(impl, loaderContext);

                runtime.deploy(definition);
                testSuite = createTestSuite(runtime, definition, domain);
                runtime.startContext(domain);
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
                Future<Void> future = coordinator.shutdown();
                future.get();
            } catch (Fabric3RuntimeException e) {
                monitor.runError(e);
                throw new MojoExecutionException("Error shutting down Fabric3 Runtime", e);
            } catch (ExecutionException e) {
                monitor.runError(e);
                throw new MojoExecutionException("Error shutting down Fabric3 Runtime", e);
            } catch (InterruptedException e) {
                monitor.runError(e);
                throw new MojoExecutionException("Error shutting down Fabric3 Runtime", e);
            } catch (ShutdownException e) {
                monitor.runError(e);
                throw new MojoExecutionException("Error shutting down Fabric3 Runtime", e);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    protected ClassLoader createHostClassLoader(ClassLoader parent, Dependency[] extensions)
            throws MojoExecutionException {
        if (extensions == null || extensions.length == 0) {
            return parent;
        }

        Set<Artifact> artifacts = new HashSet<Artifact>();
        for (Dependency extension : extensions) {
            Artifact artifact = extension.getArtifact(artifactFactory);
            try {
                resolver.resolve(artifact, remoteRepositories, localRepository);
                ResolutionGroup resolutionGroup = metadataSource.retrieve(artifact,
                                                                          localRepository,
                                                                          remoteRepositories);
                ArtifactResolutionResult result = resolver.resolveTransitively(resolutionGroup.getArtifacts(),
                                                                               artifact,
                                                                               remoteRepositories,
                                                                               localRepository,
                                                                               metadataSource);
                artifacts.add(artifact);
                artifacts.addAll(result.getArtifacts());
            } catch (ArtifactResolutionException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            } catch (ArtifactNotFoundException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            } catch (ArtifactMetadataRetrievalException e) {
                throw new MojoExecutionException(e.getMessage(), e);
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

    protected MavenEmbeddedRuntime createRuntime(ClassLoader hostClassLoader) throws MojoExecutionException {
        MavenEmbeddedArtifactRepository artifactRepository = new MavenEmbeddedArtifactRepository(artifactFactory,
                                                                                                 resolver,
                                                                                                 metadataSource,
                                                                                                 localRepository,
                                                                                                 remoteRepositories);
        MavenHostInfoImpl hostInfo = new MavenHostInfoImpl(URI.create(testDomain), artifactRepository);
        MavenMonitorFactory monitorFactory = new MavenMonitorFactory(getLog());

        MavenEmbeddedRuntime runtime = new MavenEmbeddedRuntime();
        runtime.setMonitorFactory(monitorFactory);
        runtime.setHostInfo(hostInfo);
        runtime.setHostClassLoader(hostClassLoader);
        return runtime;
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
                                                ComponentDefinition<CompositeImplementation> definition,
                                                URI uriBase) throws MojoExecutionException {
        SCATestSuite suite = new SCATestSuite();

        CompositeImplementation impl = definition.getImplementation();
        CompositeComponentType componentType = impl.getComponentType();
        Map<String, ComponentDefinition<? extends Implementation<?>>> components = componentType.getComponents();
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
