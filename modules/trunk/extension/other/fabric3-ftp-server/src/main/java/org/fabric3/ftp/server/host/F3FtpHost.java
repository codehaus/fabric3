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
package org.fabric3.ftp.server.host;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.common.IoHandler;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.fabric3.ftp.api.FtpLet;
import org.fabric3.ftp.spi.FtpHost;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

/**
 * F3 implementation of the in-process FTP host.
 *
 * @version $Revision$ $Date$
 */
public class F3FtpHost implements FtpHost {
    
    private Map<String, FtpLet> ftpLets = new HashMap<String, FtpLet>();
    private int commandPort = 21;
    private SocketAcceptor socketAcceptor;
    private IoHandler ftpHandler;
    
    /**
     * Registers an FTP let for the specified path.
     * 
     * @param path Path on which the FtpLet is listening.
     * @param ftpLet FtpLet listening for the upload request.
     */
    public void registerFtpLet(String path, FtpLet ftpLet) {
        ftpLets.put(path, ftpLet);
    }

    /**
     * Sets the handler for the FTP commands.
     */
    @Reference
    public void setFtpHandler(IoHandler ftpHandler) {
        this.ftpHandler = ftpHandler;
    }

    /**
     * Sets the FTP command port.
     * @param commandPort Command port.
     */
    @Property
    public void setCommandPort(int commandPort) {
        this.commandPort = commandPort;
    }
    
    /**
     * Starts the FTP server.
     * @throws IOException 
     */
    @Init
    public void start() throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getLocalHost(), commandPort);
        socketAcceptor = new NioSocketAcceptor();
        socketAcceptor.setHandler(ftpHandler);
        socketAcceptor.bind(socketAddress);
    }
    
    /**
     * Stops the FTP server.
     */
    @Destroy
    public void stop() {
        socketAcceptor.unbind();
    }

}
