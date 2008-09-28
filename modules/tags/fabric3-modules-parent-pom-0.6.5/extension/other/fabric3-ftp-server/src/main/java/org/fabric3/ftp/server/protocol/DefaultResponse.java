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
 * Default implementation of the FTP response.
 * 
 * @version $Revision$ $Date$
 */
public class DefaultResponse implements Response {
    
    private int code;
    private String message;
    
    /**
     * Initializes the code and the message.
     * 
     * @param code FTP response code.
     * @param message FTP response message.
     */
    public DefaultResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * String representation of the code and the message.
     * 
     * @return Concatenated code and message with a newline at the end.
     */
    @Override
    public String toString() {
        
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(code);
        if (null != message) {
            stringBuilder.append(" ");
            stringBuilder.append(message);
        }
        stringBuilder.append(".\r\n");

        return stringBuilder.toString();
        
    }

}
