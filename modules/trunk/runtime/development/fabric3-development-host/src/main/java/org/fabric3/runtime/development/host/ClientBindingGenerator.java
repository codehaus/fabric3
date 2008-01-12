package org.fabric3.runtime.development.host;

import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.policy.Policy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * Implementation of the client binding generator.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class ClientBindingGenerator implements
        BindingGenerator<ClientWireSourceDefinition, PhysicalWireTargetDefinition, ClientBindingDefinition> {
    private GeneratorRegistry registry;

    public ClientBindingGenerator(@Reference GeneratorRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(ClientBindingDefinition.class, this);
    }

    public ClientWireSourceDefinition generateWireSource(LogicalBinding<ClientBindingDefinition> logicalBinding,
                                                         Policy policy,
                                                         GeneratorContext context,
                                                         ServiceDefinition serviceDefinition)
            throws GenerationException {
        ClientWireSourceDefinition hwsd = new ClientWireSourceDefinition();
        hwsd.setUri(logicalBinding.getBinding().getTargetUri());
        return hwsd;
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalBinding<ClientBindingDefinition> logicalBinding,
                                                           Policy policy,
                                                           GeneratorContext context,
                                                           ReferenceDefinition referenceDefinition)
            throws GenerationException {
        throw new UnsupportedOperationException();
    }
}
