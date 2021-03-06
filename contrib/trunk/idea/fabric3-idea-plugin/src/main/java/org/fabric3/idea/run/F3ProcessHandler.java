/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.idea.run;

import java.io.File;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import javax.xml.namespace.QName;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import static com.intellij.execution.ui.ConsoleViewContentType.NORMAL_OUTPUT;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import org.apache.maven.surefire.report.Reporter;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.report.ReporterManager;
import org.apache.maven.surefire.suite.SurefireTestSuite;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.idea.IntelliJConstants;
import org.fabric3.idea.contribution.IntelliJModuleContributionSource;
import org.fabric3.junit.ImplementationJUnit;
import org.fabric3.maven.runtime.MavenCoordinator;
import org.fabric3.maven.runtime.MavenEmbeddedRuntime;
import org.fabric3.monitor.JavaLoggingMonitorFactory;
import org.fabric3.pojo.scdl.JavaMappedService;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Operation;
import org.fabric3.spi.deployer.CompositeClassLoader;

/**
 * Executes an embedded Fabric3 Maven runtime and a specified set of JUnit components.
 * <p/>
 * TODO Separate host and bootstrap classes as they are now placed on the host classloader.
 *
 * @version $Rev$ $Date$
 */
public class F3ProcessHandler extends ProcessHandler {
    private URI domainUri;
    private Module module;
    private String junitClass;
    private QName composite;
    private boolean terminated;
    private ConsoleView view;
    private MavenEmbeddedRuntime runtime;
    private MavenCoordinator coordinator;

    public F3ProcessHandler(URI domainUri, Module module, String junitClass, QName composite, ConsoleView view) {
        this.domainUri = domainUri;
        this.module = module;
        this.junitClass = junitClass;
        this.composite = composite;
        this.view = view;
    }

    public boolean isProcessTerminated() {
        return terminated;
    }

    public void startNotify() {
        try {
            view.print("Executing tests...", NORMAL_OUTPUT);
            ModuleRootManager manager = ModuleRootManager.getInstance(module);
            // DO NOT USE ModuleRootManager#getCompilerOutputPathUrl() or
            // ModuleRootManager#getCompilerOutputPathForTestsUrl() as they return an invalid URL (file:/<path> as
            // opposed to file://<path>)
            URL outputDir = new File(manager.getCompilerOutputPath().getPath()).toURI().toURL();
            URL testDir = new File(manager.getCompilerOutputPathForTests().getPath()).toURI().toURL();

            ClassLoader parent = getClass().getClassLoader();
            ClassLoader hostClassLoader = createHostClassLoader(parent, manager, outputDir, testDir);
            ClassLoader bootClassLoader = createBootClassLoader(hostClassLoader);


            MonitorFactory monitorFactory = createMonitorFactory();
            IntelliJHostInfo hostInfo = createHostInfo(outputDir, testDir);
            runtime = createRuntime(bootClassLoader, hostClassLoader, hostInfo, monitorFactory);
            String clazz = "org.fabric3.maven.runtime.impl.MavenCoordinatorImpl"; // TODO make configurable
            coordinator = createHostComponent(MavenCoordinator.class, clazz, bootClassLoader);
            List<URL> extensions = Collections.emptyList();
            coordinator.setExtensions(extensions);
            // URL intentsLocation = bootClassLoader.getResource("META-INF/fabric3/intents.xml");
            // coordinator.setIntentsLocation(intentsLocation);
            bootRuntime(bootClassLoader, hostClassLoader);
            SurefireTestSuite suite = contributeModule();
            run(suite, new Properties());
            view.print("Finished test run", NORMAL_OUTPUT);
            terminated = true;
        } catch (InitializationException e) {
            Logger.getInstance("fabric3").error(e);
            e.printStackTrace();
            terminated = true;
        } catch (Exception e) {
            Logger.getInstance("fabric3").error(e);
            e.printStackTrace();
            terminated = true;
        }
    }

    private MonitorFactory createMonitorFactory() {
        return new JavaLoggingMonitorFactory(null, Level.FINE, "f3");
    }

    private IntelliJHostInfo createHostInfo(URL outputDir, URL testDir) {
        Properties props = System.getProperties();
        Set<URL> dependencies = new LinkedHashSet<URL>();
        List<String> implementations = new ArrayList<String>();
        // TODO handle class versus package
        implementations.add(junitClass);
        // TODO make configurable
        List<QName> composites = new ArrayList<QName>();
        composites.add(composite);
        return new IntelliJHostInfoImpl(domainUri, props, dependencies, outputDir, testDir, implementations,
            composites);
    }

    /**
     * Creates the host classloader based on the given set of artifacts.
     *
     * @param parent    the parent classloader
     * @param manager   the root manager
     * @param outputDir the output directory
     * @param testDir   the test output directory
     * @return the host classloader
     * @throws ExecutionException if an error occurs creating the classloader
     */
    private ClassLoader createHostClassLoader(ClassLoader parent, ModuleRootManager manager, URL outputDir, URL testDir)
        throws ExecutionException {
        List<URL> classpath = new ArrayList<URL>();
        VirtualFile[] files = manager.getFiles(OrderRootType.COMPILATION_CLASSES);
        for (VirtualFile file : files) {
            try {
                String path = file.getPath();
                int index = path.indexOf("!");
                if (index > 0) {
                    // remove trailing '!/' from URLs
                    path = path.substring(0, index);
                }
                URL url = new URL("file", "", path);
                // FIXME add check for depedendent module directories as well
                if (!outputDir.equals(url) && !testDir.equals(url)) {
                    classpath.add(url);
                }
            } catch (MalformedURLException e) {
                throw new ExecutionException("Invalid classpath entry", e);
            }
        }
        return new URLClassLoader(classpath.toArray(new URL[classpath.size()]), parent);
    }

    /**
     * Creates the boot classloader from the contents of the plugin's /fabric3 directory.
     *
     * @param parent the parent classloader
     * @return the host classloader
     * @throws ExecutionException if an error occurs creating the host classloader
     */
    private ClassLoader createBootClassLoader(ClassLoader parent) throws ExecutionException {
        // calculate the location of /fabric3 relative to the /lib directory
        String path = PathUtil.getJarPathForClass(MavenEmbeddedRuntime.class);
        int index = path.lastIndexOf("/lib");
        path = path.substring(0, index);
        File bootDir = new File(path);
        List<URL> urls = new ArrayList<URL>();
        if (bootDir.exists()) {
            for (File entry : bootDir.listFiles()) {
                try {
                    urls.add(entry.toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new ExecutionException("Invalid URL", e);
                }
            }
        }
        return new CompositeClassLoader(URI.create("itestBootClassLoader"), urls.toArray(new URL[urls.size()]), parent);
    }

    /**
     * Instantiates a component implementation in the given classloader
     *
     * @param type the service interface
     * @param impl the implementation class
     * @param cl   the classloader
     * @return the instance
     * @throws Exception if an instantiation exception occurs
     */
    private <T> T createHostComponent(Class<T> type, String impl, ClassLoader cl) throws Exception {
        Class<?> implClass = cl.loadClass(impl);
        return type.cast(implClass.newInstance());
    }

    private MavenEmbeddedRuntime createRuntime(ClassLoader bootClassLoader,
                                               ClassLoader hostClassLoader,
                                               IntelliJHostInfo hostInfo,
                                               MonitorFactory monitorFactory) throws Exception {
        // TODO configure
        String runtimeImpl = "org.fabric3.maven.runtime.impl.MavenEmbeddedRuntimeImpl";
        MavenEmbeddedRuntime runtime = createHostComponent(MavenEmbeddedRuntime.class, runtimeImpl, bootClassLoader);
        runtime.setMonitorFactory(monitorFactory);
        runtime.setHostInfo(hostInfo);
        runtime.setHostClassLoader(hostClassLoader);
        return runtime;
    }

    private void bootRuntime(ClassLoader bootClassLoader, ClassLoader testClassLoader) throws Exception {
        String bootstrapperImpl = "org.fabric3.fabric.runtime.ScdlBootstrapperImpl"; // TOOD make configurable
        ScdlBootstrapper bootstrapper = createHostComponent(ScdlBootstrapper.class, bootstrapperImpl, bootClassLoader);
        URL systemScdl = bootClassLoader.getResource("intellij.composite");
        bootstrapper.setScdlLocation(systemScdl);
        coordinator.bootPrimordial(runtime, bootstrapper, bootClassLoader, testClassLoader);
        coordinator.initialize();
        Future<Void> future = coordinator.joinDomain(-1);
        future.get();
        future = coordinator.recover();
        future.get();
        future = coordinator.start();
        future.get();
    }

    private SurefireTestSuite contributeModule() throws Exception {
        ContributionSource source = new IntelliJModuleContributionSource(URI.create(module.getName()));
        Composite composite = runtime.activate(source, IntelliJConstants.TEST_COMPOSITE);
        runtime.startContext(domainUri);
        return createTestSuite(runtime, composite);
    }

    private boolean run(SurefireTestSuite suite, Properties status) throws ReporterException, TestSetFailedException {
        int totalTests = suite.getNumTests();

        boolean trimStackTrace = false; // TODO configure
        List<Reporter> reports = new ArrayList<Reporter>();
        ConsoleReporter reporter = new ConsoleReporter(view, trimStackTrace);
        reports.add(reporter);
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


    private SurefireTestSuite createTestSuite(MavenEmbeddedRuntime runtime,
                                              Composite composite) throws ExecutionException {
        SCATestSuite suite = new SCATestSuite();

        Map<String, ComponentDefinition<? extends Implementation<?>>> components = composite.getComponents();
        for (Map.Entry<String, ComponentDefinition<? extends Implementation<?>>> entry : components.entrySet()) {
            String name = entry.getKey();
            ComponentDefinition<? extends Implementation<?>> junitDefinition = entry.getValue();
            Implementation<?> implementation = junitDefinition.getImplementation();
            if (ImplementationJUnit.class.isAssignableFrom(implementation.getClass())) {
                SCATestSet testSet = createTestSet(runtime, name, domainUri, junitDefinition);
                suite.add(testSet);
            }
        }
        return suite;
    }

    protected SCATestSet createTestSet(MavenEmbeddedRuntime runtime,
                                       String name,
                                       URI contextId,
                                       ComponentDefinition definition) throws ExecutionException {
        ImplementationJUnit impl = (ImplementationJUnit) definition.getImplementation();
        PojoComponentType componentType = impl.getComponentType();
        Map services = componentType.getServices();
        JavaMappedService testService = (JavaMappedService) services.get("testService");
        if (testService == null) {
            throw new ExecutionException("No testService defined on component: " + definition.getName());
        }
        List<? extends Operation<?>> operations = testService.getServiceContract().getOperations();
        return new SCATestSet(runtime, name, contextId, operations);
    }


    protected void destroyProcessImpl() {
        // TODO shutdown
    }

    protected void detachProcessImpl() {

    }

    public boolean detachIsDefault() {
        return true;
    }

    public OutputStream getProcessInput() {
        return null;
    }
}
