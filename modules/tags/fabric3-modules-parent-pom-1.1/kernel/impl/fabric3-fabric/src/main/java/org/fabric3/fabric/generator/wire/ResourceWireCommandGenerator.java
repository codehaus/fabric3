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
package org.fabric3.fabric.generator.wire;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.AttachWireCommand;
import org.fabric3.fabric.command.ConnectionCommand;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

/**
 * Generate a command to attach local wires from a component to its resources.
 *
 * @version $Revision$ $Date$
 */
public class ResourceWireCommandGenerator implements CommandGenerator {

    private final PhysicalWireGenerator physicalWireGenerator;
    private final int order;

    public ResourceWireCommandGenerator(@Reference PhysicalWireGenerator physicalWireGenerator, @Property(name = "order") int order) {
        this.physicalWireGenerator = physicalWireGenerator;
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public ConnectionCommand generate(LogicalComponent<?> component, boolean incremental) throws GenerationException {
        if (component instanceof LogicalCompositeComponent
                || component.getResources().isEmpty()
                || (component.getState() != LogicalState.NEW && incremental)) {
            return null;
        }
        ConnectionCommand command = new ConnectionCommand();
        for (LogicalResource<?> resource : component.getResources()) {
            AttachWireCommand attachWireCommand = new AttachWireCommand();
            PhysicalWireDefinition pwd = physicalWireGenerator.generateResourceWire(resource);
            attachWireCommand.setPhysicalWireDefinition(pwd);
            command.add(attachWireCommand);
        }
        return command;
    }

}