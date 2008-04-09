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
package org.fabric3.web.runtime;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.container.web.spi.WebApplicationActivator;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.wire.ProxyService;
import org.fabric3.web.provision.WebappComponentDefinition;

/**
 * Instantiates a web component on a runtime node.
 */
@EagerInit
public class WebappComponentBuilder implements ComponentBuilder<WebappComponentDefinition, WebappComponent> {
    private WebApplicationActivator activator;
    private InjectorFactory injectorFactory;
    private ProxyService proxyService;
    private ComponentBuilderRegistry builderRegistry;

    public WebappComponentBuilder(@Reference ProxyService proxyService,
                                  @Reference ComponentBuilderRegistry registry,
                                  @Reference WebApplicationActivator activator,
                                  @Reference InjectorFactory injectorFactory) {
        this.proxyService = proxyService;
        this.builderRegistry = registry;
        this.activator = activator;
        this.injectorFactory = injectorFactory;
    }

    @Init
    public void init() {
        builderRegistry.register(WebappComponentDefinition.class, this);
    }

    @Destroy
    public void destroy() {
    }

    public WebappComponent build(WebappComponentDefinition definition) throws BuilderException {
        URI componentId = definition.getComponentId();
        URI groupId = definition.getGroupId();
        // TODO fix properties
        Map<String, ObjectFactory<?>> propertyFactories = Collections.emptyMap();
        URI classLoaderId = definition.getClassLoaderId();
        Map<String, Map<String, InjectionSite>> injectorMappings = definition.getInjectionSiteMappings();
        ClassLoader cl = activator.getWebComponentClassLoader(classLoaderId);
        URL archiveUrl = definition.getWebArchiveUrl();
        return new WebappComponent(componentId,
                                   classLoaderId,
                                   groupId,
                                   archiveUrl,
                                   cl,
                                   injectorFactory,
                                   activator,
                                   proxyService,
                                   propertyFactories,
                                   injectorMappings);
    }

}
