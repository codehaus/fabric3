package org.fabric3.fabric.generator.wire;

import java.util.List;
import java.net.URI;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.generator.RemoveCommandGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.fabric.command.WireDetachCommand;
import org.fabric3.fabric.command.WireAttachCommand;
import org.fabric3.scdl.ServiceContract;

/**
 * @author Copyright (c) 2008 by BEA Systems. All Rights Reserved.
 */
public class DetachWireCommandGenerator implements RemoveCommandGenerator {

    private final int order;
    private final PhysicalWireGenerator generator;

    public DetachWireCommandGenerator(@Reference PhysicalWireGenerator generator, @Property(name = "order")int order) {
        this.order = order;
        this.generator = generator; 
    }

    public int getOrder() {
        return order;
    }

    public WireDetachCommand generate(LogicalComponent<?> component) throws GenerationException {
        if (component instanceof LogicalCompositeComponent) {
            return null;
        }
        WireDetachCommand command = new WireDetachCommand(order);
        generatePhysicalWires(component, command);
        return command;
    }

    private void generatePhysicalWires(LogicalComponent<?> component, WireDetachCommand command) throws GenerationException {

        for (LogicalService service : component.getServices()) {
            List<LogicalBinding<?>> bindings = service.getBindings();
            if (bindings.isEmpty()) {
                continue;
            }
                        
            ServiceContract<?> callbackContract = service.getDefinition().getServiceContract().getCallbackContract();
            LogicalBinding<?> callbackBinding = null;
            URI callbackUri = null;
            if (callbackContract != null) {
                List<LogicalBinding<?>> callbackBindings = service.getCallbackBindings();
                if (callbackBindings.size() != 1) {
                    String uri = service.getUri().toString();
                    throw new UnsupportedOperationException("The runtime requires exactly one callback binding to be specified on service: " + uri);
                }
                callbackBinding = callbackBindings.get(0);
                // xcv FIXME should be on the logical binding
                callbackUri = callbackBinding.getBinding().getTargetUri();
            }

            for (LogicalBinding<?> binding : bindings) {

                //if (!binding.isProvisioned()) {
                    PhysicalWireDefinition pwd = generator.generateBoundServiceWire(service, binding, component, callbackUri);
                    command.addPhysicalWireDefinition(pwd);
                    binding.setProvisioned(true);
                //}
            }
            // generate the callback command set
            if (callbackBinding != null && !callbackBinding.isProvisioned()) {
                PhysicalWireDefinition callbackPwd = generator.generateBoundCallbackServiceWire(component, service, callbackBinding);
                command.addPhysicalWireDefinition(callbackPwd);
                callbackBinding.setProvisioned(true);
            }
        }

    }
}


