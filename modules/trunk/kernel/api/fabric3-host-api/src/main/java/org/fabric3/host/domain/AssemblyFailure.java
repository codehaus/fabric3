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
package org.fabric3.host.domain;

import java.net.URI;

/**
 * Base class for recoverable errors updating the domain assembly encountered during a deployment.
 *
 * @version $Revision$ $Date$
 */
public abstract class AssemblyFailure {
    private URI componentUri;

    /**
     * Constructor.
     *
     * @param componentUri the URI of the component associated with the failure.
     */
    public AssemblyFailure(URI componentUri) {
        this.componentUri = componentUri;
    }

    /**
     * Returns the URI of the component associated with the failure.
     *
     * @return the URI of the component associated with the failure.
     */
    public URI getComponentUri() {
        return componentUri;
    }

    /**
     * Returns the error message.
     *
     * @return the error message.
     */
    public String getMessage() {
        return "";
    }
}
