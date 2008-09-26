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
package org.fabric3.ftp.server.monitor;

import org.fabric3.api.annotation.logging.Info;
import org.fabric3.api.annotation.logging.Severe;

/**
 * Monitor interface for logging significant events.
 *
 * @version $Revision$ $Date$
 */
public interface FtpMonitor {

    /**
     * Logged when a command is received by the FTP server.
     *
     * @param command Command that was received.
     * @param user    User that sent the command.
     */
    @Info
    void onCommand(Object command, String user);

    /**
     * Logged when a response is sent by the FTP server.
     *
     * @param response Response that was sent.
     * @param user     User that sent the command.
     */
    @Info
    void onResponse(Object response, String user);

    /**
     * Logged when an exception occurs.
     *
     * @param throwable Exception that occured.
     * @param user      User whose command caused the exception.
     */
    @Severe
    void onException(Throwable throwable, String user);

    /**
     * Logged when an upload error occurs.
     *
     * @param user User whose command caused the exception.
     */
    @Severe
    void uploadError(String user);

    /**
     * Logged when an FtpLet not found for a resource.
     *
     * @param resource the resource address.
     */
    @Severe
    void noFtpLetRegistered(String resource);

    /**
     * Logged when a connection times out.
     *
     * @param user the user.
     */
    @Severe
    void connectionTimedOut(String user);

}
