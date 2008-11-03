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
package org.fabric3.runtime.standalone.server;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import org.fabric3.api.annotation.logging.Info;
import org.fabric3.api.annotation.logging.Severe;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.jmx.agent.Agent;
import org.fabric3.jmx.agent.DefaultAgent;
import org.fabric3.jmx.agent.rmi.RmiAgent;
import org.fabric3.monitor.MonitorFactory;
import org.fabric3.runtime.standalone.BootstrapException;
import org.fabric3.runtime.standalone.BootstrapHelper;
import org.fabric3.runtime.standalone.StandaloneHostInfo;
import org.fabric3.runtime.standalone.StandaloneRuntime;

/**
 * This class provides the commandline interface for starting the Fabric3 standalone server. The class boots a Fabric3 runtime and launches a daemon
 * that listens for a shutdown command.
 * <p/>
 * The administration port can be specified using the system property <code>fabric3.adminPort</code>.If not specified the default port that is used is
 * <code>1099</code>
 *
 * @version $Rev$ $Date$
 */
public class Fabric3Server implements Fabric3ServerMBean {
    private static final String JMX_DOMAIN = "fabric3.jmx";
    private static final String MONITOR_PORT_PARAM = "fabric3.monitor.port";
    private static final String MONITOR_KEY_PARAM = "fabric3.monitor.key";
    private static final String JOIN_DOMAIN_TIMEOUT = "fabric3.join.domain.timeout";
    private static final String INTENTS_FILE = "intents.xml";

    private final RmiAgent agent;
    private final File installDirectory;
    private RuntimeLifecycleCoordinator<StandaloneRuntime, Bootstrapper> coordinator;
    private ServerMonitor monitor;

    /**
     * Main method.
     *
     * @param args Commandline arguments.
     * @throws Exception if there is a problem starting the runtime
     */
    public static void main(String[] args) throws Exception {
        Fabric3Server server = new Fabric3Server();
        String jmxDomain = System.getProperty(JMX_DOMAIN, "standalone");
        server.startRuntime(jmxDomain);
        server.shutdownRuntime(jmxDomain);
        System.exit(0);
    }

    /**
     * Constructor.
     *
     * @throws MalformedURLException if the install directory is invalid
     */
    private Fabric3Server() throws MalformedURLException {
        installDirectory = BootstrapHelper.getInstallDirectory(Fabric3Server.class);

        // TODO Add better host JMX support from the next release
        agent = new RmiAgent();
    }

    /**
     * Starts a runtime specified by the bootpath.
     *
     * @param jmxDomain the domain name for the runtime.
     */
    public final void startRuntime(final String jmxDomain) {
        final StandaloneHostInfo hostInfo;
        final StandaloneRuntime runtime;
        try {

            File configDir = BootstrapHelper.getDirectory(installDirectory, null, "config");

            // load properties for this runtime
            File propFile = new File(configDir, "runtime.properties");
            Properties props = BootstrapHelper.loadProperties(propFile, System.getProperties());

            // load the monitor ports and keys
            String monitorKey = props.getProperty(MONITOR_KEY_PARAM, "f3");
            String portVal = props.getProperty(MONITOR_PORT_PARAM, "8083");
            int monitorPort;
            try {
                monitorPort = Integer.parseInt(portVal);
                if (monitorPort < 0) {
                    throw new IllegalArgumentException("Invalid monitor port number:" + monitorPort);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid monitor port", e);
            }

            // load the join timeout
            int joinTimeout;
            try {
                joinTimeout = Integer.parseInt(props.getProperty(JOIN_DOMAIN_TIMEOUT, "10000"));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid join domain timeout value", e);
            }

            // create the classloaders for booting the runtime
            String bootPath = props.getProperty("fabric3.bootDir", null);
            File bootDir = BootstrapHelper.getDirectory(installDirectory, bootPath, "boot");

            String hostPath = props.getProperty("fabric3.hostDir", null);
            File hostDir = BootstrapHelper.getDirectory(installDirectory, hostPath, "host");

            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            ClassLoader hostLoader = BootstrapHelper.createClassLoader(systemClassLoader, hostDir);
            ClassLoader bootLoader = BootstrapHelper.createClassLoader(hostLoader, bootDir);

            // create the HostInfo, MonitorFactory, and runtime
            hostInfo = BootstrapHelper.createHostInfo(installDirectory, configDir, props);
            MonitorFactory monitorFactory = BootstrapHelper.createMonitorFactory(bootLoader, props);
            runtime = BootstrapHelper.createRuntime(hostInfo, bootLoader, monitorFactory);
            runtime.setMBeanServer(agent.getMBeanServer());
            runtime.setJmxSubDomain(jmxDomain);
            monitor = runtime.getMonitorFactory().getMonitor(ServerMonitor.class);

            // boot the runtime
            coordinator = BootstrapHelper.createCoordinator(hostInfo, bootLoader);
            BootConfiguration<StandaloneRuntime, Bootstrapper> configuration = createBootConfiguration(runtime, bootLoader, hostLoader);
            coordinator.setConfiguration(configuration);
            coordinator.bootPrimordial();
            // load and initialize runtime extension components and the local runtime domain
            coordinator.initialize();
            // join a distributed domain
            Future<Void> future = coordinator.joinDomain(joinTimeout);
            future.get();
            // perform recovery. If the runtime is a controller node, this may result in reprovisioning components
            future = coordinator.recover();
            future.get();
            // start the runtime receiving requests
            future = coordinator.start();
            future.get();

            monitor.started(jmxDomain);
            agent.start();
            // create the shutdown daemon
            CountDownLatch latch = new CountDownLatch(1);
            new ShutdownDaemon(monitorPort, monitorKey, latch);
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            agent.shutdown();
        } catch (Exception ex) {
            if (monitor != null) {
                // there could have been an error initializing the monitor
                monitor.runError(ex);
            }
            throw new Fabric3ServerException(ex);
        }
    }

    public final void shutdownRuntime(String bootPath) {

        try {
            if (coordinator != null) {
                coordinator.shutdown();
            }
            monitor.stopped(bootPath);
        } catch (ShutdownException ex) {
            monitor.runError(ex);
            throw new Fabric3ServerException(ex);
        }

    }

    /**
     * Shuts the server down.
     */
    public final void shutdown() {
    }


    private BootConfiguration<StandaloneRuntime, Bootstrapper> createBootConfiguration(StandaloneRuntime runtime,
                                                                                       ClassLoader bootClassLoader,
                                                                                       ClassLoader appClassLoader)
            throws BootstrapException, InitializationException {
        StandaloneHostInfo hostInfo = runtime.getHostInfo();
        BootConfiguration<StandaloneRuntime, Bootstrapper> configuration = new BootConfiguration<StandaloneRuntime, Bootstrapper>();
        configuration.setAppClassLoader(appClassLoader);
        configuration.setBootClassLoader(bootClassLoader);

        Bootstrapper bootstrapper = BootstrapHelper.createBootstrapper(hostInfo, bootClassLoader);
        // create the runtime bootrapper
        configuration.setBootstrapper(bootstrapper);

        // add the boot libraries to export as contributions. This is necessary so extension contributions can import them
        List<String> bootExports = new ArrayList<String>();
        bootExports.add("META-INF/maven/org.codehaus.fabric3/fabric3-spi/pom.xml");
        bootExports.add("META-INF/maven/org.codehaus.fabric3/fabric3-pojo/pom.xml");
        configuration.setBootLibraryExports(bootExports);

        // process extensions
        File extensionsDir = runtime.getHostInfo().getExtensionsDirectory();
        File userExtensionsDir = runtime.getHostInfo().getUserExtensionsDirectory();
        List<ContributionSource> extensions = getExtensionContributions(extensionsDir);
        configuration.setExtensions(extensions);
        List<ContributionSource> userExtensions = getExtensionContributions(userExtensionsDir);
        configuration.setUserExtensions(userExtensions);

        // process the baseline intents
        ContributionSource source = getIntentsContribution(hostInfo.getConfigDirectory());
        configuration.setIntents(source);
        configuration.setRuntime(runtime);
        return configuration;
    }

    private ContributionSource getIntentsContribution(File dir) throws InitializationException {
        //File dir = runtime.getHostInfo().getConfigDirectory();
        try {
            File file = new File(dir, INTENTS_FILE);
            if (!file.exists()) {
                return null;
            }
            URI contribuUri = URI.create("StandardIntents");
            URL location = file.toURI().toURL();
            return new FileContributionSource(contribuUri, location, -1, new byte[0]);
        } catch (MalformedURLException e) {
            throw new InitializationException(e);
        }
    }

    private List<ContributionSource> getExtensionContributions(File dir) throws InitializationException {
        List<ContributionSource> sources = new ArrayList<ContributionSource>();
        File[] files = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                String name = pathname.getName();
                return name.endsWith(".jar") || name.endsWith(".zip") || name.endsWith(".xml");
            }
        });
        for (File file : files) {
            try {
                URI uri = URI.create(file.getName());
                URL location = file.toURI().toURL();
                ContributionSource source = new FileContributionSource(uri, location, -1, new byte[0]);
                sources.add(source);
            } catch (MalformedURLException e) {
                throw new InitializationException("Error loading extension", file.getName(), e);
            }

        }
        return sources;
    }


    public interface ServerMonitor {
        @Severe
        void runError(Exception e);

        @Info
        void started(String profile);

        @Info
        void stopped(String profile);

    }


}
