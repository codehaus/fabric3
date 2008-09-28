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
 */
package org.fabric3.ftp.server.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Represents a passive data connection.
 *
 * @version $Revision$ $Date$
 */
public class PassiveDataConnection implements DataConnection {

    private ServerSocket serverSocket;
    private InetAddress bindAddress;
    private int passivePort;
    private Socket socket;
    private int idleTimeout;

    /**
     * Initializes the passive data connection.
     *
     * @param bindAddress the address to bind the socket to.
     * @param passivePort Passive port.
     * @param idleTimeout the time to wait in milliseconds for an accept() operation on the passive socket.
     */
    public PassiveDataConnection(InetAddress bindAddress, int passivePort, int idleTimeout) {
        this.bindAddress = bindAddress;
        this.passivePort = passivePort;
        this.idleTimeout = idleTimeout;
    }

    /**
     * Closes the data connection.
     */
    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignore1) {
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException ignore2) {
            }
        }
    }

    /**
     * Get an input stream to the data connection.
     *
     * @return Input stream to the data cnnection.
     * @throws IOException If unable to get input stream.
     */
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    /**
     * Get an output stream to the data connection.
     *
     * @return Output stream to the data connection.
     * @throws IOException If unable to get output stream.
     */
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    /**
     * Opens the data connection.
     *
     * @throws IOException If unable to open connection.
     */
    public void open() throws IOException {
        socket = serverSocket.accept();
    }

    /**
     * Initializes a data connection.
     *
     * @throws IOException If unable to open connection.
     */
    public void initialize() throws IOException {
        serverSocket = new ServerSocket();
        // set the timeout to wait for the client to respond
        serverSocket.setSoTimeout(idleTimeout);
        SocketAddress address = new InetSocketAddress(bindAddress, passivePort);
        serverSocket.bind(address);
    }

}
