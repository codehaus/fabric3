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

import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;

/**
 * Abstraction for resolving targets for references. Possible implementations include explicit targets, intent based auto-wiring, and type based
 * auto-wiring. Resolution strategies are exclusive. That is, only one strategy is used per reference.
 *
 * @version $Revision$ $Date$
 */
public interface TargetResolutionService {

    /**
     * Resolves the target for a logical reference.
     *
     * @param reference Logical reference whose target needs to be resolved.
     * @param component Composite component within which the targets are resolved.
     * @param context   the instantiation context. Recoverable errors and warnings should be reported here.
     */
    void resolve(LogicalReference reference, LogicalCompositeComponent component, InstantiationContext context);

}
