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

import org.fabric3.api.annotation.LogLevel;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.junit.ImplementationJUnit;
import org.fabric3.maven.runtime.MavenCoordinator;
import org.fabric3.maven.runtime.MavenEmbeddedRuntime;
import org.fabric3.monitor.MonitorFactory;
import org.fabric3.pojo.scdl.JavaMappedService;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Operation;
import org.fabric3.spi.deployer.CompositeClassLoader;

/**
 * Run integration tests on a SCA composite using an embedded Fabric3 runtime.
 *
 * @version $Rev$ $Date$
 * @goal test
 * @phase integration-test
 */
public class Fabric3ITestMojo extends AbstractMojo {

    /**
     * POM
     *
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    protected MavenProject project;

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
     * The location if the SCDL that defines the test harness composite. The source for this would normally be placed in
     * the test/resources directory and be copied by the resource plugin; this allows property substitution if
     * required.
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
     * The location of the SCDL that configures the Fabric3 runtime. This allows the default runtime configuration
     * supplied in this plugin to be overridden.
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
     * @parameter expression="0.4"
     */
    public String runtimeVersion;

    /**
     * Set of extension artifacts that should be deployed to the runtime.
     *
     * @parameter
     */
    public Dependency[] extensions;

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

    /**
     * Build output directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    protected File outputDirectory;

    @SuppressWarnings({"ThrowFromFinallyBlock", "unchecked"})
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

        URL testScdlURL;
        try {
            testScdlURL = testScdl.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new AssertionError();
        }

        Set<Artifact> runtimeArtifacts = calculateRuntimeArtifacts(runtimeVersion);
        Set<Artifact> hostArtifacts = calculateHostArtifacts(runtimeArtifacts);
        Set<Artifact> dependencies = calculateDependencies(project.getDependencies());
        Set<URL> moduleDependencies = calculateModuleDependencies(dependencies, hostArtifacts);
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

        List<URL> extensionUrls = resolveDependencies(extensions);


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
            coordinator.setExtensions(extensionUrls);
            coordinator.setIntentsLocation(intentsLocation);
            bootRuntime(coordinator, runtime, bootClassLoader, hostClassLoader);
        } catch (InitializationException e) {
            throw new MojoExecutionException("Error initializing Fabric3 Runtime", e);
        } catch (Exception e) {
            throw new MojoExecutionException("Error starting Fabric3 Runtime", e);
        }

        try {
            SurefireTestSuite testSuite;
            log.info("Deploying test SCDL from " + testScdl);
            try {
                if (compositeName == null) {
                    testSuite = createTestSuite(runtime, testScdlURL);
                } else {
                    testSuite = createTestSuite(runtime);
                }
            } catch (Exception e) {
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
                // ignore
            }
        }
    }

    private List<URL> resolveDependencies(Dependency[] dependencies) throws MojoExecutionException {
        if (dependencies == null) {
            return Collections.emptyList();
        }
        List<URL> urls = new ArrayList<URL>(dependencies.length);
        for (Dependency dependency : dependencies) {
        	if(dependency.getVersion() == null){
        		resolveDependencyVersion(dependency);
        	}
            Artifact artifact = createArtifact(dependency);
            try {
                resolver.resolve(artifact, remoteRepositories, localRepository);
            } catch (ArtifactResolutionException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            } catch (ArtifactNotFoundException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
            try {
                urls.add(artifact.getFile().toURI().toURL());
            } catch (MalformedURLException e) {
                // should not happen as toURI should escape the filename
                throw new AssertionError();
            }
        }
        return urls;
    }

    private Artifact createArtifact(Dependency dependency) {
        return artifactFactory.createArtifact(dependency.getGroupId(),
                                              dependency.getArtifactId(),
                                              dependency.getVersion(),
                                              Artifact.SCOPE_RUNTIME,
                                              dependency.getType());
    }

    private void resolveDependencyVersion(Dependency extension){

    	List<org.apache.maven.model.Dependency> dependencies = project.getDependencyManagement().getDependencies();
		for(org.apache.maven.model.Dependency dependecy : dependencies) {
			if(dependecy.getGroupId().equals(extension.getGroupId())
				&& dependecy.getArtifactId().equals(extension.getArtifactId())){
				extension.setVersion(dependecy.getVersion());

			}
		}
    }

    private void shutdownRuntime(RuntimeLifecycleCoordinator<MavenEmbeddedRuntime, Bootstrapper> coordinator)
            throws ShutdownException, InterruptedException, ExecutionException {
        Future<Void> future = coordinator.shutdown();
        future.get();
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

    private URL getSystemConfig() throws MalformedURLException {
        File systemConfig = new File(outputDirectory, "classes" + File.separator + "META-INF" + File.separator + "systemConfig.xml");
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

        return new CompositeClassLoader(URI.create("itestBoot"), urls, parent);
    }

    private Set<Artifact> calculateRuntimeArtifacts(String runtimeVersion) throws MojoExecutionException {
        Set<Artifact> artifacts = new HashSet<Artifact>();
        // add in the runtime
        List<Exclusion> exclusions = Collections.emptyList();
        Dependency dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-maven-host");
        dependency.setVersion(runtimeVersion);
        dependency.setExclusions(exclusions);
        addArtifacts(artifacts, dependency);
        return artifacts;
    }

    private void addArtifacts(Set<Artifact> artifacts, Dependency extension) throws MojoExecutionException {

    	if (extension.getVersion() == null) {
              resolveDependencyVersion(extension);
    	}
        final List<Exclusion> exclusions = extension.getExclusions();

        Artifact artifact = createArtifact(extension);
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

    // this is a hack for now
    private Set<Artifact> resolveArtifacts(Dependency dependency) throws MojoExecutionException {
        Set<Artifact> artifacts = new HashSet<Artifact>();
        addArtifacts(artifacts, dependency);
        return artifacts;
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
        return runtime;
    }

    protected <T> T createHostComponent(Class<T> type, String impl, ClassLoader cl) throws Exception {
        Class<?> implClass = cl.loadClass(impl);
        return type.cast(implClass.newInstance());

    }

    protected SurefireTestSuite createTestSuite(MavenEmbeddedRuntime runtime, URL testScdlURL) throws Exception {
        URI domain = URI.create(testDomain);
        Composite composite = runtime.activate(buildDirectory.toURI().toURL(), testScdlURL);
        runtime.startContext(domain);
        return createTestSuite(runtime, composite, domain);
    }

    protected SurefireTestSuite createTestSuite(MavenEmbeddedRuntime runtime) throws Exception {
        URI domain = URI.create(testDomain);
        QName qName = new QName(compositeNamespace, compositeName);
        Composite composite = runtime.activate(buildDirectory.toURI().toURL(), qName);
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

    /**
     * Transitively calculates the set of artifacts to be included in the host classloader based on the artifacts
     * associated with the Maven module.
     *
     * @param runtimeArtifacts the artifacts associated with the Maven module
     * @return set of artifacts to be included in the host classloader
     * @throws MojoExecutionException if an error occurs calculating the transitive dependencies
     */
    private Set<Artifact> calculateHostArtifacts(Set<Artifact> runtimeArtifacts)
            throws MojoExecutionException {
        Set<Artifact> hostArtifacts = new HashSet<Artifact>();
        List<Exclusion> exclusions = Collections.emptyList();
        // find the version of fabric3-api being used by the runtime
        String version = null;
        for (Artifact artifact : runtimeArtifacts) {
            if ("org.codehaus.fabric3".equals(artifact.getGroupId())
                    && "fabric3-api".equals(artifact.getArtifactId())) {
                version = artifact.getVersion();
                break;
            }
        }
        if (version == null) {
            throw new MojoExecutionException("org.codehaus.fabric3:fabric3-api version not found");
        }
        // add transitive dependencies of fabric3-api to the list of artifacts in the host classloader
        Dependency dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-api");
        dependency.setVersion(version);
        dependency.setExclusions(exclusions);
        addArtifacts(hostArtifacts, dependency);

        // add commons annotations dependency
        Dependency jsr250API = new Dependency();
        jsr250API.setGroupId("org.apache.geronimo.specs");
        jsr250API.setArtifactId("geronimo-annotation_1.0_spec");
        jsr250API.setVersion("1.1");
        jsr250API.setExclusions(exclusions);
        addArtifacts(hostArtifacts, jsr250API);

        // add shared artifacts to the host classpath
        if (shared != null) {
            for (Dependency extension : shared) {
                addArtifacts(hostArtifacts, extension);
            }
        }
        return hostArtifacts;
    }

    private Set<Artifact> calculateDependencies(List<Dependency> dependencies) throws MojoExecutionException {
        Set<Artifact> artifacts = new HashSet<Artifact>();
        for (Dependency dependency : dependencies) {
            addArtifacts(artifacts, dependency);
        }
        return artifacts;
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
                throw new AssertionError();
            }

        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
    }

    /**
     * Calculates module dependencies based on the set of project artifacts. Module dependencies must be visible to
     * implementation code in a composite and encompass project artifacts minus artifacts provided by the host
     * classloader and those that are "provided scope".
     *
     * @param projectArtifacts the artifact set to determine module dependencies from
     * @param hostArtifacts    the set of host artifacts
     * @return the set of URLs pointing to module depedencies.
     */
    private Set<URL> calculateModuleDependencies(Set<Artifact> projectArtifacts, Set<Artifact> hostArtifacts) {
        Set<URL> urls = new LinkedHashSet<URL>();
        for (Artifact artifact : projectArtifacts) {
            try {
                if (hostArtifacts.contains(artifact) || Artifact.SCOPE_PROVIDED.equals(artifact.getScope())) {
                    continue;
                }
                File pathElement = artifact.getFile();
                URL url = pathElement.toURI().toURL();
                getLog().debug("Adding module dependency URL: " + url);
                urls.add(url);

            } catch (MalformedURLException e) {
                // toURI should have encoded the URL
                throw new AssertionError();
            }

        }
        return urls;
    }

    public interface MojoMonitor {
        @LogLevel("SEVERE")
        void runError(Exception e);
    }

}
