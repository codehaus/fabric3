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
package org.fabric3.fabric.generator.component;

import org.osoa.sca.annotations.Property;

import org.fabric3.fabric.command.StopComponentCommand;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.RemoveCommandGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Creates a command to stop an atomic component on a runtime.
 *
 * @version $Revision$ $Date$
 */
public class StopComponentCommandGenerator implements RemoveCommandGenerator {

    private final int order;
    
    public StopComponentCommandGenerator(@Property(name = "order")int order) {
        this.order = order;
    }

        public int getOrder() {
        return order;
    }

    @SuppressWarnings("unchecked")
    public StopComponentCommand generate(LogicalComponent<?> component) throws GenerationException {
        // start a component if it is atomic and not provisioned
        if (!(component instanceof LogicalCompositeComponent) && component.isProvisioned()) {
            return new StopComponentCommand(order, component.getUri());
        }
        return null;
    }
}
