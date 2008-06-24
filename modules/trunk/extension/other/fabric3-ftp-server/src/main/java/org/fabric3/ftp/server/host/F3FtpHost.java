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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.mina.common.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

/**
 * F3 implementation of the in-process FTP host.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class F3FtpHost implements FtpHost {

    private int commandPort = 21;
    private SocketAcceptor acceptor;
    private IoHandler ftpHandler;
    private ProtocolCodecFactory codecFactory;

    /**
     * Starts the FTP server.
     *
     * @throws IOException If unable to start the FTP server.
     */
    @Init
    public void start() throws IOException {

        ExecutorService filterExecutor = Executors.newCachedThreadPool();
        InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getLocalHost(), commandPort);
        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(filterExecutor));
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(codecFactory));
        acceptor.setHandler(ftpHandler);
        acceptor.bind(socketAddress);

    }

    /**
     * Stops the FTP server.
     */
    @Destroy
    public void stop() {
        acceptor.unbind();
        acceptor.dispose();
    }

    /**
     * Sets the handler for the FTP commands.
     *
     * @param ftpHandler FTP Handler.
     */
    @Reference
    public void setFtpHandler(IoHandler ftpHandler) {
        this.ftpHandler = ftpHandler;
    }

    /**
     * Sets the protocol codec factory.
     *
     * @param codecFactory Protocol codec.
     */
    @Reference
    public void setCodecFactory(ProtocolCodecFactory codecFactory) {
        this.codecFactory = codecFactory;
    }

    /**
     * Sets the FTP command port.
     *
     * @param commandPort Command port.
     */
    @Property
    public void setCommandPort(int commandPort) {
        this.commandPort = commandPort;
    }

}
