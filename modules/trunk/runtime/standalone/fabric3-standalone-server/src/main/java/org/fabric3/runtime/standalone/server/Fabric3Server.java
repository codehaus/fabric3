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
package org.fabric3.runtime.standalone.server;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.fabric3.api.annotation.LogLevel;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.jmx.agent.Agent;
import org.fabric3.jmx.agent.rmi.RmiAgent;
import org.fabric3.runtime.standalone.BootstrapHelper;
import org.fabric3.runtime.standalone.StandaloneHostInfo;
import org.fabric3.runtime.standalone.StandaloneRuntime;
import org.fabric3.monitor.MonitorFactory;

/**
 * This class provides the commandline interface for starting the Fabric3 standalone server. <p/> <p/> The class boots the Fabric3 server and also
 * starts a JMX server and listens for shutdown command. The server itself is available by the object name
 * <code>fabric3:type=server,name=fabric3Server </code>. It also allows a runtime to be booted given a bootpath. The JMX domain in which the runtime
 * is registered si definied in the file <code>$bootPath/etc/runtime.properties</code>. The properties defined are <code>jmx.domain</code> and
 * <code>offline</code>. </p> <p/> <p/> The install directory can be specified using the system property <code>fabric3.installDir</code>. If not
 * specified it is asumed to be the directory from where the JAR file containing the main class is loaded. </p>
 * <p/>
 * <p/>
 * The administration port can be specified using the system property <code>fabric3.adminPort</code>.If not specified the default port that is used is
 * <code>1099</code>
 *
 * @version $Rev$ $Date$
 */
public class Fabric3Server implements Fabric3ServerMBean {
    private static final String JMX_DOMAIN = "fabric3.jmx";

    private final Agent agent;

    /**
     * Install directory
     */
    private final File installDirectory;

    /**
     * Started runtimes.
     */
    private final Map<String, RuntimeLifecycleCoordinator<StandaloneRuntime, Bootstrapper>> bootedRuntimes =
            new ConcurrentHashMap<String, RuntimeLifecycleCoordinator<StandaloneRuntime, Bootstrapper>>();
    private ServerMonitor monitor;

    /**
     * Main method.
     *
     * @param args Commandline arguments.
     * @throws Exception if there is a problem starting the runtime
     */
    public static void main(String[] args) throws Exception {
        Fabric3Server server = new Fabric3Server();
        server.start();
        String jmxDomain = System.getProperty(JMX_DOMAIN, "standalone");
        server.startRuntime(jmxDomain);
        server.run();
        for (String bootPath : server.bootedRuntimes.keySet()) {
            server.shutdownRuntime(bootPath);
        }
        System.exit(0);
    }

    /**
     * Constructor initializes all the required classloaders.
     *
     * @throws MalformedURLException if the install directory is invalid
     */
    private Fabric3Server() throws MalformedURLException {
        installDirectory = BootstrapHelper.getInstallDirectory(Fabric3Server.class);
        agent = RmiAgent.newInstance();
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

            // create the classloaders for booting the runtime
            String bootPath = props.getProperty("fabric3.bootDir", null);
            File bootDir = BootstrapHelper.getDirectory(installDirectory, bootPath, "boot");

            String hostPath = props.getProperty("fabric3.hostDir", null);
            File hostDir = BootstrapHelper.getDirectory(installDirectory, hostPath, "host");

            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            ClassLoader hostLoader = BootstrapHelper.createClassLoader(systemClassLoader, hostDir);
            ClassLoader bootLoader = BootstrapHelper.createClassLoader(hostLoader, bootDir);

            // create the HostInfo and runtime
            hostInfo = BootstrapHelper.createHostInfo(installDirectory, configDir, props);
            MonitorFactory monitorFactory = BootstrapHelper.createMonitorFactory(bootLoader, props);
            runtime = BootstrapHelper.createRuntime(hostInfo, bootLoader, monitorFactory);

            runtime.setMBeanServer(agent.getMBeanServer());
            runtime.setJMXDomain(jmxDomain);
            monitor = runtime.getMonitorFactory().getMonitor(ServerMonitor.class);

            // boot the runtime
            Bootstrapper bootstrapper = BootstrapHelper.createBootstrapper(hostInfo, bootLoader);
            RuntimeLifecycleCoordinator<StandaloneRuntime, Bootstrapper> coordinator = BootstrapHelper.createCoordinator(hostInfo, bootLoader);

            // load the primordial system components
            coordinator.bootPrimordial(runtime, bootstrapper, bootLoader, hostLoader);

            // load and initialize runtime extension components and the local runtime domain
            coordinator.initialize();

            // join a distributed domain
            Future<Void> future = coordinator.joinDomain(10000);
            future.get();
            // perform recovery. If the runtime is a controller node, this may result in reprovisioning components
            future = coordinator.recover();
            future.get();
            // start the runtime receiving requests
            future = coordinator.start();
            future.get();
            bootedRuntimes.put(jmxDomain, coordinator);
            monitor.started(jmxDomain);
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
            RuntimeLifecycleCoordinator<StandaloneRuntime, Bootstrapper> coordinator = bootedRuntimes.get(bootPath);
            if (coordinator != null) {
                coordinator.shutdown();
                bootedRuntimes.remove(bootPath);
            }
            monitor.stopped(bootPath);
        } catch (ShutdownException ex) {
            monitor.runError(ex);
            throw new Fabric3ServerException(ex);
        }

    }

    public final void run() {
        agent.run();
    }

    /**
     * Shuts the server down.
     */
    public final void shutdown() {
        agent.shutdown();
    }

    /**
     * Starts the server and starts the JMX agent.
     */
    private void start() {
        agent.start();
        agent.register(this, "fabric3:type=server,name=fabric3Server");
    }

    public interface ServerMonitor {
        @LogLevel("SEVERE")
        void runError(Exception e);

        @LogLevel("INFO")
        void started(String profile);

        @LogLevel("INFO")
        void stopped(String profile);

    }


}
