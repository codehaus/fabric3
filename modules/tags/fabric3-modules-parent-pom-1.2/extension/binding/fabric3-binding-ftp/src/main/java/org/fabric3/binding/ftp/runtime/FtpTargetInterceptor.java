/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
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

import org.fabric3.binding.ftp.provision.FtpSecurity;
import org.fabric3.ftp.api.FtpConstants;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.wire.Interceptor;

/**
 * @version $Rev$ $Date$
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

            if (remotePath != null && remotePath.length() > 0) {
                remoteFileLocation = remotePath.endsWith("/") ? remotePath + fileName : remotePath + "/" + fileName;
            }

            String remoteTmpFileLocation = remoteFileLocation;
            if (tmpFileSuffix != null && tmpFileSuffix.length() > 0) {
                remoteTmpFileLocation += tmpFileSuffix;
            }

            monitor.onCommand("STOR " + remoteFileLocation);
            if (!ftpClient.storeFile(remoteTmpFileLocation, data)) {
                throw new ServiceUnavailableException("Unable to upload data. Response sent from server: " + ftpClient.getReplyString() +
                        " ,remoteFileLocation:" + remoteFileLocation);
            }
            monitor.onResponse(ftpClient.getReplyString());

            //Rename file back to original name if temporary file suffix was used while transmission.
            if (!remoteTmpFileLocation.equals(remoteFileLocation)) {
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