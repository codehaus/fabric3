/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.fabric.monitor;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.SingletonObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacherRegistry;
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
@EagerInit
public class MonitorWireAttacher implements TargetWireAttacher<MonitorWireTargetDefinition> {
    private final TargetWireAttacherRegistry wireAttacherRegistry;
    private final MonitorFactory monitorFactory;
    private final ClassLoaderRegistry classLoaderRegistry;

    public MonitorWireAttacher(@Reference TargetWireAttacherRegistry wireAttacherRegistry,
                               @Reference MonitorFactory monitorFactory,
                               @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.wireAttacherRegistry = wireAttacherRegistry;
        this.monitorFactory = monitorFactory;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Init
    public void init() {
        wireAttacherRegistry.register(MonitorWireTargetDefinition.class, this);
    }

    @Destroy
    public void destroy() {
        wireAttacherRegistry.unregister(MonitorWireTargetDefinition.class, this);
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
