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
package org.fabric3.spi.model.physical;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.scdl.ModelObject;

/**
 * Models a physical change set, sent from the master to the slave.
 *
 * @version $Revsion$ $Date$
 */
public class PhysicalChangeSet extends ModelObject {

    // Set of physical component definitions
    private Set<PhysicalComponentDefinition> componentDefinitions;

    // Set of wire definitions
    private Set<PhysicalWireDefinition> wireDefinitions;

    private Map<Class<? extends PhysicalResourceContainerDefinition>,
            Map<URI, ? extends PhysicalResourceContainerDefinition>> resourceDefinitions;

    public PhysicalChangeSet() {
        componentDefinitions = new HashSet<PhysicalComponentDefinition>();
        wireDefinitions = new HashSet<PhysicalWireDefinition>();
        resourceDefinitions = new HashMap<Class<? extends PhysicalResourceContainerDefinition>,
                Map<URI, ? extends PhysicalResourceContainerDefinition>>();
    }

    /**
     * Get all the physical component definitions.
     *
     * @return Physical component definitions in the changeset.
     */
    public Set<PhysicalComponentDefinition> getComponentDefinitions() {
        return componentDefinitions;
    }

    /**
     * Get all the wire definitions.
     *
     * @return Wire definitions in the changeset.
     */
    public Set<PhysicalWireDefinition> getWireDefinitions() {
        return wireDefinitions;
    }

    /**
     * Get all resource definitions of a given type.
     *
     * @param type the resource definition type
     * @return Resource definitions in the changeset
     */
    @SuppressWarnings({"unchecked"})
    public <D extends PhysicalResourceContainerDefinition> Map<URI, ? extends PhysicalResourceContainerDefinition>
                                                           getResourceDefinitions(Class<D> type) {
        return resourceDefinitions.get(type);
    }

    @SuppressWarnings({"unchecked"})
    public <D extends PhysicalResourceContainerDefinition> D getResourceDefinition(Class<D> type, URI uri) {
        Map<URI, D> map = (Map<URI, D>) resourceDefinitions.get(type);
        if (map == null) {
            return null;
        }
        return map.get(uri);
    }

    public List<PhysicalResourceContainerDefinition> getAllResourceDefinitions() {
        List<PhysicalResourceContainerDefinition> definitions = new ArrayList<PhysicalResourceContainerDefinition>();
        Collection<Map<URI, ? extends PhysicalResourceContainerDefinition>> maps = resourceDefinitions.values();
        for (Map<URI, ? extends PhysicalResourceContainerDefinition> map : maps) {
            definitions.addAll(map.values());
        }
        return definitions;
    }

    /**
     * Adds a physical resource definition to the physical change set.
     *
     * @param definition Physical resource definition.
     */
    @SuppressWarnings({"unchecked"})
    public <D extends PhysicalResourceContainerDefinition> void addResourceDefinition(D definition) {
        Class<D> type = (Class<D>) definition.getClass();
        Map<URI, D> map = (Map<URI, D>) resourceDefinitions.get(type);
        if (map == null) {
            map = new HashMap<URI, D>();
            resourceDefinitions.put(type, map);
        }
        map.put(definition.getUri(), definition);

    }

    /**
     * Adds a physical component definition to the physical change set.
     *
     * @param componentDefinition Physical component definition.
     */
    public void addComponentDefinition(PhysicalComponentDefinition componentDefinition) {
        componentDefinitions.add(componentDefinition);
    }

    /**
     * Adds a physical wire definition to the physical change set.
     *
     * @param wireDefinition Physical wire definition.
     */
    public void addWireDefinition(PhysicalWireDefinition wireDefinition) {
        wireDefinitions.add(wireDefinition);
    }


}
