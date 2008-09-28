/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.fabric.monitor;

import org.osoa.sca.annotations.Reference;

import org.fabric3.monitor.MonitorFactory;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.SingletonObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.Wire;

/**
 * TargetWireAttacher that handles monitor resources.
 * <p/>
 * This only support optimized resources.
 *
 * @version $Rev$ $Date$
 */
public class MonitorWireAttacher implements TargetWireAttacher<MonitorWireTargetDefinition> {
    private final MonitorFactory monitorFactory;
    private final ClassLoaderRegistry classLoaderRegistry;

    public MonitorWireAttacher(@Reference MonitorFactory monitorFactory, @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.monitorFactory = monitorFactory;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, MonitorWireTargetDefinition target, Wire wire) throws WiringException {
        throw new UnsupportedOperationException();
    }

    public ObjectFactory<?> createObjectFactory(MonitorWireTargetDefinition target) throws WiringException {
        try {
            Class<?> type = classLoaderRegistry.loadClass(target.getClassLoaderId(), target.getMonitorType());
            Object monitor = monitorFactory.getMonitor(type, target.getUri());
            return new SingletonObjectFactory<Object>(monitor);
        } catch (ClassNotFoundException e) {
            throw new WireAttachException("Unable to load monitor class: " + target.getMonitorType(), target.getUri(), null, e);
        }
    }
}
