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
package org.fabric3.jmx.agent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Abstract super class for all the agents.
 *
 * @version $Revison$ $Date$
 */
public abstract class AbstractAgent implements Agent {

    private static final String DOMAIN = "fabric3";
    private MBeanServer mBeanServer;
    private AtomicBoolean started = new AtomicBoolean();
    private JMXConnectorServer connectorServer;
    private int minPort;
    private int maxPort;
    private int port = -1;

    /**
     * Initialies the server.
     *
     * @throws ManagementException If unable to start the agent.
     */
    protected AbstractAgent(int minPort, int maxPort) throws ManagementException {
        mBeanServer = MBeanServerFactory.createMBeanServer(DOMAIN);
        this.minPort = minPort;
        this.maxPort = maxPort;
    }

    /**
     * @see org.fabric3.jmx.agent.Agent#getMBeanServer()
     */
    public MBeanServer getMBeanServer() {
        return mBeanServer;
    }

    /**
     * @see org.fabric3.jmx.agent.Agent#register(java.lang.Object,java.lang.String)
     */
    public final void register(Object instance, String name) throws ManagementException {

        try {
            mBeanServer.registerMBean(instance, new ObjectName(name));
        } catch (Exception ex) {
            throw new ManagementException(ex);
        }

    }

    /**
     * @see org.fabric3.jmx.agent.Agent#start()
     */
    public final void start() throws ManagementException {

        try {

            if (started.get()) {
                throw new IllegalArgumentException("Agent already started");
            }

            preStart();

            JMXServiceURL url = getAdaptorUrl();
            connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mBeanServer);

            connectorServer.start();

            started.set(true);

        } catch (MalformedURLException ex) {
            throw new ManagementException(ex);
        } catch (IOException ex) {
            throw new ManagementException(ex);
        }

    }

    public final void run() {
        while (started.get()) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // continue;
                }
            }
        }
    }

    /**
     * @see org.fabric3.jmx.agent.Agent#shutdown()
     */
    public final void shutdown() throws ManagementException {

        try {

            if (!started.get()) {
                throw new IllegalArgumentException("Agent not started");
            }

            connectorServer.stop();
            postStop();
            started.set(false);
            synchronized (this) {
                notify();
            }

        } catch (IOException ex) {
            throw new ManagementException(ex);
        }

    }
    
    /**
     * Returns the listen port for the agent.
     * @return The listen port for the agent.
     */
    protected int getPort() {
        
        if (port == -1) {
            port = getAvvailablePort();
        }
        if (port == -1) {
            throw new IllegalStateException("Unable to bind to management ports between " + minPort + " and " + maxPort);
        }
        return port;
    }

    /**
     * Gets the adaptor URL.
     *
     * @return Adaptor URL.
     */
    protected abstract JMXServiceURL getAdaptorUrl();

    /**
     * Any initialiation required for protocol specific agent.
     */
    protected abstract void preStart();

    /**
     * Any initialiation required for protocol specific agent.
     */
    protected abstract void postStop();
    
    /*
     * Gets the next available port.
     */
    private int getAvvailablePort() {
        
        for (int i = minPort;i < maxPort;i++) {
            
            try {
                ServerSocket serverSocket = new ServerSocket();
                InetAddress inetAddress = InetAddress.getLocalHost();
                SocketAddress socketAddress = new InetSocketAddress(inetAddress, i);
                serverSocket.bind(socketAddress);
                serverSocket.close();
                return i;
            } catch (IOException ex) {
                continue;
            }
            
        }
        
        return -1;
        
    }

}
