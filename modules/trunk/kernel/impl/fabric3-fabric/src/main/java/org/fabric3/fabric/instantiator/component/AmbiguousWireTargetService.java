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
package org.fabric3.fabric.instantiator.component;

import java.net.URI;

import org.fabric3.host.domain.AssemblyFailure;

public class AmbiguousWireTargetService extends AssemblyFailure {
    private URI targetUri;

    public AmbiguousWireTargetService(URI targetUri, URI compositeUri, URI contributionUri) {
        super(compositeUri, contributionUri);
        this.targetUri = targetUri;
    }

    public URI getTargetUri() {
        return targetUri;
    }

    public String getMessage() {
        return "Target component " + targetUri + "for wire in " + getComponentUri() + " has more than one service. " +
                "The service must be specified in the wire";
    }

}