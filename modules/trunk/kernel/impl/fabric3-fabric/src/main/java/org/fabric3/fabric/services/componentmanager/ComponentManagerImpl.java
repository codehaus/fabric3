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
package org.fabric3.fabric.services.componentmanager;

import java.net.URI;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.runtime.component.RegistrationException;
import org.fabric3.spi.services.management.Fabric3ManagementService;

/**
 * Default implementation of the component manager
 *
 * @version $Rev$ $Date$
 */
public class ComponentManagerImpl implements ComponentManager {
    private final Fabric3ManagementService managementService;
    private final Map<URI, Component> components;

    public ComponentManagerImpl() {
        this(null);
    }

    public ComponentManagerImpl(Fabric3ManagementService managementService) {
        this.managementService = managementService;
        components = new ConcurrentHashMap<URI, Component>();
    }

    public synchronized void register(Component component) throws RegistrationException {
        URI uri = component.getUri();

        assert uri != null;
        assert !uri.toString().endsWith("/");
        if (components.containsKey(uri)) {
            throw new DuplicateComponentException(uri.toString());
        }
        components.put(uri, component);

        if (managementService != null && component instanceof AtomicComponent) {
            // FIXME shouldn't it take the canonical name and also not distinguish atomic components?
            managementService.registerComponent(component.getUri().toString(), component);
        }

    }

    public synchronized void unregister(Component component) throws RegistrationException {
        URI uri = component.getUri();
        components.remove(uri);
    }

    public Component getComponent(URI name) {
        return components.get(name);
    }

    public List<URI> getComponentsInHierarchy(URI uri) {
        String strigified = uri.toString();
        List<URI> uris = new ArrayList<URI>();
        for (Component component : components.values()) {
            URI componentUri = component.getUri();
            if (componentUri.toString().startsWith(strigified)) {
                uris.add(componentUri);
            }
        }
        return uris;
    }
}
