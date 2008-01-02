package org.fabric3.fabric.implementation.singleton;

import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.wire.Wire;

/**
 * Exists as a no-op attacher for system singleton components
 *
 * @version $Rev$ $Date$
 */
public class SingletonWireAttacher implements TargetWireAttacher<SingletonWireTargetDefinition> {

    public void attachToTarget(PhysicalWireSourceDefinition source, SingletonWireTargetDefinition target, Wire wire)
            throws WiringException {
    }
}
