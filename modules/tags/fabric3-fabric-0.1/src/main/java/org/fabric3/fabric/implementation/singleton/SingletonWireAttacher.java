package org.fabric3.fabric.implementation.singleton;

import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Wire;

/**
 * Exists as a no-op attacher for system singleton components
 *
 * @version $Rev$ $Date$
 */
public class SingletonWireAttacher<PWSD extends PhysicalWireSourceDefinition,
        PWTD extends PhysicalWireTargetDefinition> implements WireAttacher<PWSD, PWTD> {

    public void attachToSource(PWSD sourceDefinition,
                               PhysicalWireTargetDefinition targetDefinition,
                               Wire wire) throws WiringException {
        throw new UnsupportedOperationException();
    }

    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition,
                               PWTD targetDefinition,
                               Wire wire)
            throws WiringException {
    }
}
