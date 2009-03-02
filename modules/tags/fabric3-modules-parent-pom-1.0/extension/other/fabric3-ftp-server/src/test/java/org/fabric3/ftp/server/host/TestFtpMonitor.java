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
package org.fabric3.ftp.server.host;

import org.fabric3.api.annotation.logging.Severe;
import org.fabric3.ftp.server.monitor.FtpMonitor;

/**
 * @version $Revision$ $Date$
 */
public class TestFtpMonitor implements FtpMonitor {

    public void onCommand(Object command, String user) {
        System.err.println("Command received from user " + user + ": " + command);
    }

    public void onException(Throwable throwable, String user) {
        System.err.println("Exception " + throwable.getMessage() + " by user " + user);
        throwable.printStackTrace();
    }

    public void uploadError(String user) {
        System.err.println("Upload error: " + user);
    }

    public void noFtpLetRegistered(String resource) {
        System.err.println("No registered FTPLet:" + resource);
    }

    @Severe
    public void connectionTimedOut(String user) {
        System.err.println("Connection timeout: " + user);
    }

    public void onResponse(Object response, String user) {
        System.err.println("Response sent to user " + user + ": " + response);
    }

}
