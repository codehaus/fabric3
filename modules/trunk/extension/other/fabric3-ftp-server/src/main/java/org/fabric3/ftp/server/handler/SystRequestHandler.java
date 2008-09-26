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

import org.fabric3.ftp.server.protocol.DefaultResponse;
import org.fabric3.ftp.server.protocol.FtpSession;
import org.fabric3.ftp.server.protocol.Request;
import org.fabric3.ftp.server.protocol.RequestHandler;

/**
 * Handles the <code>SYST</code> command.
 *
 * @version $Revision$ $Date$
 */
public class SystRequestHandler implements RequestHandler {

    /**
     * Handles a <code>SYST</code> command from the remote client.
     *
     * @param request Object the encapsuates the current FTP command.
     */
    public void service(Request request) {
        FtpSession session = request.getSession();
        session.write(new DefaultResponse(215, "UNIX Type: L8"));
    }

}