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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.runtime.maven.itest;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import org.fabric3.api.annotation.logging.Severe;
import org.fabric3.host.runtime.MaskingClassLoader;
import org.fabric3.runtime.maven.MavenRuntime;
import org.fabric3.util.io.FileHelper;

/**
 * Run integration tests on a SCA composite using an embedded Fabric3 runtime.
 *
 * @version $Rev$ $Date$
 * @goal test
 * @phase integration-test
 * @execute phase="integration-test"
 */
public class Fabric3ITestMojo extends AbstractMojo {
    private static final String CLEAN = "fabric3.extensions.dependencies.cleanup";

    static {
        // This static block is used to optionally clean the temporary directory between test runs. A static block is used as the iTest plugin may
        // be instantiated multiple times during a run.
        boolean clearTmp = Boolean.valueOf(System.getProperty(CLEAN, "false"));
        if (clearTmp) {
            clearTempFiles();
        }
    }

    /**
     * POM
     *
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    protected MavenProject project;

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
     * The version of the runtime to use.
     *
     * @parameter expression="RELEASE"
     */
    public String runtimeVersion;

    /**
     * Set of runtime extension artifacts that should be deployed to the runtime.
     *
     * @parameter
     */
    public Dependency[] extensions = new Dependency[0];

    /**
     * Set of profiles for the runtime.
     *
     * @parameter
     */
    public Dependency[] profiles = new Dependency[0];

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
     * @parameter expression="${component.org.fabric3.runtime.maven.itest.ArtifactHelper}"
     * @required
     * @readonly
     */
    public ArtifactHelper artifactHelper;

    /**
     * @parameter expression="${component.org.fabric3.runtime.maven.itest.ExtensionHelper}"
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

    /**
     * JDK and system classpath packages to hide from the runtime classpath.
     *
     * @parameter
     */
    public String[] hiddenPackages = new String[]{"javax.xml.bind.", "javax.xml.ws.", "javax.xml.soap."};

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

        MavenRuntime runtime = booter.boot();
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
     * Recursively cleans the F3 temporary directory.
     */
    private static void clearTempFiles() {
        File f3TempDir = new File(System.getProperty("java.io.tmpdir"), ".f3");
        try {
            FileHelper.deleteDirectory(f3TempDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the configuration to boot the Maven runtime, including resolving dependencies.
     *
     * @return the boot configuration
     * @throws MojoExecutionException if there is an error creating the configuration
     */
    private MavenBootConfiguration createBootConfiguration() throws MojoExecutionException {

        Set<Artifact> runtimeArtifacts = artifactHelper.calculateRuntimeArtifacts(runtimeVersion);
        Set<Artifact> hostArtifacts = artifactHelper.calculateHostArtifacts(runtimeArtifacts, shared);
        Set<Artifact> dependencies = artifactHelper.calculateDependencies();
        Set<URL> moduleDependencies = artifactHelper.calculateModuleDependencies(dependencies, hostArtifacts);

        Set<Dependency> expandedExtensions = new HashSet<Dependency>();
        expandedExtensions.addAll(getCoreExtensions());
        expandedExtensions.addAll(Arrays.asList(extensions));
        expandedExtensions.addAll(artifactHelper.expandProfileExtensions(profiles));

        ClassLoader parentClassLoader = getClass().getClassLoader();
        if (hiddenPackages.length > 0) {
            // mask hidden JDK and system classpath packages
            parentClassLoader = new MaskingClassLoader(parentClassLoader, hiddenPackages);
        }


        ClassLoader hostClassLoader = createHostClassLoader(parentClassLoader, hostArtifacts);
        ClassLoader bootClassLoader = createBootClassLoader(hostClassLoader, runtimeArtifacts);

        MavenBootConfiguration configuration = new MavenBootConfiguration();
        configuration.setBootClassLoader(bootClassLoader);
        configuration.setHostClassLoader(hostClassLoader);
        configuration.setLog(getLog());
        configuration.setExtensionHelper(extensionHelper);

        configuration.setExtensions(expandedExtensions);

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
     * Returns the core runtime extensions as a set of dependencies
     *
     * @return the extensions
     */
    private Set<Dependency> getCoreExtensions() {
        Set<Dependency> extensions = new HashSet<Dependency>();

        Dependency dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-jdk-proxy");
        dependency.setVersion(runtimeVersion);
        dependency.setType("jar");
        extensions.add(dependency);

        dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-java");
        dependency.setVersion(runtimeVersion);
        dependency.setType("jar");
        extensions.add(dependency);

        dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-async");
        dependency.setVersion(runtimeVersion);
        dependency.setType("jar");
        extensions.add(dependency);

        dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-conversation-propagation");
        dependency.setVersion(runtimeVersion);
        dependency.setType("jar");
        extensions.add(dependency);

        dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-sca-intents");
        dependency.setVersion(runtimeVersion);
        dependency.setType("jar");
        extensions.add(dependency);

        dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-resource");
        dependency.setVersion(runtimeVersion);
        dependency.setType("jar");
        extensions.add(dependency);

        dependency = new Dependency();
        dependency.setGroupId("javax.transaction");
        dependency.setArtifactId("com.springsource.javax.transaction");
        dependency.setVersion("1.1.0");
        dependency.setType("jar");
        extensions.add(dependency);

        dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-maven-extension");
        dependency.setVersion(runtimeVersion);
        dependency.setType("jar");
        extensions.add(dependency);

        dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-junit");
        dependency.setVersion(runtimeVersion);
        dependency.setType("jar");
        extensions.add(dependency);

        return extensions;
    }

    public interface MojoMonitor {
        @Severe
        void runError(Exception e);
    }

}
