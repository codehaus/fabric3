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
package org.fabric3.ftp.server.protocol;

/**
 * Default implementation of the FTP request.
 * 
 * @version $Revision$ $Date$
 */
public class DefaultRequest implements Request {
    
    private String command;
    private String argument;
    private FtpSession session;
    
    /**
     * Initializes the message, argument and the session.
     * 
     * @param message FTP command and argument.
     * @param session FTP session.
     */
    public DefaultRequest(String message, FtpSession session) {
        
        message = message.trim();
        int index = message.indexOf(" ");
        if (index != -1) {
            command = message.substring(0, index).toUpperCase();
            argument = message.substring(index + 1);
        } else {
            command = message.trim();
        }
        
        this.session = session;
        
    }
    
    /**
     * Gets the command for the FTP request.
     * 
     * @return FTP command.
     */
    public String getCommand() {
        return command;
    }

    /**
     * Gets the argument for the FTP request.
     * 
     * @return FTP command argument.
     */
    public String getArgument() {
        return argument;
    }
    
    /**
     * Gets the session associated with the FTP request.
     * 
     * @return FTP session.
     */
    public FtpSession getSession() {
        return session;
    }

}
