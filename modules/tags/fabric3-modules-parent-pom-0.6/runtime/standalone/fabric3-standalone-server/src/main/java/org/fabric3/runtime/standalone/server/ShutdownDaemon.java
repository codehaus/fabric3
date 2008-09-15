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

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * Listens for server stop events
 *
 * @version $Rev$ $Date$
 */
public class ShutdownDaemon extends Thread {
    private ServerSocket serverSocket;
    private String key;
    private CountDownLatch latch;

    ShutdownDaemon(int port, String key, CountDownLatch latch) {
        this.key = key;
        this.latch = latch;
        try {
            if (port < 0)
                return;
            setDaemon(true);
            setName("StopMonitor");
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
            if (port == 0) {
                port = serverSocket.getLocalPort();
            }
        }
        catch (Exception e) {
            System.err.println(e.toString());
        }
        if (serverSocket != null) {
            this.start();
        } else {
            System.err.println("Eror initiating monitor listener on port: " + port);
        }
    }

    public void run() {
        while (true) {
            Socket socket = null;
            try {
                socket = this.serverSocket.accept();

                InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
                LineNumberReader reader = new LineNumberReader(streamReader);
                String key = reader.readLine();
                if (!this.key.equals(key)) {
                    continue;
                }
                String cmd = reader.readLine();
                if ("stop".equals(cmd)) {
                    try {
                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        this.serverSocket.close();
                        latch.countDown();
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if ("status".equals(cmd)) {
                    socket.getOutputStream().write("OK\r\n".getBytes());
                    socket.getOutputStream().flush();
                }
            } catch (Exception e) {
                System.err.println(e);
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (Exception e) {
                        //ok
                    }
                }
                socket = null;
            }
        }
    }

}
