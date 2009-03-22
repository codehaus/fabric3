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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.fabric3.api.annotation.logging.Info;
import org.fabric3.api.annotation.logging.Severe;
import org.fabric3.host.Names;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.jmx.agent.rmi.RmiAgent;
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
    private static final String JMX_PORT = "fabric3.jmx.port";
    private static final String MONITOR_PORT_PARAM = "fabric3.monitor.port";
    private static final String MONITOR_KEY_PARAM = "fabric3.monitor.key";
    private static final String JOIN_DOMAIN_TIMEOUT = "fabric3.join.domain.timeout";
    private static final String INTENTS_FILE = "intents.xml";

    private final File installDirectory;
    private RuntimeLifecycleCoordinator coordinator;
    private RmiAgent agent;
    private ServerMonitor monitor;

    /**
     * Main method.
     *
     * @param args Commandline arguments.
     * @throws Exception if there is a problem starting the runtime
     */
    public static void main(String[] args) throws Exception {
        Fabric3Server server = new Fabric3Server();

        RuntimeMode runtimeMode = getRuntimeMode(args);
        String jmxDomain = System.getProperty(JMX_DOMAIN, "standalone");
        server.startRuntime(runtimeMode, jmxDomain);
        server.shutdownRuntime(jmxDomain);

        System.exit(0);
    }

    private static RuntimeMode getRuntimeMode(String[] args) {
        RuntimeMode runtimeMode = RuntimeMode.VM;
        if (args.length > 0) {
            if ("controller".equals(args[0])) {
                runtimeMode = RuntimeMode.CONTROLLER;
            } else if ("participant".equals(args[0])) {
                runtimeMode = RuntimeMode.PARTICIPANT;
            } else if (!"vm".equals(args[0])) {
                throw new IllegalArgumentException("Invalid runtime mode: " + args[0]
                        + ". Valid modes are 'controller', 'participant' or 'vm' (default).");
            }
        }
        return runtimeMode;
    }

    /**
     * Constructor.
     *
     * @throws MalformedURLException if the install directory is invalid
     */
    private Fabric3Server() throws MalformedURLException {
        installDirectory = BootstrapHelper.getInstallDirectory(Fabric3Server.class);
    }

    public final void startRuntime(RuntimeMode runtimeMode, String jmxDomain) {
        StandaloneHostInfo hostInfo;
        StandaloneRuntime runtime;
        try {
            //  calculate config directories based on the mode the runtime is booted in
            File configDir = BootstrapHelper.getDirectory(installDirectory, "config");
            File modeConfigDir = BootstrapHelper.getDirectory(configDir, runtimeMode.toString().toLowerCase());

            // load properties for this runtime
            File propFile = new File(modeConfigDir, "runtime.properties");
            Properties props = BootstrapHelper.loadProperties(propFile, System.getProperties());

            // load the monitor ports and keys
            String monitorKey = props.getProperty(MONITOR_KEY_PARAM, "f3");
            String portVal = props.getProperty(MONITOR_PORT_PARAM, "8083");

            int minMonitorPort;
            int maxMonitorPort = -1;
            String[] monitorTokens = portVal.split("-");
            if (monitorTokens.length == 1) {
                minMonitorPort = parsePortNumber(portVal, "monitor");
            } else if (monitorTokens.length == 2) {
                // port range specified
                minMonitorPort = parsePortNumber(monitorTokens[0], "monitor");
                maxMonitorPort = parsePortNumber(monitorTokens[1], "monitor");
            } else {
                throw new IllegalArgumentException("Invalid monitor port range in runtime.properties");
            }


            // load the join timeout
            int joinTimeout;
            try {
                joinTimeout = Integer.parseInt(props.getProperty(JOIN_DOMAIN_TIMEOUT, "10000"));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid join domain timeout value", e);
            }

            // create the classloaders for booting the runtime
            File bootDir = BootstrapHelper.getDirectory(installDirectory, "boot");

            File hostDir = BootstrapHelper.getDirectory(installDirectory, "host");

            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            ClassLoader hostLoader = BootstrapHelper.createClassLoader(systemClassLoader, hostDir);
            ClassLoader bootLoader = BootstrapHelper.createClassLoader(hostLoader, bootDir);

            // create the HostInfo, MonitorFactory, and runtime
            hostInfo = BootstrapHelper.createHostInfo(runtimeMode, installDirectory, configDir, modeConfigDir, props);
            String monitorFactoryName = props.getProperty("fabric3.monitorFactoryClass");
            MonitorFactory monitorFactory;
            if (monitorFactoryName != null) {
                monitorFactory = BootstrapHelper.createMonitorFactory(bootLoader, monitorFactoryName);
            } else {
                monitorFactory = BootstrapHelper.createDefaultMonitorFactory(bootLoader);
            }
            File logConfigFile = new File(configDir, "monitor.properties");
            if (logConfigFile.exists()) {
                monitorFactory.readConfiguration(logConfigFile.toURI().toURL());
            }

            runtime = BootstrapHelper.createRuntime(hostInfo, hostLoader, bootLoader, monitorFactory);
            monitor = runtime.getMonitorFactory().getMonitor(ServerMonitor.class);

            // boot the JMX agent
            String jmxString = props.getProperty(JMX_PORT, "1099");
            String[] tokens = jmxString.split("-");
            if (tokens.length == 1) {
                // port specified
                int jmxPort = parsePortNumber(jmxString, "JMX");
                agent = new RmiAgent(jmxPort);
            } else if (tokens.length == 2) {
                // port range specified
                int minPort = parsePortNumber(tokens[0], "JMX");
                int maxPort = parsePortNumber(tokens[1], "JMX");
                agent = new RmiAgent(minPort, maxPort);
            } else {
                throw new IllegalArgumentException("Invalid JMX port specified in runtime.properties");
            }
            runtime.setMBeanServer(agent.getMBeanServer());
            runtime.setJmxSubDomain(jmxDomain);

            // boot the runtime
            coordinator = BootstrapHelper.createCoordinator(bootLoader);
            BootConfiguration configuration = createBootConfiguration(runtime, bootLoader);
            coordinator.setConfiguration(configuration);
            coordinator.bootPrimordial();
            // load and initialize runtime extension components and the local runtime domain
            coordinator.initialize();
            coordinator.recover();
            coordinator.joinDomain(joinTimeout);
            coordinator.start();

            agent.start();
            // create the shutdown daemon
            CountDownLatch latch = new CountDownLatch(1);
            ShutdownDaemon daemon = new ShutdownDaemon(minMonitorPort, maxMonitorPort, monitorKey, latch);
            monitor.started(runtimeMode.toString(), jmxDomain, agent.getAssignedPort(), daemon.getPort());
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

    private int parsePortNumber(String portVal, String portType) {
        int port;
        try {
            port = Integer.parseInt(portVal);
            if (port < 0) {
                throw new IllegalArgumentException("Invalid " + portType + " port number specified in runtime.properties:" + port);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + portType + " port", e);
        }
        return port;
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


    private BootConfiguration createBootConfiguration(StandaloneRuntime runtime, ClassLoader bootClassLoader)
            throws BootstrapException, InitializationException {
        StandaloneHostInfo hostInfo = runtime.getHostInfo();
        BootConfiguration configuration = new BootConfiguration();
        configuration.setBootClassLoader(bootClassLoader);

        Bootstrapper bootstrapper = BootstrapHelper.createBootstrapper(hostInfo, bootClassLoader);
        // create the runtime bootrapper
        configuration.setBootstrapper(bootstrapper);

        // process extensions
        File repositoryDirectory = hostInfo.getRepositoryDirectory();
        RepositoryScanner scanner = new RepositoryScanner();
        ScanResult result = scanner.scan(repositoryDirectory);
        configuration.setExtensionContributions(result.getExtensionContributions());
        configuration.setUserContributions(result.getUserContributions());

        // process the baseline intents
        ContributionSource source = getIntentsContribution(hostInfo.getConfigDirectory());
        configuration.setIntents(source);
        configuration.setRuntime(runtime);
        return configuration;
    }

    private ContributionSource getIntentsContribution(File dir) throws InitializationException {
        try {
            File file = new File(dir, INTENTS_FILE);
            if (!file.exists()) {
                return null;
            }
            URL location = file.toURI().toURL();
            return new FileContributionSource(Names.CORE_INTENTS_CONTRIBUTION, location, -1, new byte[0]);
        } catch (MalformedURLException e) {
            throw new InitializationException(e);
        }
    }
    public interface ServerMonitor {
        @Severe
        void runError(Exception e);

        @Info
        void started(String mode, String domain, int jmxPort, int monitorPort);

        @Info
        void stopped(String domain);

    }


}
