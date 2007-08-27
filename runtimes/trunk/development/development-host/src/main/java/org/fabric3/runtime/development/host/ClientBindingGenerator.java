package org.fabric3.runtime.development.host;

import java.util.Set;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

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
                                                         GeneratorContext generatorContext,
                                                         ServiceDefinition serviceDefinition)
            throws GenerationException {
        ClientWireSourceDefinition hwsd = new ClientWireSourceDefinition();
        hwsd.setUri(logicalBinding.getBinding().getTargetUri());
        return hwsd;
    }

    public ClientWireSourceDefinition generateWireSource(LogicalBinding<ClientBindingDefinition> logicalBinding,
                                                         Set<QName> intentsToBeProvided,
                                                         GeneratorContext context,
                                                         ServiceDefinition serviceDefinition)
            throws GenerationException {
        ClientWireSourceDefinition hwsd = new ClientWireSourceDefinition();
        hwsd.setUri(logicalBinding.getBinding().getTargetUri());
        return hwsd;
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalBinding<ClientBindingDefinition> logicalBinding,
                                                           Set<QName> intentsToBeProvided,
                                                           GeneratorContext context,
                                                           ReferenceDefinition referenceDefinition)
            throws GenerationException {
        throw new UnsupportedOperationException();
    }
}
