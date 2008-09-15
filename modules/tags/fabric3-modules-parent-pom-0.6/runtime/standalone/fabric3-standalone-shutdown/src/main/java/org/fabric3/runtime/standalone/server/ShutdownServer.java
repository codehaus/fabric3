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

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import javax.management.JMException;

/**
 * @version $Rev$ $Date$
 */
public class ShutdownServer {
    private static final String MONITOR_PORT_PARAM = "fabric3.monitor.port";
    private static final String MONITOR_KEY_PARAM = "fabric3.monitor.key";
    /**
     * Fabric3 admin host.
     */
    private static final String ADMIN_HOST_PROPERTY = "fabric3.adminHost";

    /**
     * Fabric3 admin port.
     */
    private static final String ADMIN_PORT_PROPERTY = "fabric3.adminPort";

    /**
     * Default host.
     */
    private static final String DEFAULT_ADMIN_HOST = "localhost";

    /**
     * Default port.
     */
    private static final int DEFAULT_ADMIN_PORT = 1099;

    /**
     * Host.
     */
    private String host = DEFAULT_ADMIN_HOST;

    /**
     * Port.
     */
    private int port = DEFAULT_ADMIN_PORT;

    /**
     * @param args Commandline arguments.
     */
    public static void main(String[] args) throws Exception {
        String monitorKey = System.getProperty(MONITOR_KEY_PARAM, "f3");
        String portVal = System.getProperty(MONITOR_PORT_PARAM, "8083");
        int monitorPort;
        try {
            monitorPort = Integer.parseInt(portVal);
            if (monitorPort < 0) {
                throw new IllegalArgumentException("Invalid port number:" + monitorPort);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port", e);
        }

        ShutdownServer shutdownServer = new ShutdownServer();
        shutdownServer.shutdown(monitorKey, monitorPort);

    }

    /**
     * Initializes the host and the port.
     */
    private ShutdownServer() {
        host = System.getProperty(ADMIN_HOST_PROPERTY, DEFAULT_ADMIN_HOST);
        port = Integer.getInteger(ADMIN_PORT_PROPERTY, DEFAULT_ADMIN_PORT);
    }

    private void shutdown(String monitorKey, int monitorPort) throws IOException, JMException {
        try {
            Socket s = new Socket(InetAddress.getByName("127.0.0.1"), monitorPort);
            OutputStream out = s.getOutputStream();
            out.write((monitorKey + "\r\nstop\r\n").getBytes());
            out.flush();
            s.shutdownOutput();
            s.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
//        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/server");
//        RMIConnector rmiConnector = new RMIConnector(url, null);
//        rmiConnector.connect();
//
//        MBeanServerConnection con = rmiConnector.getMBeanServerConnection();
//        con.invoke(new ObjectName("fabric3:type=server,name=fabric3Server"), "shutdown", null, null);
    }
}
