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
package org.fabric3.fabric.runtime;

import org.fabric3.host.runtime.InitializationException;

/**
 * @version $Rev$ $Date$
 */
public class ExtensionInitializationException extends InitializationException {
    private static final long serialVersionUID = 7390375093657355129L;

    public ExtensionInitializationException(String message) {
        super(message);
    }

    public ExtensionInitializationException(String message, String identifier, Throwable cause) {
        super(message, identifier, cause);
    }

    public ExtensionInitializationException(String message, String identifier) {
        super(message, identifier);
    }

    public ExtensionInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getMessage() {
        if (getIdentifier() != null) {
            return super.getMessage() + ": " + getIdentifier();
        } else {
            return super.getMessage();
        }
    }
}
