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
package org.fabric3.runtime.webapp.implementation.webapp;

import java.net.URI;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.ComponentManager;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.ProxyService;
import org.fabric3.spi.wire.Wire;

/**
 */
@EagerInit
@Service(interfaces = {ComponentBuilder.class, WireAttacher.class})
public class WebAppComponentBuilder
        implements ComponentBuilder<WebappPhysicalComponentDefinition, WebappComponent>,
        WireAttacher<WebAppPhysicalWireSourceDefinition, PhysicalWireTargetDefinition> {

    private ProxyService proxyService;
    private ComponentBuilderRegistry builderRegistry;
    private ComponentManager manager;
    private WireAttacherRegistry wireAttacherRegistry;

    public WebAppComponentBuilder(@Reference ProxyService proxyService,
                                  @Reference ComponentManager manager,
                                  @Reference ComponentBuilderRegistry builderRegistry,
                                  @Reference WireAttacherRegistry wireAttacherRegistry) {
        this.proxyService = proxyService;
        this.manager = manager;
        this.builderRegistry = builderRegistry;
        this.wireAttacherRegistry = wireAttacherRegistry;
    }


    @Init
    public void init() {
        builderRegistry.register(WebappPhysicalComponentDefinition.class, this);
        wireAttacherRegistry.register(WebAppPhysicalWireSourceDefinition.class, this);
    }

    public WebappComponent build(WebappPhysicalComponentDefinition definition) throws BuilderException {
        URI componentId = definition.getComponentId();
        URI groupId = definition.getGroupId();
        Map<String, ObjectFactory<?>> attributes = definition.getAttributes();
        Map<String, Class<?>> referenceTypes = definition.getReferenceTypes();
        return new WebappComponent(componentId, proxyService, groupId, attributes, referenceTypes);
    }

    @SuppressWarnings({"unchecked"})
    public void attachToSource(WebAppPhysicalWireSourceDefinition sourceDefinition,
                               PhysicalWireTargetDefinition targetDefinition,
                               Wire wire) {
        URI sourceName = UriHelper.getDefragmentedName(sourceDefinition.getUri());
        Component source = manager.getComponent(sourceName);
        assert source instanceof WebappComponent;
        ((WebappComponent) source).attachWire(wire);
    }

    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition,
                               PhysicalWireTargetDefinition target,
                               Wire wire) throws WireAttachException {
        throw new UnsupportedOperationException();
    }

}
