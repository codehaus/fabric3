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
package org.fabric3.spi.policy;

import org.fabric3.host.Fabric3Exception;

/**
 * Exception thrown in case of invalid policy configuration.
 * 
 * @version $Revision$ $Date$
 */
public class PolicyResolutionException extends Fabric3Exception {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 8016179162459803135L;

    /**
     * Initializes the message and the identifier.
     * 
     * @param message Message of the exception.
     * @param identifier Contextual information.
     */
    public PolicyResolutionException(String message, Object identifier) {
        super(message, identifier.toString());
    }

    public PolicyResolutionException(String message) {
        super(message);
    }

    public PolicyResolutionException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        return super.getMessage() + ":" + getIdentifier();
    }
    
}
