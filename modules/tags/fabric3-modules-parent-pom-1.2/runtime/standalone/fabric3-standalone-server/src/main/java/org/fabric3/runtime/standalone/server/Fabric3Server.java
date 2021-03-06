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
package org.fabric3.runtime.standalone.server;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.fabric3.api.annotation.logging.Info;
import org.fabric3.api.annotation.logging.Severe;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.BootstrapHelper;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.MaskingClassLoader;
import org.fabric3.host.runtime.RepositoryScanner;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ScanResult;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.jmx.agent.rmi.RmiAgent;

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
    private static final String HIDE_PACKAGES = "fabric3.hidden.packages";

    private final File installDirectory;
    private RuntimeLifecycleCoordinator coordinator;
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
        server.shutdownRuntime();

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
        HostInfo hostInfo;
        Fabric3Runtime<HostInfo> runtime;
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
            String hiddenPackageString = (String) props.get(HIDE_PACKAGES);
            if (hiddenPackageString != null && hiddenPackageString.length() > 0) {
                // mask hidden JDK and system classpath packages
                String[] hiddenPackages = hiddenPackageString.split(",");
                systemClassLoader = new MaskingClassLoader(systemClassLoader, hiddenPackages);
            }
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
            RmiAgent agent;
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

    public final void shutdownRuntime() {

        try {
            if (coordinator != null) {
                coordinator.shutdown();
            }
            monitor.stopped();
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


    private BootConfiguration createBootConfiguration(Fabric3Runtime<HostInfo> runtime, ClassLoader bootClassLoader) throws InitializationException {
        HostInfo hostInfo = runtime.getHostInfo();
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

        configuration.setRuntime(runtime);
        return configuration;
    }

    public interface ServerMonitor {
        @Severe
        void runError(Exception e);

        @Info
        void started(String mode, String domain, int jmxPort, int monitorPort);

        @Info
        void stopped();

    }


}
