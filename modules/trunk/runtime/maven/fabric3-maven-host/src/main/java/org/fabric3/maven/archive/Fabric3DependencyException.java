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
package org.fabric3.maven.archive;

import org.fabric3.host.Fabric3RuntimeException;

/**
 * Exception thrown in case of an artifact error.
 *
 * @version $Rev$ $Date$
 */
public class Fabric3DependencyException extends Fabric3RuntimeException {
    private static final long serialVersionUID = -3993762841835195146L;

    /**
     * Initializes the cause.
     *
     * @param cause Cause of the exception.
     */
    public Fabric3DependencyException(Throwable cause) {
        super(cause);
    }

    /**
     * Initializes the message.
     *
     * @param message Message of the exception.
     */
    public Fabric3DependencyException(String message) {
        super(message);
    }


    /**
     * Initializes the message.
     *
     * @param message    Message of the exception.
     * @param identifier an identifier for the exeption.
     */
    public Fabric3DependencyException(String message, String identifier) {
        super(message, identifier);
    }
}
