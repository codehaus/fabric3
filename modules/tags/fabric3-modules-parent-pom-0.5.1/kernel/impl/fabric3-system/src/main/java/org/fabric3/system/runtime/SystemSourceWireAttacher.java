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
package org.fabric3.system.runtime;

import java.net.URI;

import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.wire.PojoSourceWireAttacher;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;
import org.fabric3.system.provision.SystemWireSourceDefinition;
import org.fabric3.transform.PullTransformer;
import org.fabric3.transform.TransformerRegistry;

/**
 * @version $Rev$ $Date$
 */
public class SystemSourceWireAttacher extends PojoSourceWireAttacher implements SourceWireAttacher<SystemWireSourceDefinition> {

    private final ComponentManager manager;

    public SystemSourceWireAttacher(@Reference ComponentManager manager,
                                    @Reference TransformerRegistry<PullTransformer<?, ?>> transformerRegistry,
                                    @Reference ClassLoaderRegistry classLoaderRegistry
    ) {
        super(transformerRegistry, classLoaderRegistry);
        this.manager = manager;
    }

    public void attachToSource(SystemWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        // should never be called as the wire must be optimized
        throw new AssertionError();
    }

    public void detachFromSource(SystemWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        throw new AssertionError();
    }

    public void attachObjectFactory(SystemWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition target)
            throws WiringException {
        URI sourceId = UriHelper.getDefragmentedName(source.getUri());
        SystemComponent<?> sourceComponent = (SystemComponent<?>) manager.getComponent(sourceId);
        InjectableAttribute referenceSource = source.getValueSource();
        Object key = getKey(source, sourceComponent, target, referenceSource);
        sourceComponent.attachReferenceToTarget(referenceSource, objectFactory, key);
    }
}