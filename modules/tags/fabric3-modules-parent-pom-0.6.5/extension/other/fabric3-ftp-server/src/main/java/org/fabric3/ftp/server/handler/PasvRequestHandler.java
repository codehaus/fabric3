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
package org.fabric3.ftp.server.handler;

import java.io.IOException;
import java.net.InetAddress;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.ftp.server.data.DataConnection;
import org.fabric3.ftp.server.data.PassiveDataConnection;
import org.fabric3.ftp.server.passive.PassiveConnectionService;
import org.fabric3.ftp.server.protocol.DefaultResponse;
import org.fabric3.ftp.server.protocol.FtpSession;
import org.fabric3.ftp.server.protocol.Request;
import org.fabric3.ftp.server.protocol.RequestHandler;

/**
 * Handles the <code>PASV</code> command.
 *
 * @version $Revision$ $Date$
 */
public class PasvRequestHandler implements RequestHandler {

    private PassiveConnectionService passiveConnectionService;
    private String listenAddress;
    private int idleTimeout = 60000;  // 60 seconds default

    /**
     * Initializes the passive data connection on request of <code>PASV</code> command from an authenticated user.
     *
     * @param request Object the encapsuates the current FTP command.
     */
    public void service(Request request) {

        FtpSession session = request.getSession();

        if (!session.isAuthenticated()) {
            session.write(new DefaultResponse(530, "Access denied"));
            return;
        }

        int passivePort = 0;

        try {

            passivePort = passiveConnectionService.acquire();

            InetAddress localAddress;
            if (listenAddress == null) {
                localAddress = InetAddress.getLocalHost();
            } else {
                localAddress = InetAddress.getByName(listenAddress);
            }

            String socketAddress = localAddress.getHostAddress().replace('.', ',') + ',' + (passivePort >> 8) + ',' + (passivePort & 0xFF);
            session.setPassivePort(passivePort);

            DataConnection dataConnection = new PassiveDataConnection(localAddress, passivePort, idleTimeout);
            dataConnection.initialize();
            session.setDataConnection(dataConnection);

            session.write(new DefaultResponse(227, "Entering Passive Mode (" + socketAddress + ")"));

        } catch (InterruptedException e) {
            session.write(new DefaultResponse(427, "Can't open passive connection"));
        } catch (IOException e) {
            passiveConnectionService.release(passivePort);
            session.write(new DefaultResponse(427, "Can't open passive connection"));
        }

    }

    @Property
    public void setListenAddress(String listenAddress) {
        this.listenAddress = listenAddress;
    }

    /**
     * Sets the optional timeout in milliseconds for sockets that are idle.
     *
     * @param timeout timeout in milliseconds.
     */
    @Property
    public void setIdleTimeout(int timeout) {
        this.idleTimeout = timeout / 1000;   // convert to seconds used by Mina
    }


    /**
     * Injects the passive connection service.
     *
     * @param passiveConnectionService Passive connection service.
     */
    @Reference
    public void setPassivePortService(PassiveConnectionService passiveConnectionService) {
        this.passiveConnectionService = passiveConnectionService;
    }

}
