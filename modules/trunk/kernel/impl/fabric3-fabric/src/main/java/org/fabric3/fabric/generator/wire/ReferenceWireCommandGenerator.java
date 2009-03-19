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
import org.fabric3.fabric.command.ReferenceConnectionCommand;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

/**
 * Generates commands to attach component reference wires to their transports for components being deployed or commands to detach reference wires for
 * components being undeployed.
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
    public ReferenceConnectionCommand generate(LogicalComponent<?> component, boolean incremental) throws GenerationException {
        if (component instanceof LogicalCompositeComponent) {
            return null;
        }
        ReferenceConnectionCommand command = new ReferenceConnectionCommand();

        for (LogicalReference logicalReference : component.getReferences()) {
            boolean reinjection = isReinjection(logicalReference, incremental);

            for (LogicalBinding<?> logicalBinding : logicalReference.getBindings()) {
                generateCommand(logicalReference, logicalBinding, command, incremental, reinjection, false);
            }
            if (logicalReference.getDefinition().getServiceContract().getCallbackContract() != null) {
                List<LogicalBinding<?>> callbackBindings = logicalReference.getCallbackBindings();
                boolean bindings = !logicalReference.getBindings().isEmpty();
                if (bindings) {
                    if (callbackBindings.size() != 1) {
                        // if the reference is explicitly bound, it must have one callback binding
                        String uri = logicalReference.getUri().toString();
                        throw new UnsupportedOperationException("The runtime requires exactly one callback binding to be " +
                                "specified on reference: " + uri);
                    }
                    LogicalBinding<?> callbackBinding = callbackBindings.get(0);
                    generateCommand(logicalReference, callbackBinding, command, incremental, reinjection, true);
                }
            }

        }
        if (command.getAttachCommands().isEmpty() && command.getDetachCommands().isEmpty()) {
            return null;
        }
        return command;
    }

    private void generateCommand(LogicalReference logicalReference,
                                 LogicalBinding<?> logicalBinding,
                                 ReferenceConnectionCommand command,
                                 boolean incremental,
                                 boolean reinjection,
                                 boolean callback) throws GenerationException {
        LogicalComponent<?> component = logicalReference.getParent();
        if (LogicalState.MARKED == component.getState() || LogicalState.MARKED == logicalBinding.getState()) {
            PhysicalWireDefinition wireDefinition;
            if (callback) {
                wireDefinition = physicalWireGenerator.generateBoundCallbackRerenceWire(logicalReference, logicalBinding);
            } else {
                wireDefinition = physicalWireGenerator.generateBoundReferenceWire(logicalReference, logicalBinding);
            }
            DetachWireCommand wireCommand = new DetachWireCommand();
            wireCommand.setPhysicalWireDefinition(wireDefinition);
            command.add(wireCommand);

        } else if (LogicalState.NEW == logicalBinding.getState() || !incremental || reinjection) {
            PhysicalWireDefinition wireDefinition;
            if (callback) {
                wireDefinition = physicalWireGenerator.generateBoundCallbackRerenceWire(logicalReference, logicalBinding);
            } else {
                wireDefinition = physicalWireGenerator.generateBoundReferenceWire(logicalReference, logicalBinding);
            }
            AttachWireCommand wireCommand = new AttachWireCommand();
            wireCommand.setPhysicalWireDefinition(wireDefinition);
            command.add(wireCommand);
        }

    }

    private boolean isReinjection(LogicalReference logicalReference, boolean incremental) {
        Multiplicity multiplicity = logicalReference.getDefinition().getMultiplicity();
        if (incremental && multiplicity == Multiplicity.ZERO_N || multiplicity == Multiplicity.ONE_N) {
            for (LogicalBinding<?> binding : logicalReference.getBindings()) {
                if (binding.getState() == LogicalState.NEW || binding.getState() == LogicalState.MARKED) {
                    return true;
                }
            }
        }
        return false;
    }

}
