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

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.wire.PojoWireAttacher;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.ComponentManager;
import org.fabric3.spi.model.instance.ValueSource;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class SystemWireAttacher extends PojoWireAttacher<SystemWireSourceDefinition, SystemWireTargetDefinition> {

    private WireAttacherRegistry wireAttacherRegistry;
    private ComponentManager manager;

    public SystemWireAttacher(
            ComponentManager manager,
            WireAttacherRegistry wireAttacherRegistry,
            TransformerRegistry<PullTransformer<?, ?>> transformerRegistry
    ) {
        super(transformerRegistry);
        this.manager = manager;
        this.wireAttacherRegistry = wireAttacherRegistry;
    }

    @Init
    public void init() {
        wireAttacherRegistry.register(SystemWireSourceDefinition.class, this);
        wireAttacherRegistry.register(SystemWireTargetDefinition.class, this);
    }

    public void attachToSource(SystemWireSourceDefinition sourceDefinition,
                               PhysicalWireTargetDefinition targetDefinition,
                               Wire wire) throws WiringException {
        
        URI sourceName = UriHelper.getDefragmentedName(sourceDefinition.getUri());
        Component source = manager.getComponent(sourceName);        
        assert source instanceof SystemComponent;        
        SystemComponent<?> sourceComponent = (SystemComponent) source;
        
        URI targetName = UriHelper.getDefragmentedName(targetDefinition.getUri());        
        Component target = manager.getComponent(targetName);
        assert target instanceof AtomicComponent;
        AtomicComponent<?> targetComponent = (AtomicComponent<?>) target;
        
        URI sourceUri = sourceDefinition.getUri();
        ValueSource referenceSource = new ValueSource(ValueSource.ValueSourceType.REFERENCE, sourceUri.getFragment());
        ObjectFactory<?> factory = targetComponent.createObjectFactory();
        sourceComponent.setObjectFactory(referenceSource, factory);
        
        Object key = getKey(sourceDefinition, sourceComponent, referenceSource);

        sourceComponent.attachReferenceToTarget(referenceSource, factory, key);
        
    }

    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition,
                               SystemWireTargetDefinition targetDefinition,
                               Wire wire) throws WiringException {
        // nothing to do here as the wire will always be optimized
    }
}
