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
package org.fabric3.system.singleton;

import java.net.URI;

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;

/**
 * Exists as a no-op attacher for system singleton components
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class SingletonTargetWireAttacher implements TargetWireAttacher<SingletonWireTargetDefinition> {
    private final ComponentManager manager;

    public SingletonTargetWireAttacher(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, SingletonWireTargetDefinition target, Wire wire)
            throws WiringException {
    }

    public void detachFromTarget(PhysicalWireSourceDefinition source, SingletonWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }

    public ObjectFactory<?> createObjectFactory(SingletonWireTargetDefinition target) throws WiringException {
        URI targetId = UriHelper.getDefragmentedName(target.getUri());
        SingletonComponent<?> targetComponent = (SingletonComponent<?>) manager.getComponent(targetId);
        return targetComponent.createObjectFactory();
    }
}
