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
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * A specialization of component type for composite components.
 *
 * @version $Rev$ $Date$
 */
public class Composite extends AbstractComponentType<CompositeService, CompositeReference, Property<?>> implements PolicyAware {

    private final QName name;
    private URI contributionUri;
    private boolean local;
    private Autowire autowire;
    private final Map<String, ComponentDefinition<? extends Implementation<?>>> components =
            new HashMap<String, ComponentDefinition<? extends Implementation<?>>>();
    private final Map<QName, Include> includes = new HashMap<QName, Include>();
    private final List<WireDefinition> wires = new ArrayList<WireDefinition>();
    private QName constrainingType;
    private Set<QName> intents;
    private Set<QName> policySets;

    /**
     * Constructor defining the composite name.
     *
     * @param name the qualified name of this composite
     */
    public Composite(QName name) {
        this.name = name;
        setImplementationScope(Scope.COMPOSITE);
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
     * Indicates that components in this composite should be co-located.
     * @return true if components in this composite should be co-located
     */
    public boolean isLocal() {
        return local;
    }

    /**
     * Sets whether components in this composite should be co-located.
     * @param local true if components in this composite should be co-located
     */
    public void setLocal(boolean local) {
        this.local = local;
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

    /**
     * Returns the name of the constraining type for this composite.
     *
     * @return the name of the constraining type for this composite
     */
    public QName getConstrainingType() {
        return constrainingType;
    }

    /**
     * Sets the name of the constraining type for this composite.
     *
     * @param constrainingType the name of the constraining type for this composite
     */
    public void setConstrainingType(QName constrainingType) {
        this.constrainingType = constrainingType;
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
    public Map<String, CompositeReference> getReferences() {
        Map<String, CompositeReference> view = new HashMap<String, CompositeReference>(super.getReferences());
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
    public Map<String, CompositeService> getServices() {
        Map<String, CompositeService> view = new HashMap<String, CompositeService>(super.getServices());
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
    public Map<String, CompositeReference> getDeclaredReferences() {
        return super.getReferences();
    }

    /**
     * Get declared services in this composite type, included doesn't count
     */
    public Map<String, CompositeService> getDeclaredServices() {
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


    public Set<QName> getIntents() {
        return intents;
    }

    public void setIntents(Set<QName> intents) {
        this.intents = intents;
    }

    public Set<QName> getPolicySets() {
        return policySets;
    }

    public void setPolicySets(Set<QName> policySets) {
        this.policySets = policySets;
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

        Composite that = (Composite) o;
        return name.equals(that.name);
    }
}
