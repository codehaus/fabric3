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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.container.web.spi.WebApplicationActivationException;
import org.fabric3.container.web.spi.WebApplicationActivator;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.BuilderConfigException;
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
    private ProxyService proxyService;
    private ComponentBuilderRegistry builderRegistry;

    public WebappComponentBuilder(@Reference ProxyService proxyService,
                                  @Reference ComponentBuilderRegistry registry,
                                  @Reference WebApplicationActivator activator) {
        this.proxyService = proxyService;
        this.builderRegistry = registry;
        this.activator = activator;
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
        ServletContext context;
        Map<String, ObjectFactory<?>> attributes = Collections.emptyMap();
        URI classLoaderId = definition.getClassLoaderId();
        try {
            ClassLoader cl = activator.getWebComponentClassLoader(classLoaderId);
            // XCV FIXME! uri
            context = activator.activate("/calculator", definition.getWebArchiveUrl(), classLoaderId);
            Map<String, Class<?>> referenceTypes = loadReferenceTypes(definition.getReferenceTypes(), cl);
            return new WebappComponent(componentId, context, proxyService, groupId, attributes, referenceTypes, null);
        } catch (WebApplicationActivationException e) {
            throw new WebComponentCreationException("Error activating web archive: " + definition.getWebArchiveUrl(), e);
        }
    }

    private Map<String, Class<?>> loadReferenceTypes(Map<String, String> references, ClassLoader cl) throws BuilderException {
        try {
            Map<String, Class<?>> referenceTypes = new HashMap<String, Class<?>>(references.size());
            for (Map.Entry<String, String> entry : references.entrySet()) {
                String name = entry.getKey();
                String className = entry.getValue();
                Class<?> type = Class.forName(className, true, cl);
                referenceTypes.put(name, type);
            }
            return referenceTypes;
        } catch (ClassNotFoundException e) {
            throw new BuilderConfigException(e);
        }
    }
}
