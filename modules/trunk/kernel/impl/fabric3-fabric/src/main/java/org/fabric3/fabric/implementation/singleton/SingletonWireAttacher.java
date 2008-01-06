package org.fabric3.fabric.implementation.singleton;

import java.net.URI;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;

/**
 * Exists as a no-op attacher for system singleton components
 *
 * @version $Rev$ $Date$
 */
public class SingletonWireAttacher implements TargetWireAttacher<SingletonWireTargetDefinition> {
    private final ComponentManager manager;

    public SingletonWireAttacher(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, SingletonWireTargetDefinition target, Wire wire)
            throws WiringException {
    }

    public ObjectFactory<?> createObjectFactory(SingletonWireTargetDefinition target) throws WiringException {
        URI targetId = UriHelper.getDefragmentedName(target.getUri());
        SingletonComponent<?> targetComponent = (SingletonComponent<?>) manager.getComponent(targetId);
        return targetComponent.createObjectFactory();
    }
}
