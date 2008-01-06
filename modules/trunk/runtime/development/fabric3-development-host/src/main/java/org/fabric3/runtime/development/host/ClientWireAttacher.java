package org.fabric3.runtime.development.host;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.SourceWireAttacherRegistry;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Wire;
import org.fabric3.spi.ObjectFactory;

/**
 * Wire Attacher for the client binding
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ClientWireAttacher implements SourceWireAttacher<ClientWireSourceDefinition> {
    private final SourceWireAttacherRegistry sourceWireAttacherRegistry;
    private final ClientWireCache wireCache;

    public ClientWireAttacher(@Reference SourceWireAttacherRegistry sourceWireAttacherRegistry,
                              @Reference ClientWireCache wireCache) {
        this.sourceWireAttacherRegistry = sourceWireAttacherRegistry;
        this.wireCache = wireCache;
    }

    @Init
    public void init() {
        sourceWireAttacherRegistry.register(ClientWireSourceDefinition.class, this);
    }

    public void attachToSource(ClientWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire)
            throws WiringException {
        wireCache.putWire(target.getUri(), wire);
    }

    public void attachObjectFactory(ClientWireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        throw new AssertionError();
    }
}
