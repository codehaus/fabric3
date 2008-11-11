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
package org.fabric3.fabric.services.contribution;

import org.fabric3.host.contribution.InstallException;

/**
 * Exception thrown to indicate that a Content-Type is not supported by this SCA Domain. The Content-Type value supplied will be returned as the
 * message text for this exception.
 *
 * @version $Rev$ $Date$
 */
public class UnsupportedContentTypeException extends InstallException {
    private static final long serialVersionUID = -1831797280021355672L;

    /**
     * Constructor specifying the Content-Type value that is not supported and an identifier to use with this exception (typically the resource being
     * processed).
     *
     * @param message    the error message
     * @param identifier an identifier for this exception
     */
    public UnsupportedContentTypeException(String message, String identifier) {
        super(message, identifier);
    }
}
