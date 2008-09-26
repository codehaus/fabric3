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
import org.fabric3.ftp.server.security.User;
import org.fabric3.ftp.server.security.UserManager;

/**
 * Handles the <code>PASS</code> command.
 *
 * @version $Revision$ $Date$
 */
public class PassRequestHandler implements RequestHandler {

    private UserManager userManager;

    /**
     * Uses the registered user manager to authenticate the <code>PASS</code> command.
     *
     * @param request Object the encapsuates the current FTP command.
     */
    public void service(Request request) {

        FtpSession session = request.getSession();
        User user = session.getUser();

        if (user == null) {
            session.write(new DefaultResponse(503, "Login with USER first"));
            return;
        }

        String userName = user.getName();
        String password = request.getArgument();

        if (password == null) {
            session.write(new DefaultResponse(501, "Syntax error in parameters or arguments"));
        }

        if (userManager.login(userName, password)) {
            session.setAuthenticated();
            session.write(new DefaultResponse(230, "User logged in, proceed"));
        } else {
            session.write(new DefaultResponse(530, "Authentication failed"));
        }

    }

    /**
     * Injects the user manager.
     *
     * @param userManager Injects the user manager.
     */
    @Reference
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

}
