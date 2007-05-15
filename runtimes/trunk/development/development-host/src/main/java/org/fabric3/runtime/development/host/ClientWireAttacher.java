package org.fabric3.runtime.development.host;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class ClientWireAttacher implements WireAttacher<ClientWireSourceDefinition, PhysicalWireTargetDefinition> {
    private ClientWireCache wireCache;
    private WireAttacherRegistry registry;

    public ClientWireAttacher(@Reference WireAttacherRegistry registry,
                              @Reference ClientWireCache wireCache) {
        this.registry = registry;
        this.wireCache = wireCache;
    }

    @Init
    public void init() {
        registry.register(ClientWireSourceDefinition.class, this);
    }

    public void attachToSource(ClientWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire)
            throws WiringException {
        wireCache.putWire(target.getUri(), wire);
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire)
            throws WiringException {
        throw new UnsupportedOperationException();

    }
}
