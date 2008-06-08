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
import java.net.URI;

import org.apache.commons.net.ftp.FTPClient;
import org.fabric3.binding.ftp.provision.FtpSecurity;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.wire.Interceptor;
import org.osoa.sca.ServiceUnavailableException;

/**
 *
 * @version $Revision$ $Date$
 */
public class FtpTargetInterceptor implements Interceptor {
    
    private Interceptor next;
    private final URI uri;
    private final FtpSecurity security;
    private final boolean active;

    public FtpTargetInterceptor(URI uri, FtpSecurity security, boolean active) {
        this.uri = uri;
        this.security = security;
        this.active = active;
    }

    public Interceptor getNext() {
        return next;
    }

    public Message invoke(Message msg) {
        
        FTPClient ftpClient = new FTPClient();
        
        try {
            
            ftpClient.connect(InetAddress.getByName(uri.toString()));
            
            if (ftpClient.login(security.getUser(), security.getPassword())) {
                throw new ServiceUnavailableException("Invalid credentials");
            }
            
            Object[] args = (Object[]) msg.getBody();
            String fileName = (String) args[0];
            InputStream data = (InputStream) args[1];
            
            if (active) {
                ftpClient.enterLocalActiveMode();
            } else {
                ftpClient.enterLocalPassiveMode();
            }
            
            if (ftpClient.storeFile(fileName, data)) {
                throw new ServiceUnavailableException("Unable to upload data");
            }
            
        } catch (IOException e) {
           throw new ServiceUnavailableException(e);
        }

        return null;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

}
