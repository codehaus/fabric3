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
import org.fabric3.ftp.server.monitor.FtpMonitor;
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
        
        FtpMonitor ftpMonitor = new TestFtpMonitor();
        
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
        passiveConnectionService.init();
        PasvRequestHandler pasvRequestHandler = new PasvRequestHandler();
        pasvRequestHandler.setPassivePortService(passiveConnectionService);
        requestHandlers.put("PASV", pasvRequestHandler);
        
        StorRequestHandler storRequestHandler = new StorRequestHandler();
        storRequestHandler.setPassivePortService(passiveConnectionService);
        storRequestHandler.setFtpMonitor(ftpMonitor);
        FtpLetContainer ftpLetContainer = new DefaultFtpLetContainer();
        ftpLetContainer.registerFtpLet("/", new DummyFtpLet());
        storRequestHandler.setFtpLetContainer(ftpLetContainer);
        requestHandlers.put("STOR", storRequestHandler);
        
        ftpHost = new F3FtpHost();
        
        FtpHandler ftpHandler = new FtpHandler();
        ftpHandler.setRequestHandlers(requestHandlers);
        ftpHandler.setFtpMonitor(ftpMonitor);
        
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
