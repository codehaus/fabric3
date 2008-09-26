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

import org.osoa.sca.annotations.Reference;

import org.fabric3.ftp.server.protocol.DefaultResponse;
import org.fabric3.ftp.server.protocol.FtpSession;
import org.fabric3.ftp.server.protocol.Request;
import org.fabric3.ftp.server.protocol.RequestHandler;
import org.fabric3.ftp.spi.FtpLetContainer;

/**
 * Handles the <code>CWD</code> command.
 * <p/>
 *
 * @version $Revision$ $Date$
 */
public class CwdRequestHandler implements RequestHandler {
    private FtpLetContainer container;

    @Reference
    public void setContainer(FtpLetContainer container) {
        this.container = container;
    }

    /**
     * Services the <code>CWD</code> request.
     *
     * @param request Object the encapsuates the current FTP command.
     */
    public void service(Request request) {
        FtpSession session = request.getSession();
        if (!session.isAuthenticated()) {
            session.write(new DefaultResponse(530, "Access Denied"));
            return;
        }
        String directory = request.getArgument();
        if (!container.isRegistered(directory)) {
            session.write(new DefaultResponse(550, directory + ": No such file or directory"));
            return;
        }
        session.setCurrentDirectory(directory);
        session.write(new DefaultResponse(250, "CWD command successful"));

    }

}