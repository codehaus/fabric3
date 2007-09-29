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
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.Autowire;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.PropertyValue;
import org.osoa.sca.Constants;

/**
 * Represents an instantiated component in the service network.
 *
 * @version $Rev$ $Date$
 */
public class LogicalComponent<I extends Implementation<?>> extends LogicalScaArtifact<LogicalComponent<CompositeImplementation>> {

    private static final QName TYPE = new QName(Constants.SCA_NS, "component");

    private final ComponentDefinition<I> definition;
    private final Map<String, PropertyValue> propertyValues = new HashMap<String, PropertyValue>();
    private final Map<URI, LogicalComponent<?>> components = new HashMap<URI, LogicalComponent<?>>();
    private final Map<String, LogicalService> services = new HashMap<String, LogicalService>();
    private final Map<String, LogicalReference> references = new HashMap<String, LogicalReference>();
    private final Map<String, LogicalResource> resources = new HashMap<String, LogicalResource>();
    private URI runtimeId;
    private boolean active;
    private Autowire autowire;

    /**
     * @param uri        URI of the component.
     * @param runtimeId  URI of the runtime to which the component has to be provisioned.
     * @param definition Definition of the component.
     * @param parent     Parent of the component.
     */
    public LogicalComponent(URI uri, URI runtimeId,
                            ComponentDefinition<I> definition,
                            LogicalComponent<CompositeImplementation> parent) {
        super(uri, parent, TYPE);
        this.runtimeId = runtimeId;
        this.definition = definition;
    }

    /**
     * Returns the runtime id the component is provisioned to.
     *
     * @return the runtime id the component is provisioned to
     */
    public URI getRuntimeId() {
        return runtimeId;
    }

    /**
     * Sets the runtime id the component is provisioned to.
     *
     * @param runtimeId the runtime id the component is provisioned to
     */
    public void setRuntimeId(URI runtimeId) {
        this.runtimeId = runtimeId;
    }

    /**
     * True if the component is curently active on a node.
     *
     * @return true if the component is curently active on a node
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets if the component is currently active on a node.
     *
     * @param active true if the component is active
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns the overriden autowire value or null if not overriden
     *
     * @return the overriden autowire value or null if not overriden
     */
    public Autowire getAutowireOverride() {
        return autowire;
    }

    /**
     * Sets the overriden autowire value
     *
     * @param autowire the autowire value
     */
    public void setAutowireOverride(Autowire autowire) {
        this.autowire = autowire;
    }

    /**
     * Returns the child components of the current component.
     *
     * @return the child components of the current component
     */
    public Collection<LogicalComponent<?>> getComponents() {
        return Collections.unmodifiableCollection(components.values());
    }

    /**
     * Returns a child component with the given URI.
     *
     * @param uri the child component URI
     * @return a child component with the given URI.
     */
    public LogicalComponent<?> getComponent(URI uri) {
        return components.get(uri);
    }

    /**
     * Adds a child component
     *
     * @param component the child component to add
     */
    public void addComponent(LogicalComponent<?> component) {
        components.put(component.getUri(), component);
    }

    /**
     * Returns the services offered by the current component.
     *
     * @return the services offered by the current component
     */
    public Collection<LogicalService> getServices() {
        return Collections.unmodifiableCollection(services.values());
    }

    /**
     * Returns a service with the given URI.
     *
     * @param name the service name
     * @return the service.
     */
    public LogicalService getService(String name) {
        return services.get(name);
    }

    /**
     * Adds a the resolved service
     *
     * @param service the service to add
     */
    public void addService(LogicalService service) {
        services.put(service.getUri().getFragment(), service);
    }

    /**
     * Returns the resources required by the current component.
     *
     * @return the resources required by the current component
     */
    public Collection<LogicalResource> getResources() {
        return Collections.unmodifiableCollection(resources.values());
    }

    /**
     * Returns a resource with the given URI.
     *
     * @param name the resource name
     * @return the resource.
     */
    public LogicalResource getResource(String name) {
        return resources.get(name);
    }

    /**
     * Adds a the resolved resource
     *
     * @param reference the resource to add
     */
    public void addResource(LogicalResource resource) {
        resources.put(resource.getUri().getFragment(), resource);
    }

    /**
     * Returns the resolved component references.
     *
     * @return the component references
     */
    public Collection<LogicalReference> getReferences() {
        return Collections.unmodifiableCollection(references.values());
    }

    /**
     * Returns a the resolved reference with the given URI.
     *
     * @param name the reference name
     * @return the reference.
     */
    public LogicalReference getReference(String name) {
        return references.get(name);
    }

    /**
     * Adds a resolved reference
     *
     * @param reference the reference to add
     */
    public void addReference(LogicalReference reference) {
        references.put(reference.getUri().getFragment(), reference);
    }

    /**
     * Returns the resolved property values for the component.
     *
     * @return the resolved property values for the component
     */
    public Collection<PropertyValue> getPropertyValues() {
        return Collections.unmodifiableCollection(propertyValues.values());
    }

    /**
     * Gets the value of a property.
     * 
     * @param name Name of the property.
     * @return Propert value for the specified property.
     */
    public PropertyValue getPropertyValue(String name) {
        return propertyValues.get(name);
    }

    /**
     * Adds a resolved property value
     *
     * @param name  the property name
     * @param value the property value
     */
    public void addPropertyValue(String name, PropertyValue value) {
        propertyValues.put(name, value);
    }

    /**
     * Returns the component implementation type.
     *
     * @return the component implementation type
     */
    public ComponentDefinition<I> getDefinition() {
        return definition;
    }
    
    /**
     * Gets the component type.
     * @return Component type.
     */
    public AbstractComponentType<?, ?, ?, ?> getComponentType() {
        return getDefinition().getComponentType();
    }

}
