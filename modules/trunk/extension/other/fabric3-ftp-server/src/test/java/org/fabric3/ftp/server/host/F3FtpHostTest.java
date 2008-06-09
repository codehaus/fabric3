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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.net.ftp.FTPClient;
import org.fabric3.ftp.server.codec.CodecFactory;
import org.fabric3.ftp.server.ftplet.DefaultFtpLetContainer;
import org.fabric3.ftp.server.handler.PassRequestHandler;
import org.fabric3.ftp.server.handler.PasvRequestHandler;
import org.fabric3.ftp.server.handler.StorRequestHandler;
import org.fabric3.ftp.server.handler.UserRequestHandler;
import org.fabric3.ftp.server.passive.PassiveConnectionServiceImpl;
import org.fabric3.ftp.server.protocol.RequestHandler;
import org.fabric3.ftp.server.security.FileSystemUserManager;
import org.fabric3.ftp.spi.FtpLetContainer;

/**
 *
 * @version $Revision$ $Date$
 */
public class F3FtpHostTest extends TestCase {
    
    private F3FtpHost ftpHost;
    
    public void setUp() throws Exception {
        
        Map<String, RequestHandler> requestHandlers = new HashMap<String, RequestHandler>();
        
        Map<String, String> users = new HashMap<String, String>();
        users.put("meeraj", "password");
        FileSystemUserManager userManager = new FileSystemUserManager();
        userManager.setUsers(users);
        requestHandlers.put("USER", new UserRequestHandler());
        
        PassRequestHandler passCommandHandler = new PassRequestHandler();
        passCommandHandler.setUserManager(userManager);
        requestHandlers.put("PASS", passCommandHandler);
        
        PassiveConnectionServiceImpl passiveConnectionService = new PassiveConnectionServiceImpl();
        passiveConnectionService.setMinPort(50000);
        passiveConnectionService.setMaxPort(60000);
        passiveConnectionService.setPassiveAddress("127.0.0.1");
        passiveConnectionService.init();
        PasvRequestHandler pasvRequestHandler = new PasvRequestHandler();
        pasvRequestHandler.setPassivePortService(passiveConnectionService);
        requestHandlers.put("PASV", pasvRequestHandler);
        
        StorRequestHandler storRequestHandler = new StorRequestHandler();
        storRequestHandler.setPassivePortService(passiveConnectionService);
        FtpLetContainer ftpLetContainer = new DefaultFtpLetContainer();
        ftpLetContainer.registerFtpLet("/", new DummyFtpLet());
        storRequestHandler.setFtpLetContainer(ftpLetContainer);
        requestHandlers.put("STOR", storRequestHandler);
        
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
    
    public void testStor() throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(InetAddress.getLocalHost(), 1234);
        ftpClient.user("meeraj");
        ftpClient.pass("password");
        ftpClient.enterLocalPassiveMode();
        ftpClient.storeFile("/resource/test.dat", new ByteArrayInputStream("TEST\r\n".getBytes()));
    }

}
