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
package org.fabric3.fabric.instantiator;

import org.fabric3.host.domain.AssemblyFailure;
import org.fabric3.spi.model.instance.LogicalComponent;

public class ReferenceNotFound extends AssemblyFailure {
    private String message;
    private LogicalComponent<?> component;
    private String referenceName;

    public ReferenceNotFound(String message, LogicalComponent<?> component, String referenceName) {
        super(component.getUri());
        this.message = message;
        this.component = component;
        this.referenceName = referenceName;
    }

    public LogicalComponent<?> getComponent() {
        return component;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public String getMessage() {
        return message;
    }
}
