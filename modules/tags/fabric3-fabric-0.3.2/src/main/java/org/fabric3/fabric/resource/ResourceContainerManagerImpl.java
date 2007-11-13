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
package org.fabric3.fabric.resource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.spi.resource.RegistrationException;
import org.fabric3.spi.resource.ResourceContainer;
import org.fabric3.spi.resource.ResourceContainerManager;

/**
 * Default ResourceContainerManager implementation
 *
 * @version $Rev$ $Date$
 */
public class ResourceContainerManagerImpl implements ResourceContainerManager {
    private Map<String, ResourceContainer> containers = new ConcurrentHashMap<String, ResourceContainer>();

    public ResourceContainer getResourceContainer(String id) {
        return containers.get(id);
    }

    public void registerResourceContainer(ResourceContainer container) throws RegistrationException {
        String id = container.getId();
        if (containers.containsKey(id)) {
            throw new DuplicateResourceContainerException("Duplicate resource container", id);
        }
        containers.put(id, container);
    }

    public void deregisterResourceContainer(String id) throws RegistrationException {
        containers.remove(id);
    }
}
