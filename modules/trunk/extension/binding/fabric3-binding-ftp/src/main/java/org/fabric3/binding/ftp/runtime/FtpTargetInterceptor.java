/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the ÒLicenseÓ), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an Òas isÓ basis,
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
