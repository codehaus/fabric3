/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
import org.osoa.sca.annotations.Service;

import org.fabric3.fabric.component.InstanceFactoryProvider;
import org.fabric3.fabric.component.instancefactory.IFProviderBuilderRegistry;
import org.fabric3.fabric.implementation.pojo.PojoComponentBuilder;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.ComponentManager;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.model.instance.ValueSource;
import static org.fabric3.spi.model.instance.ValueSource.ValueSourceType.REFERENCE;
import org.fabric3.spi.model.physical.InstanceFactoryProviderDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.type.Scope;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
@Service(interfaces = {ComponentBuilder.class, WireAttacher.class})
public class SystemComponentBuilder<T>
        extends PojoComponentBuilder<T, SystemComponentDefinition, SystemComponent<T>>
        implements WireAttacher<SystemWireSourceDefinition, SystemWireTargetDefinition> {

    public SystemComponentBuilder(
            @Reference ComponentBuilderRegistry builderRegistry,
            @Reference ComponentManager manager,
            @Reference WireAttacherRegistry wireAttacherRegistry,
            @Reference ScopeRegistry scopeRegistry,
            @Reference IFProviderBuilderRegistry providerBuilders,
            @Reference ClassLoaderRegistry classLoaderRegistry) {
        super(builderRegistry, manager, wireAttacherRegistry, scopeRegistry, providerBuilders, classLoaderRegistry);
    }

    @Init
    public void init() {
        builderRegistry.register(SystemComponentDefinition.class, this);
        wireAttacherRegistry.register(SystemWireSourceDefinition.class, this);
        wireAttacherRegistry.register(SystemWireTargetDefinition.class, this);
    }

    public SystemComponent<T> build(SystemComponentDefinition definition) throws BuilderException {
        URI componentId = definition.getComponentId();
        int initLevel = definition.getInitLevel();
        URI groupId = definition.getGroupId();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(definition.getClassLoaderId());

        // get the scope container for this component
        Scope scope = definition.getScope();
        ScopeContainer<?> scopeContainer = scopeRegistry.getScopeContainer(scope);

        // create the InstanceFactoryProvider based on the definition in the model
        InstanceFactoryProviderDefinition providerDefinition = definition.getInstanceFactoryProviderDefinition();
        InstanceFactoryProvider<T> provider = providerBuilders.build(providerDefinition, classLoader);

        createPropertyFactories(definition, provider);

        return new SystemComponent<T>(componentId, provider, scopeContainer, groupId, initLevel, -1, -1);
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
        ValueSource referenceSource = new ValueSource(REFERENCE, sourceUri.getFragment());
        ObjectFactory<?> factory = targetComponent.createObjectFactory();
        sourceComponent.setObjectFactory(referenceSource, factory);
    }

    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition,
                               SystemWireTargetDefinition targetDefinition,
                               Wire wire) throws WiringException {
        // nothing to do here as the wire will always be optimized
    }
}
