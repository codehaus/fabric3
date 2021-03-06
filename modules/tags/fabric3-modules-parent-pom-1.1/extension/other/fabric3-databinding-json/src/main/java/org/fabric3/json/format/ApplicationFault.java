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
package org.fabric3.json.format;

/**
 * Wrapper for a declared fault thrown by application code. This wrapper is used to serialize fault information to a client.
 *
 * @version $Revision$ $Date$
 */
public class ApplicationFault {
    private String message;
    private String type;

    /**
     * Returns the fault message.
     *
     * @return the fault message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the fault message.
     *
     * @param message the fault message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the fault type.
     *
     * @return the fault type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the fault type.
     *
     * @param type the fault type
     */
    public void setType(String type) {
        this.type = type;
    }
}
