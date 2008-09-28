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
package org.fabric3.web.introspection;

import org.fabric3.host.contribution.ValidationFailure;

/**
 * Thrown when a web.xml file is invalid.
 *
 * @version $Revision$ $Date$
 */
public class InvalidWebManifest extends ValidationFailure<Void> {
    private String message;
    private Throwable cause;

    public InvalidWebManifest(String message, Throwable cause) {
        super(null);
        this.message = message;
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }

    public String getMessage() {
        return message + ". Original cause was: \n" + cause.toString();
    }
}
