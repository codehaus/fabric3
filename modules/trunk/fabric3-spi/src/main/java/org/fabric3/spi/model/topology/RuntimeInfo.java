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
package org.fabric3.spi.model.topology;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.spi.Constants;
import org.fabric3.spi.model.type.ResourceDescription;

/**
 * Tracks information regarding a runtime service node, including available capabilities and resources
 *
 * @version $Rev$ $Date$
 */
public class RuntimeInfo {
    public static final QName QNAME = new QName(Constants.FABRIC3_NS, "runtimeInfo");

    private String id;
    private List<ResourceDescription<?>> resources;
    private Set<URI> components;

    public RuntimeInfo() {
        resources = new ArrayList<ResourceDescription<?>>();
        components = new HashSet<URI>();
    }

    public RuntimeInfo(String id) {
        this();
        this.id = id;
    }

    /**
     * Returns the runtime id.
     *
     * @return the runtime id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns a list of resource descriptions for available runtime resources such as extensions.
     *
     * @return the list of resource descriptions
     */
    public List<ResourceDescription<?>> getResourceDescriptions() {
        return Collections.unmodifiableList(resources);
    }

    /**
     * Adds a resource description representing an available runtime resources
     *
     * @param resource the resource description
     */
    public void addResourceDescription(ResourceDescription<?> resource) {
        resources.add(resource);
    }


    public Set<URI> getComponents() {
        return Collections.unmodifiableSet(components);
    }

    public void addComponent(URI uri) {
        components.add(uri);
    }
}
