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
import java.io.InputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.net.ftp.FTPClient;
import org.fabric3.ftp.server.codec.CodecFactory;
import org.fabric3.ftp.server.handler.PassCommandHandler;
import org.fabric3.ftp.server.handler.UserCommandHandler;
import org.fabric3.ftp.server.protocol.RequestHandler;
import org.fabric3.ftp.server.security.FileSystemUserManager;

/**
 *
 * @version $Revision$ $Date$
 */
public class F3FtpHostTest extends TestCase {
    
    private F3FtpHost ftpHost;
    
    public void setUp() throws Exception {
        
        InputStream users = getClass().getClassLoader().getResourceAsStream("user.properties");
        FileSystemUserManager userManager = new FileSystemUserManager();
        userManager.setUsers(users);
        
        Map<String, RequestHandler> requestHandlers = new HashMap<String, RequestHandler>();
        requestHandlers.put("USER", new UserCommandHandler());
        PassCommandHandler passCommandHandler = new PassCommandHandler();
        passCommandHandler.setUserManager(userManager);
        requestHandlers.put("PASS", passCommandHandler);
        
        ftpHost = new F3FtpHost();
        
        FtpHandler ftpHandler = new FtpHandler();
        ftpHandler.setFtpCommands(requestHandlers);
        
        ftpHost.setFtpHandler(ftpHandler);
        ftpHost.setCommandPort(1234);
        ftpHost.setCodecFactory(new CodecFactory());
        ftpHost.start();
        
    }
    
    public void tearDown() throws Exception {
        ftpHost.stop();
    }

    public void testValidLogin() throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(InetAddress.getLocalHost(), 1234);
        ftpClient.user("meeraj");
        assertEquals(230, ftpClient.pass("password"));        
    }

    public void testInvalidLogin() throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(InetAddress.getLocalHost(), 1234);
        ftpClient.user("meeraj");
        assertEquals(530, ftpClient.pass("password1"));        
    }

}
