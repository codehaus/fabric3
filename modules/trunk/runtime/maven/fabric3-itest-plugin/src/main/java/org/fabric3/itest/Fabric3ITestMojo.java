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
 * --- Original Apache License ---
 *
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

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
import org.fabric3.featureset.FeatureSet;
import org.fabric3.maven.MavenEmbeddedRuntime;
import org.fabric3.util.closure.Closure;
import org.fabric3.util.closure.CollectionUtils;
import org.xml.sax.SAXException;

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
     * Optional parameter for management domain.
     *
     * @parameter
     */
    public String managementDomain = "itest-host";

    /**
     * Optional parameter for thread pool size.
     *
     * @parameter
     */
    public int numWorkers = 10;

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
     * The location of the SCDL that configures the Fabric3 runtime. This allows the default runtime configuration supplied in this plugin to be
     * overridden.
     *
     * @parameter
     */
    public URL systemScdl;

    /**
     * The location of the default intents file for the Fabric3 runtime.
     *
     * @parameter
     */
    public URL intentsLocation;

    /**
     * The version of the runtime to use.
     *
     * @parameter expression="0.7"
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
     * Whether to exclude default features.
     *
     * @parameter
     */
    public boolean excludeDefaultFeatures;

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
    public ArtifactFactory _artifactFactory;

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
     * Allows the optional in-line specification of system configuration in the plugin configuration.
     *
     * @parameter
     */
    public String systemConfig;

    /**
     * Build output directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    protected File outputDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {

        if (!testScdl.exists()) {
            getLog().info("No itest composite found, skipping integration tests");
            return;
        }
        if (skip) {
            getLog().info("Skipping integration tests by user request.");
            return;
        }

        artifactHelper.setLocalRepository(localRepository);
        artifactHelper.setProject(project);

        MavenBootConfiguration configuration = createBootConfiguration();

        Thread.currentThread().setContextClassLoader(configuration.getBootClassLoader());

        MavenRuntimeBooter booter = new MavenRuntimeBooter(configuration);

        MavenEmbeddedRuntime runtime = booter.boot();
        try {
            TestDeployer deployer;
            if (compositeName == null) {
                deployer = new TestDeployer(testScdl, buildDirectory, getLog());
            } else {
                deployer = new TestDeployer(compositeNamespace, compositeName, buildDirectory, getLog());
            }
            deployer.deploy(runtime);
            TestRunner runner = new TestRunner(reportsDirectory, trimStackTrace, getLog());
            runner.executeTests(runtime);
        } finally {
            try {
                booter.shutdown();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * Creates the configuration to boot the Maven runtime, including resolving dependencies.
     *
     * @return the boot configuration
     * @throws MojoExecutionException if there is an error creating the configuration
     */
    private MavenBootConfiguration createBootConfiguration() throws MojoExecutionException {
        List<FeatureSet> featureSets = resolveFeatureSets();
        Set<Artifact> runtimeArtifacts = artifactHelper.calculateRuntimeArtifacts(runtimeVersion);
        Set<Artifact> hostArtifacts = artifactHelper.calculateHostArtifacts(runtimeArtifacts, shared, featureSets);
        Set<Artifact> dependencies = artifactHelper.calculateDependencies();
        Set<URL> moduleDependencies = artifactHelper.calculateModuleDependencies(dependencies, hostArtifacts);

        ClassLoader parentClassLoader = getClass().getClassLoader();
        ClassLoader hostClassLoader = createHostClassLoader(parentClassLoader, hostArtifacts);
        ClassLoader bootClassLoader = createBootClassLoader(hostClassLoader, runtimeArtifacts);

        MavenBootConfiguration configuration = new MavenBootConfiguration();
        configuration.setBootClassLoader(bootClassLoader);
        configuration.setHostClassLoader(hostClassLoader);
        configuration.setManagementDomain(managementDomain);
        configuration.setLog(getLog());
        configuration.setExtensionHelper(extensionHelper);

        configuration.setFeatureSets(featureSets);
        configuration.setExtensions(extensions);
        configuration.setExtensionArtifacts(getArtifacts("f3-extension"));
        
        configuration.setUserExtensions(userExtensions);
        configuration.setUserExtensionsArchives(userExtensionsArchives);
        configuration.setIntentsLocation(intentsLocation);
        configuration.setModuleDependencies(moduleDependencies);
        configuration.setOutputDirectory(outputDirectory);
        configuration.setProperties(properties);
        configuration.setSystemConfig(systemConfig);
        configuration.setSystemConfigDir(systemConfigDir);
        configuration.setSystemScdl(systemScdl);
        return configuration;
    }

    /**
     * Creates the classloader to boot the runtime.
     *
     * @param parent    the parent classloader
     * @param artifacts the set of artifacts to include on the boot classpath
     * @return the boot classloader
     */
    private ClassLoader createBootClassLoader(ClassLoader parent, Set<Artifact> artifacts) {

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
            log.debug("Fabric3 boot classpath:");
            for (URL url : urls) {
                log.debug("  " + url);
            }
        }
        return new URLClassLoader(urls, parent);
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

    /**
     * Resolves configured feature sets to load as runtime extensions.
     *
     * @return the resolved set of FeatureSets
     * @throws MojoExecutionException if a resolution error occurs
     */
    private List<FeatureSet> resolveFeatureSets() throws MojoExecutionException {
        List<Dependency> featurestoInstall = getFeaturesToInstall();
        // Resolved feature sets
        List<FeatureSet> featureSets = new LinkedList<FeatureSet>();

        if (!featurestoInstall.isEmpty()) {
            for (Dependency feature : featurestoInstall) {
                Artifact artifact = artifactHelper.resolve(feature);
                try {
                    FeatureSet featureSet = FeatureSet.deserialize(artifact.getFile());
                    featureSets.add(featureSet);
                } catch (ParserConfigurationException e) {
                    throw new MojoExecutionException("Error booting Fabric3 runtime", e);
                } catch (SAXException e) {
                    throw new MojoExecutionException("Error booting Fabric3 runtime", e);
                } catch (IOException e) {
                    throw new MojoExecutionException("Error booting Fabric3 runtime", e);
                }
            }
        }
        return featureSets;
    }

    private List<Dependency> getFeaturesToInstall() {
        List<Dependency> featuresToInstall = new ArrayList<Dependency>();

        if (features != null) {
            featuresToInstall.addAll(Arrays.asList(features));
        }
        if (!excludeDefaultFeatures) {
            Dependency dependency = new Dependency();
            dependency.setArtifactId("fabric3-default-feature");
            dependency.setGroupId("org.codehaus.fabric3");
            dependency.setVersion(runtimeVersion);
            dependency.setType("xml");
            featuresToInstall.add(dependency);
        }
        return featuresToInstall;
    }

    @SuppressWarnings("unchecked")
    private Set<Artifact> getArtifacts(final String scope) throws MojoExecutionException {
        
        Set<Artifact> artifacts = (Set<Artifact>) project.getArtifacts();
        return CollectionUtils.filter(artifacts, new Closure<Artifact, Boolean>() {
            public Boolean execute(Artifact artifact) {
                return scope.equals(artifact.getScope());
            }
        });
        
    }

    public interface MojoMonitor {
        @Severe
        void runError(Exception e);
    }

}
