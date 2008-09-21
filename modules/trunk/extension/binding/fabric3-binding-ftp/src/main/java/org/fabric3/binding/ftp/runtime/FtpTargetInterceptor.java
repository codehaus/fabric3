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
package org.fabric3.binding.ftp.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.net.SocketFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.osoa.sca.ServiceUnavailableException;

import org.fabric3.binding.ftp.provision.FtpSecurity;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.ftp.api.FtpConstants;

/**
 * @version $Revision$ $Date$
 */
public class FtpTargetInterceptor implements Interceptor {

    private Interceptor next;
    private final int port;
    private final InetAddress hostAddress;
    private final int timeout;
    private SocketFactory factory;

    private final FtpSecurity security;
    private final boolean active;

    public FtpTargetInterceptor(InetAddress hostAddress, int port, FtpSecurity security, boolean active, int timeout, SocketFactory factory)
            throws UnknownHostException {
        this.hostAddress = hostAddress;
        this.port = port;
        this.security = security;
        this.active = active;
        this.timeout = timeout;
        this.factory = factory;
    }

    public Interceptor getNext() {
        return next;
    }

    public Message invoke(Message msg) {

        FTPClient ftpClient = new FTPClient();
        ftpClient.setSocketFactory(factory);
        try {
            if (timeout > 0) {
                ftpClient.setDefaultTimeout(timeout);
                ftpClient.setDataTimeout(timeout);
            }
            ftpClient.connect(hostAddress, port);
            String type = msg.getWorkContext().getHeader(String.class, FtpConstants.HEADER_CONTENT_TYPE);
            if (type != null && type.equalsIgnoreCase(FtpConstants.BINARY_TYPE)) {
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            } else if (type != null && type.equalsIgnoreCase(FtpConstants.TEXT_TYPE)) {
                ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
            }

            /*if (!ftpClient.login(security.getUser(), security.getPassword())) {
                throw new ServiceUnavailableException("Invalid credentials");
            }*/
            // TODO Fix above
            ftpClient.login(security.getUser(), security.getPassword());

            Object[] args = (Object[]) msg.getBody();
            String fileName = (String) args[0];
            InputStream data = (InputStream) args[1];

            if (active) {
                ftpClient.enterLocalActiveMode();
            } else {
                ftpClient.enterLocalPassiveMode();
            }

            if (!ftpClient.storeFile(fileName, data)) {
                throw new ServiceUnavailableException("Unable to upload data. Response sent from server: " + ftpClient.getReplyString());
            }

        } catch (IOException e) {
            throw new ServiceUnavailableException(e);
        }

        return new MessageImpl();
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

}
