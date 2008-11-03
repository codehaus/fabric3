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
package org.fabric3.fabric.generator.wire;

import java.util.List;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.AttachWireCommand;
import org.fabric3.fabric.command.DetachWireCommand;
import org.fabric3.fabric.command.WireCommand;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

/**
 * Generates a command to attach component reference wires to their transports for components being deployed or a command to detach reference wires
 * for components being undeployed.
 *
 * @version $Revision$ $Date$
 */
public class ReferenceWireCommandGenerator implements CommandGenerator {

    private final PhysicalWireGenerator physicalWireGenerator;
    private final int order;

    public ReferenceWireCommandGenerator(@Reference PhysicalWireGenerator physicalWireGenerator, @Property(name = "order") int order) {
        this.physicalWireGenerator = physicalWireGenerator;
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    @SuppressWarnings("unchecked")
    public Command generate(LogicalComponent<?> component) throws GenerationException {
        if (component instanceof LogicalCompositeComponent) {
            return null;
        }
        WireCommand command;
        if (LogicalState.NEW == component.getState()) {
            command = new AttachWireCommand(order);
        } else if (LogicalState.MARKED == component.getState()) {
            command = new DetachWireCommand(order);
        } else {
            return null;
        }

        generatePhysicalWires(component, command);
        if (command.getPhysicalWireDefinitions().isEmpty()) {
            return null;
        }
        return command;
    }

    private void generatePhysicalWires(LogicalComponent<?> component, WireCommand command) throws GenerationException {

        for (LogicalReference logicalReference : component.getReferences()) {
            // FIXME the check for component state should be done individually for each binding, not on the component since bindings may be
            // dynamically added
            if (logicalReference.getBindings().isEmpty() || LogicalState.NEW != component.getState()) {
                continue;
            }

            // TODO this should be extensible and moved out
            for (LogicalBinding<?> logicalBinding : logicalReference.getBindings()) {
                PhysicalWireDefinition pwd = physicalWireGenerator.generateBoundReferenceWire(component, logicalReference, logicalBinding);
                command.addPhysicalWireDefinition(pwd);
                if (logicalReference.getDefinition().getServiceContract().getCallbackContract() != null) {
                    List<LogicalBinding<?>> callbackBindings = logicalReference.getCallbackBindings();
                    if (callbackBindings.size() != 1) {
                        String uri = logicalReference.getUri().toString();
                        throw new UnsupportedOperationException("The runtime requires exactly one callback binding to be " +
                                "specified on reference: " + uri);
                    }
                    LogicalBinding<?> callbackBinding = callbackBindings.get(0);
                    // generate the callback wire
                    PhysicalWireDefinition callbackPwd = physicalWireGenerator.generateBoundCallbackRerenceWire(logicalReference,
                                                                                                                callbackBinding,
                                                                                                                component);
                    command.addPhysicalWireDefinition(callbackPwd);
                }
            }
        }
    }

}
