/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.ftp.server.handler;

import java.io.IOException;
import java.io.InputStream;

import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.ftp.api.FtpLet;
import org.fabric3.ftp.server.data.DataConnection;
import org.fabric3.ftp.server.monitor.FtpMonitor;
import org.fabric3.ftp.server.passive.PassiveConnectionService;
import org.fabric3.ftp.server.protocol.DefaultResponse;
import org.fabric3.ftp.server.protocol.FtpSession;
import org.fabric3.ftp.server.protocol.Request;
import org.fabric3.ftp.server.protocol.RequestHandler;
import org.fabric3.ftp.spi.FtpLetContainer;

/**
 * Handles the <code>STOR</code> command.
 * <p/>
 * TODO Add mechanism to register the FTPlet.
 *
 * @version $Revision$ $Date$
 */
public class StorRequestHandler implements RequestHandler {

    private PassiveConnectionService passiveConnectionService;
    private FtpLetContainer ftpLetContainer;
    private FtpMonitor ftpMonitor;

    /**
     * Services the <code>STOR</code> request. Currently only supports passive connections. This means <code>STOR</STOR> command should be preceded by
     * a <code>PASV</code> command.
     *
     * @param request Object the encapsuates the current FTP command.
     */
    public void service(Request request) {

        FtpSession session = request.getSession();
        if (!session.isAuthenticated()) {
            session.write(new DefaultResponse(530, "Access Denied"));
            return;
        }
        int passivePort = session.getPassivePort();

        if (0 == passivePort) {
            session.write(new DefaultResponse(503, "PASV must be issued first"));
            return;
        }

        String fileName = request.getArgument();
        if (null == fileName) {
            closeDataConnection(session, passivePort);
            session.write(new DefaultResponse(501, "Syntax error in parameters or arguments"));
            return;
        }

        session.write(new DefaultResponse(150, "File status okay; about to open data connection"));

        DataConnection dataConnection = session.getDataConnection();

        try {
            dataConnection.open();
        } catch (IOException ex) {
            closeDataConnection(session, passivePort);
            session.write(new DefaultResponse(425, "Can't open data connection"));
            return;
        }

        transfer(session, passivePort, dataConnection, fileName);

    }

    /**
     * Sets the monitor for logging significant events.
     *
     * @param ftpMonitor Monitor for logging significant events.
     */
    @Monitor
    public void setFtpMonitor(FtpMonitor ftpMonitor) {
        this.ftpMonitor = ftpMonitor;
    }

    /**
     * Injects the FtpLet container.
     *
     * @param ftpLetContainer Ftplet container.
     */
    @Reference
    public void setFtpLetContainer(FtpLetContainer ftpLetContainer) {
        this.ftpLetContainer = ftpLetContainer;
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

    /*
     * Transfers the file by calling the mapped FtpLet.
     */
    private void transfer(FtpSession session, int passivePort, DataConnection dataConnection, String fileName) {

        try {

            InputStream uploadData = dataConnection.getInputStream();

            FtpLet ftpLet = ftpLetContainer.getFtpLet(session.getCurrentDirectory());
            if (ftpLet == null) {
                ftpMonitor.noFtpLetRegistered(fileName);
                session.write(new DefaultResponse(426, "Data connection error"));
                return;
            }
            String type = session.getContentType();
            if (!ftpLet.onUpload(fileName, type, uploadData)) {
                ftpMonitor.uploadError(session.getUserName());
                session.write(new DefaultResponse(426, "Data connection error"));
                return;
            }
            session.write(new DefaultResponse(226, "Transfer complete"));

        } catch (Exception ex) {
            ftpMonitor.onException(ex, session.getUserName());
            session.write(new DefaultResponse(426, "Data connection error"));
        } finally {
            closeDataConnection(session, passivePort);
        }

    }

    /*
     * Closes the data connection.
     */
    private void closeDataConnection(FtpSession session, int passivePort) {
        session.closeDataConnection();
        passiveConnectionService.release(passivePort);
    }

}
