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
import org.fabric3.fabric.runtime.ComponentNames;
import org.fabric3.scdl.CompositeImplementation;
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

    public AttachWireCommand generate(LogicalComponent<?> component) throws GenerationException {
        if (component instanceof LogicalCompositeComponent) {
            return null;
        }
        return generatePhysicalWires(component);
    }

    private AttachWireCommand generatePhysicalWires(LogicalComponent<?> component) throws GenerationException {
        AttachWireCommand command = new AttachWireCommand(order);

        for (LogicalReference logicalReference : component.getReferences()) {
            if (logicalReference.getBindings().isEmpty()) {
                generateUnboundReferenceWires(logicalReference, command);
            }
        }
        if (command.getPhysicalWireDefinitions().isEmpty()) {
            return null;
        }
        return command;
    }

    private void generateUnboundReferenceWires(LogicalReference logicalReference, AttachWireCommand command) throws GenerationException {

        LogicalComponent<?> component = logicalReference.getParent();

        for (LogicalWire logicalWire : logicalReference.getWires()) {

            if (logicalWire.getState() != LogicalState.NEW) {
                continue;
            }

            URI uri = logicalWire.getTargetUri();
            String serviceName = uri.getFragment();
            LogicalComponent<?> target;
            if (uri.toString().startsWith(ComponentNames.RUNTIME_NAME)) {
                target = runtimeLCM.getComponent(uri);
            } else {
                target = applicationLCM.getComponent(uri);
            }
            assert target != null;
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
            PhysicalWireDefinition pwd = physicalWireGenerator.generateUnboundWire(component, reference, targetService, target);
            command.addPhysicalWireDefinition(pwd);

            // generate physical callback wires if the forward service is bidirectional
            if (reference.getDefinition().getServiceContract().getCallbackContract() != null) {
                pwd = physicalWireGenerator.generateUnboundCallbackWire(target, reference, component);
                command.addPhysicalWireDefinition(pwd);
            }

        }

    }

    public int getOrder() {
        return order;
    }

}