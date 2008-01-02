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

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.SourceWireAttacherRegistry;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.ProxyService;
import org.fabric3.spi.wire.Wire;

/**
 */
@EagerInit
@Service(interfaces = {ComponentBuilder.class, SourceWireAttacher.class})
public class WebappComponentBuilder
        implements ComponentBuilder<WebappComponentDefinition, WebappComponent>,
        SourceWireAttacher<WebappWireSourceDefinition> {

    private final SourceWireAttacherRegistry sourceWireAttacherRegistry;
    private ProxyService proxyService;
    private ComponentBuilderRegistry builderRegistry;
    private ComponentManager manager;

    public WebappComponentBuilder(@Reference ProxyService proxyService,
                                  @Reference ComponentManager manager,
                                  @Reference ComponentBuilderRegistry builderRegistry,
                                  @Reference SourceWireAttacherRegistry sourceWireAttacherRegistry) {
        this.proxyService = proxyService;
        this.manager = manager;
        this.builderRegistry = builderRegistry;
        this.sourceWireAttacherRegistry = sourceWireAttacherRegistry;
    }


    @Init
    public void init() {
        builderRegistry.register(WebappComponentDefinition.class, this);
        sourceWireAttacherRegistry.register(WebappWireSourceDefinition.class, this);
    }

    @Destroy
    public void destroy() {
        sourceWireAttacherRegistry.unregister(WebappWireSourceDefinition.class, this);
    }

    public WebappComponent build(WebappComponentDefinition definition) throws BuilderException {
        URI componentId = definition.getComponentId();
        URI groupId = definition.getGroupId();
        Map<String, ObjectFactory<?>> attributes = definition.getAttributes();
        Map<String, Class<?>> referenceTypes = definition.getReferenceTypes();
        return new WebappComponent(componentId, proxyService, groupId, attributes, referenceTypes, null);
    }

    @SuppressWarnings({"unchecked"})
    public void attachToSource(WebappWireSourceDefinition sourceDefinition,
                               PhysicalWireTargetDefinition targetDefinition,
                               Wire wire) {
        URI sourceUri = UriHelper.getDefragmentedName(sourceDefinition.getUri());
        String referenceName = sourceDefinition.getUri().getFragment();
        Component source = manager.getComponent(sourceUri);
        assert source instanceof WebappComponent;
        ((WebappComponent) source).attachWire(referenceName, wire);
    }
}
