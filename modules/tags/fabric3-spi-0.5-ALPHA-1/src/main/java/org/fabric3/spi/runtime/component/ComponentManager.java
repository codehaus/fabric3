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
package org.fabric3.spi.runtime.component;

import java.net.URI;
import java.util.List;

import org.fabric3.spi.component.Component;

/**
 * Responsible for tracking and managing the component tree for a runtime instance. The tree corresponds to components
 * deployed to the current runtime and hence may be sparse in comparison to the assembly component hierarchy for the SCA
 * domain as parents and children may be distributed to different runtimes.
 *
 * @version $Rev$ $Date$
 */
public interface ComponentManager {

    /**
     * Registers a component which will be managed by the runtime
     *
     * @param component the component
     * @throws RegistrationException when an error ocurrs registering the component
     */
    void register(Component component) throws RegistrationException;

    /**
     * Deregisters a component
     *
     * @param component the component to deregister
     * @throws RegistrationException when an error ocurrs registering the component
     */
    void unregister(Component component) throws RegistrationException;

    /**
     * Returns the component with the given URI
     *
     * @param uri the component URI
     * @return the component or null if not found
     */
    Component getComponent(URI uri);

    /**
     * Returns a list of component URIs in the given hierarchy, e.g a domain or composite within a domain.
     *
     * @param uri a URI representing the hierarchy
     * @return the list of component URIs
     */
    List<URI> getComponentsInHierarchy(URI uri);
}
