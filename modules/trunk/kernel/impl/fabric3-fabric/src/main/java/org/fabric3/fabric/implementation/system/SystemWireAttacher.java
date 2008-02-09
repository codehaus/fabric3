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
package org.fabric3.fabric.implementation.system;

import java.net.URI;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.pojo.wire.PojoWireAttacher;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.SourceWireAttacherRegistry;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacherRegistry;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.model.instance.ValueSource;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
@Service(interfaces={SourceWireAttacher.class, TargetWireAttacher.class})
public class SystemWireAttacher extends PojoWireAttacher implements SourceWireAttacher<SystemWireSourceDefinition>, TargetWireAttacher<SystemWireTargetDefinition> {

    private final SourceWireAttacherRegistry sourceWireAttacherRegistry;
    private final TargetWireAttacherRegistry targetWireAttacherRegistry;
    private final ComponentManager manager;

    public SystemWireAttacher(@Reference ComponentManager manager,
                              @Reference SourceWireAttacherRegistry sourceWireAttacherRegistry,
                              @Reference TargetWireAttacherRegistry targetWireAttacherRegistry,
                              @Reference TransformerRegistry<PullTransformer<?, ?>> transformerRegistry,
                              @Reference ClassLoaderRegistry classLoaderRegistry
    ) {
        super(transformerRegistry, classLoaderRegistry);
        this.manager = manager;
        this.sourceWireAttacherRegistry = sourceWireAttacherRegistry;
        this.targetWireAttacherRegistry = targetWireAttacherRegistry;
    }

    @Init
    public void init() {
        sourceWireAttacherRegistry.register(SystemWireSourceDefinition.class, this);
        targetWireAttacherRegistry.register(SystemWireTargetDefinition.class, this);
    }

    @Destroy
    public void destroy() {
        sourceWireAttacherRegistry.unregister(SystemWireSourceDefinition.class, this);
        targetWireAttacherRegistry.unregister(SystemWireTargetDefinition.class, this);
    }

    public void attachToSource(SystemWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        // should never be called as the wire must be optimized
        throw new AssertionError();
    }

    public void attachObjectFactory(SystemWireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        URI sourceId = UriHelper.getDefragmentedName(source.getUri());
        SystemComponent<?> sourceComponent = (SystemComponent<?>) manager.getComponent(sourceId);
        ValueSource referenceSource = new ValueSource(ValueSource.ValueSourceType.REFERENCE, source.getUri().getFragment());

        Object key = getKey(source, sourceComponent, referenceSource);
        sourceComponent.attachReferenceToTarget(referenceSource, objectFactory, key);
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, SystemWireTargetDefinition target, Wire wire) throws WiringException {
        // should never be called as the wire must be optimized
        throw new AssertionError();
    }

    public ObjectFactory<?> createObjectFactory(SystemWireTargetDefinition target) throws WiringException {
        URI targetId = UriHelper.getDefragmentedName(target.getUri());
        SystemComponent<?> targetComponent = (SystemComponent<?>) manager.getComponent(targetId);
        return targetComponent.createObjectFactory();
    }
}
