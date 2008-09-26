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
import java.io.OutputStream;

import org.osoa.sca.annotations.Reference;

import org.fabric3.ftp.server.data.DataConnection;
import org.fabric3.ftp.server.passive.PassiveConnectionService;
import org.fabric3.ftp.server.protocol.DefaultResponse;
import org.fabric3.ftp.server.protocol.FtpSession;
import org.fabric3.ftp.server.protocol.Request;
import org.fabric3.ftp.server.protocol.RequestHandler;

/**
 * Handles the <code>LIST</code> command.
 * <p/>
 *
 * @version $Revision$ $Date$
 */
public class ListRequestHandler implements RequestHandler {
    private static final byte[] BYTES = "".getBytes();
    private PassiveConnectionService passiveConnectionService;

    /**
     * Injects the passive connection service.
     *
     * @param passiveConnectionService Passive connection service.
     */
    @Reference
    public void setPassivePortService(PassiveConnectionService passiveConnectionService) {
        this.passiveConnectionService = passiveConnectionService;
    }

    public void service(Request request) {

        FtpSession session = request.getSession();
        int passivePort = session.getPassivePort();

        if (0 == passivePort) {
            session.write(new DefaultResponse(503, "PASV must be issued first"));
            return;
        }

        session.write(new DefaultResponse(150, "File status okay; about to open data connection"));


        try {
            DataConnection dataConnection = session.getDataConnection();
            dataConnection.open();
            OutputStream stream = dataConnection.getOutputStream();
            stream.write(BYTES);
            stream.close();
            session.write(new DefaultResponse(226, "Transfer complete"));
        } catch (IOException ex) {
            closeDataConnection(session, passivePort);
            session.write(new DefaultResponse(425, "Can't open data connection"));
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