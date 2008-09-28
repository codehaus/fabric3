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
package org.fabric3.fabric.instantiator.target;

import java.net.URI;

import org.fabric3.host.domain.AssemblyFailure;
import org.fabric3.spi.model.instance.LogicalReference;

public class TargetComponentNotFound extends AssemblyFailure {
    private LogicalReference reference;
    private URI targetUri;

    public TargetComponentNotFound(LogicalReference reference, URI targetUri) {
        super(reference.getUri());
        this.reference = reference;
        this.targetUri = targetUri;
    }

    public LogicalReference getReference() {
        return reference;
    }

    public URI getTargetUri() {
        return targetUri;
    }

    public String getMessage() {
        return "Target component component for reference " + reference.getUri() + " not found: " + targetUri;
    }

}
