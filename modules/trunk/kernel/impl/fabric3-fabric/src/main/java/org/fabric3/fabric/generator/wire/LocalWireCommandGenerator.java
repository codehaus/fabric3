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

import java.net.URI;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.AttachWireCommand;
import org.fabric3.fabric.command.DetachWireCommand;
import org.fabric3.fabric.command.ReferenceConnectionCommand;
import org.fabric3.host.Names;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.services.lcm.LogicalComponentManager;
import org.fabric3.spi.util.UriHelper;

/**
 * Generate commands to attach local wires between components.
 *
 * @version $Revision$ $Date$
 */
public class LocalWireCommandGenerator implements CommandGenerator {

    private PhysicalWireGenerator physicalWireGenerator;
    private LogicalComponentManager applicationLCM;
    private LogicalComponentManager runtimeLCM;
    private int order;

    /**
     * Constructor used during bootstrap.
     *
     * @param physicalWireGenerator the bootstrap physical wire generator
     * @param runtimeLCM            the bootstrap LogicalComponentManager
     * @param order                 the order value for commands generated
     */
    public LocalWireCommandGenerator(PhysicalWireGenerator physicalWireGenerator, LogicalComponentManager runtimeLCM, int order) {
        this.physicalWireGenerator = physicalWireGenerator;
        this.runtimeLCM = runtimeLCM;
        this.order = order;
    }

    /**
     * Constructor used for instantiation after bootstrap. After bootstrap on a controller instance, two domains will be active: the runtime domain
     * containing system components and the application domain containing end-user components. On runtime nodes, the application domain may not be
     * active, in which case a null value may be injected.
     *
     * @param physicalWireGenerator the bootstrap physical wire generator
     * @param runtimeLCM            the LogicalComponentManager associated with the runtime domain
     * @param applicationLCM        the LogicalComponentManager associated with the application domain
     * @param order                 the order value for commands generated
     */
    @Constructor
    public LocalWireCommandGenerator(@Reference PhysicalWireGenerator physicalWireGenerator,
                                     @Reference(name = "runtimeLCM") LogicalComponentManager runtimeLCM,
                                     @Reference(name = "applicationLCM") LogicalComponentManager applicationLCM,
                                     @Property(name = "order") int order) {
        this.physicalWireGenerator = physicalWireGenerator;
        this.runtimeLCM = runtimeLCM;
        this.applicationLCM = applicationLCM;
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public ReferenceConnectionCommand generate(LogicalComponent<?> component, boolean incremental) throws GenerationException {
        if (component instanceof LogicalCompositeComponent || LogicalState.MARKED == component.getState()) {
            return null;
        }
        ReferenceConnectionCommand command = new ReferenceConnectionCommand();

        for (LogicalReference logicalReference : component.getReferences()) {
            if (logicalReference.getBindings().isEmpty()) {
                generateUnboundReferenceWires(logicalReference, command, incremental);
            }
        }
        if (command.getAttachCommands().isEmpty() && command.getDetachCommands().isEmpty()) {
            return null;
        }
        return command;
    }

    private void generateUnboundReferenceWires(LogicalReference logicalReference, ReferenceConnectionCommand command, boolean incremental)
            throws GenerationException {

        // if the reference is a multiplicity and one of the wires has changed, all of the wires need to be regenerated for reinjection
        boolean reinjection = isReinjection(logicalReference, incremental);

        for (LogicalWire logicalWire : logicalReference.getWires()) {
            LogicalComponent<?> component = logicalReference.getParent();

            URI uri = logicalWire.getTargetUri();
            LogicalComponent<?> target = findTarget(logicalWire);
            if (!reinjection && (logicalWire.getState() == LogicalState.PROVISIONED && target.getState() != LogicalState.MARKED && incremental)) {
                continue;
            }
            String serviceName = uri.getFragment();
            LogicalService targetService = target.getService(serviceName);

            assert targetService != null;
            while (CompositeImplementation.class.isInstance(target.getDefinition().getImplementation())) {
                LogicalCompositeComponent composite = (LogicalCompositeComponent) target;
                URI promoteUri = targetService.getPromotedUri();
                URI promotedComponent = UriHelper.getDefragmentedName(promoteUri);
                target = composite.getComponent(promotedComponent);
                targetService = target.getService(promoteUri.getFragment());
            }

            LogicalReference reference = logicalWire.getSource();
            boolean attach = true;
            if (target.getState() == LogicalState.MARKED || logicalWire.getState() == LogicalState.MARKED) {
                PhysicalWireDefinition pwd = physicalWireGenerator.generateUnboundWire(component, reference, targetService, target);
                attach = false;
                DetachWireCommand detachCommand = new DetachWireCommand();
                detachCommand.setPhysicalWireDefinition(pwd);
                command.add(detachCommand);
            } else if (reinjection || !incremental || logicalWire.getState() == LogicalState.NEW || target.getState() == LogicalState.NEW) {
                PhysicalWireDefinition pwd = physicalWireGenerator.generateUnboundWire(component, reference, targetService, target);
                AttachWireCommand attachCommand = new AttachWireCommand();
                attachCommand.setPhysicalWireDefinition(pwd);
                command.add(attachCommand);
            }
            // generate physical callback wires if the forward service is bidirectional
            if (reference.getDefinition().getServiceContract().getCallbackContract() != null) {
                if (attach) {
                    PhysicalWireDefinition pwd = physicalWireGenerator.generateUnboundCallbackWire(target, reference, component);
                    AttachWireCommand attachCommand = new AttachWireCommand();
                    attachCommand.setPhysicalWireDefinition(pwd);
                    command.add(attachCommand);
                } else {
                    PhysicalWireDefinition pwd = physicalWireGenerator.generateUnboundCallbackWire(target, reference, component);
                    DetachWireCommand detachCommand = new DetachWireCommand();
                    detachCommand.setPhysicalWireDefinition(pwd);
                    command.add(detachCommand);
                }
            }

        }

    }

    private boolean isReinjection(LogicalReference logicalReference, boolean incremental) {
        Multiplicity multiplicity = logicalReference.getDefinition().getMultiplicity();
        if (incremental && multiplicity == Multiplicity.ZERO_N || multiplicity == Multiplicity.ONE_N) {
            for (LogicalWire wire : logicalReference.getWires()) {
                LogicalComponent<?> target = findTarget(wire);
                // check the source and target sides since a target may have been added or removed
                if (wire.getState() == LogicalState.NEW
                        || wire.getState() == LogicalState.MARKED
                        || target.getState() == LogicalState.NEW
                        || target.getState() == LogicalState.MARKED) {
                    return true;
                }
            }
        }
        return false;
    }

    private LogicalComponent<?> findTarget(LogicalWire logicalWire) {
        URI uri = UriHelper.getDefragmentedName(logicalWire.getTargetUri());
        if (uri.toString().startsWith(Names.RUNTIME_NAME)) {
            return runtimeLCM.getComponent(uri);
        } else {
            return applicationLCM.getComponent(uri);
        }
    }

}