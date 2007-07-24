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
package org.fabric3.scdl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

/**
 * A specialization of component type for composite components.
 *
 * @version $Rev$ $Date$
 */
public class CompositeComponentType extends ComponentType<ServiceDefinition, ReferenceDefinition, Property<?>> {

    private QName name;
    private URI contributionUri;
    private Autowire autowire;
    private final Map<String, ComponentDefinition<? extends Implementation<?>>> components =
            new HashMap<String, ComponentDefinition<? extends Implementation<?>>>();
    private final Map<QName, Include> includes = new HashMap<QName, Include>();
    private final List<WireDefinition> wires = new ArrayList<WireDefinition>();

    public CompositeComponentType() {
        setImplementationScope(Scope.COMPOSITE);
    }

    /**
     * Constructor defining the composite name.
     *
     * @param name the qualified name of this composite
     */
    public CompositeComponentType(QName name) {
        this();
        this.name = name;
    }

    /**
     * Returns the qualified name of this composite. The namespace portion of this name is the targetNamespace for other
     * qualified names used in the composite.
     *
     * @return the qualified name of this composite
     */
    public QName getName() {
        return name;
    }

    /**
     * Set the qualified name of this composite.
     *
     * @param name the qualified name of this composite
     */
    public void setName(QName name) {
        this.name = name;
    }

    /**
     * Returns the URI of the contribution this componentType is associated with.
     *
     * @return the URI of the contribution this componentType is associated with
     */
    public URI getContributionUri() {
        return contributionUri;
    }

    /**
     * Sets the URI of the contribution this componentType is associated with.
     *
     * @param contributionUri tcontribution URI
     */
    public void setContributionUri(URI contributionUri) {
        this.contributionUri = contributionUri;
    }

    /**
     * Returns if the autowire status for composite
     *
     * @return the autowire status for the composite
     */
    public Autowire getAutowire() {
        return autowire;
    }

    /**
     * Sets the autowire status for the composite
     *
     * @param autowire the autowire status for the composite
     */
    public void setAutowire(Autowire autowire) {
        this.autowire = autowire;
    }

    @Override
    @SuppressWarnings("unchecked")
    /**
     * Get all properties including the ones are from included composites
     * @return
     */
    public Map<String, Property<?>> getProperties() {
        Map<String, Property<?>> view = new HashMap<String, Property<?>>(super.getProperties());
        for (Include i : includes.values()) {
            view.putAll(i.getIncluded().getProperties());
        }
        return Collections.unmodifiableMap(view);
    }

    @Override
    @SuppressWarnings("unchecked")
    /**
     * Get all references including the ones are from included composites
     * @return
     */
    public Map<String, ReferenceDefinition> getReferences() {
        Map<String, ReferenceDefinition> view = new HashMap<String, ReferenceDefinition>(super.getReferences());
        for (Include i : includes.values()) {
            view.putAll(i.getIncluded().getReferences());
        }
        return Collections.unmodifiableMap(view);
    }

    @SuppressWarnings("unchecked")
    @Override
    /**
     * Get all services including the ones are from included composites
     * @return
     */
    public Map<String, ServiceDefinition> getServices() {
        Map<String, ServiceDefinition> view = new HashMap<String, ServiceDefinition>(super.getServices());
        for (Include i : includes.values()) {
            view.putAll(i.getIncluded().getServices());
        }
        return Collections.unmodifiableMap(view);
    }

    /**
     * Get all components including the ones are from included composites
     */
    @SuppressWarnings("unchecked")
    public Map<String, ComponentDefinition<? extends Implementation<?>>> getComponents() {
        Map<String, ComponentDefinition<? extends Implementation<?>>> view =
                new HashMap<String, ComponentDefinition<? extends Implementation<?>>>(components);
        for (Include i : includes.values()) {
            view.putAll(i.getIncluded().getComponents());
        }
        return Collections.unmodifiableMap(view);
    }


    /**
     * Get all wires including the ones are from included composites
     */
    @SuppressWarnings("unchecked")
    public List<WireDefinition> getWires() {
        List<WireDefinition> view =
                new ArrayList<WireDefinition>(wires);
        for (Include i : includes.values()) {
            view.addAll(i.getIncluded().getWires());
        }
        return Collections.unmodifiableList(view);
    }

    /**
     * Get declared properties in this composite type, included doesn't count
     */
    public Map<String, Property<?>> getDeclaredProperties() {
        return super.getProperties();
    }

    /**
     * Get declared references in this composite type, included doesn't count
     */
    public Map<String, ReferenceDefinition> getDeclaredReferences() {
        return super.getReferences();
    }

    /**
     * Get declared services in this composite type, included doesn't count
     */
    public Map<String, ServiceDefinition> getDeclaredServices() {
        return super.getServices();
    }

    /**
     * Get declared components in this composite type, included doesn't count
     */
    public Map<String, ComponentDefinition<? extends Implementation<?>>> getDeclaredComponents() {
        return components;
    }

    /**
     * Get declared wires in this composite type, included doesn't count
     */
    public List<WireDefinition> getDeclaredWires() {
        return wires;
    }

    public void add(WireDefinition wireDefn) {
        wires.add(wireDefn);
    }


    public void add(ComponentDefinition<? extends Implementation<?>> componentDefinition) {
        components.put(componentDefinition.getName(), componentDefinition);
    }

    public Map<QName, Include> getIncludes() {
        return includes;
    }

    public void add(Include include) {
        includes.put(include.getName(), include);
    }


    public int hashCode() {
        return name.hashCode();
    }


    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CompositeComponentType that = (CompositeComponentType) o;
        return name.equals(that.name);
    }
}
