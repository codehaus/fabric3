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
package org.fabric3.binding.ftp.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.net.SocketFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.oasisopen.sca.ServiceUnavailableException;

import org.fabric3.api.ftp.FtpConstants;
import org.fabric3.binding.ftp.provision.FtpSecurity;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.wire.Interceptor;

/**
 * @version $Revision$ $Date$
 */
public class FtpTargetInterceptor implements Interceptor {

    private Interceptor next;
    private final int port;
    private final InetAddress hostAddress;
    private String remotePath;
    private String tmpFileSuffix;
    private final int timeout;
    private SocketFactory factory;
    private List<String> commands;
    private FtpInterceptorMonitor monitor;

    private final FtpSecurity security;
    private final boolean active;

    public FtpTargetInterceptor(InetAddress hostAddress,
                                int port,
                                FtpSecurity security,
                                boolean active,
                                int timeout,
                                SocketFactory factory,
                                List<String> commands,
                                FtpInterceptorMonitor monitor) throws UnknownHostException {
        this.hostAddress = hostAddress;
        this.port = port;
        this.security = security;
        this.active = active;
        this.timeout = timeout;
        this.factory = factory;
        this.commands = commands;
        this.monitor = monitor;
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
            monitor.onConnect(hostAddress, port);
            ftpClient.connect(hostAddress, port);
            monitor.onResponse(ftpClient.getReplyString());
            String type = msg.getWorkContext().getHeader(String.class, FtpConstants.HEADER_CONTENT_TYPE);
            if (type != null && type.equalsIgnoreCase(FtpConstants.BINARY_TYPE)) {
                monitor.onCommand("TYPE I");
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                monitor.onResponse(ftpClient.getReplyString());
            } else if (type != null && type.equalsIgnoreCase(FtpConstants.TEXT_TYPE)) {
                monitor.onCommand("TYPE A");
                ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
                monitor.onResponse(ftpClient.getReplyString());
            }

            /*if (!ftpClient.login(security.getUser(), security.getPassword())) {
                throw new ServiceUnavailableException("Invalid credentials");
            }*/
            // TODO Fix above
            monitor.onAuthenticate();
            ftpClient.login(security.getUser(), security.getPassword());
            monitor.onResponse(ftpClient.getReplyString());

            Object[] args = (Object[]) msg.getBody();
            String fileName = (String) args[0];
            String remoteFileLocation = fileName;
            InputStream data = (InputStream) args[1];

            if (active) {
                monitor.onCommand("ACTV");
                ftpClient.enterLocalActiveMode();
                monitor.onResponse(ftpClient.getReplyString());
            } else {
                monitor.onCommand("PASV");
                ftpClient.enterLocalPassiveMode();
                monitor.onResponse(ftpClient.getReplyString());
            }
            if (commands != null) {
                for (String command : commands) {
                    monitor.onCommand(command);
                    ftpClient.sendCommand(command);
                    monitor.onResponse(ftpClient.getReplyString());
                }
            }
            
            if(remotePath != null && remotePath.length() > 0) {
                remoteFileLocation = remotePath.endsWith("/") ? remotePath + fileName : remotePath + "/" + fileName;
            }
            
            String remoteTmpFileLocation = remoteFileLocation;
            if(tmpFileSuffix != null && tmpFileSuffix.length() > 0) {
                remoteTmpFileLocation += tmpFileSuffix;
            }
            
            monitor.onCommand("STOR " + remoteFileLocation);
            if (!ftpClient.storeFile(remoteTmpFileLocation, data)) {
                throw new ServiceUnavailableException("Unable to upload data. Response sent from server: " + ftpClient.getReplyString() +
                                                      " ,remoteFileLocation:" + remoteFileLocation);
            }
            monitor.onResponse(ftpClient.getReplyString());
            
            //Rename file back to original name if temporary file suffix was used while transmission.
            if(!remoteTmpFileLocation.equals(remoteFileLocation)) {
                ftpClient.rename(remoteTmpFileLocation, remoteFileLocation);
            }
        } catch (IOException e) {
            throw new ServiceUnavailableException(e);
        }

        return new MessageImpl();
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    /**
     * Sets remote path for the STOR operation.
     * 
     * @param remotePath remote path for the STOR operation
     */
    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    /**
     * Sets temporary file suffix to be used while file being transmitted.
     * 
     * @param tmpFileSuffix temporary file suffix to be used for file in transmission
     */
    public void setTmpFileSuffix(String tmpFileSuffix) {
        this.tmpFileSuffix = tmpFileSuffix;
    }

}