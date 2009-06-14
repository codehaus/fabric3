/*
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.model.type.component;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

import org.fabric3.model.type.AbstractPolicyAware;

/**
 * Represents a component. <p>A component is a configured instance of an implementation. The provided and consumed services, as well as the available
 * configuration properties are defined by the implementation (represented by its componentType).</p> <p>Every component has a name which uniquely
 * identifies it within the scope of the composite that contains it; the name must be different from the names of all other components, services and
 * references immediately contained in the composite (directly or through an &lt;include&gt; element).</p> <p>A component may define a {@link
 * PropertyValue} that overrides the default value of a {@link Property} specified in the componentType.</p> <p>It may also define a
 * {@link ComponentReference} for a {@link ReferenceDefinition} defined in the componentType. The ComponentReference must resolve to
 * another component or a reference in the enclosing composite.</p> <p>A component may define a {@link ComponentService} for a {@link
 * ServiceDefinition} specified in the componentType.</p> <p>Components may specify an initialization level that will determine the
 * order in which it will be eagerly initialized relative to other components from the enclosing composite that are in the same scope. This can be
 * used to define a startup sequence for components that are otherwise independent. Any initialization required to resolve references between
 * components will override this initialization order.</p>
 *
 * @version $Rev: 5594 $ $Date: 2008-10-10 09:41:58 -0700 (Fri, 10 Oct 2008) $
 */
public class ComponentDefinition<I extends Implementation<?>> extends AbstractPolicyAware {
    private static final long serialVersionUID = 4909969579651563484L;

    private final String name;
    private Autowire autowire = Autowire.INHERITED;
    private Integer initLevel;
    private I implementation;
    private final Map<String, ComponentService> services = new HashMap<String, ComponentService>();
    private final Map<String, ComponentReference> references = new HashMap<String, ComponentReference>();
    private final Map<String, PropertyValue> propertyValues = new HashMap<String, PropertyValue>();
    private Document key;
    private URI contributionUri;

    /**
     * Constructor specifying the component's name.
     *
     * @param name the name of this component
     */
    public ComponentDefinition(String name) {
        this.name = name;
    }

    /**
     * Constructor specifying the component's name and implementation.
     *
     * @param name           the name of this component
     * @param implementation the implementation of this component
     */
    public ComponentDefinition(String name, I implementation) {
        this.name = name;
        this.implementation = implementation;
    }

    /**
     * Sets the {@link Implementation} of this component.
     *
     * @param implementation the implementation of this component
     */
    public void setImplementation(I implementation) {
        this.implementation = implementation;
    }

    /**
     * Returns the {@link Implementation} of this component.
     *
     * @return the implementation of this component
     */
    public I getImplementation() {
        return implementation;
    }

    /**
     * Returns the name of this component.
     *
     * @return the name of this component
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the autowire status for the component.
     *
     * @return the autowire status for the component.
     */
    public Autowire getAutowire() {
        return autowire;
    }

    /**
     * Sets the autowire status for the component.
     *
     * @param autowire the autowire status.
     */
    public void setAutowire(Autowire autowire) {
        this.autowire = autowire;
    }

    /**
     * Returns the initialization level of this component.
     *
     * @return the initialization level of this component
     */
    public Integer getInitLevel() {
        return initLevel;
    }

    /**
     * Sets the initialization level of this component. If set to null then the level from the componentType is used. If set to zero or a negative
     * value then the component will not be eagerly initialized.
     *
     * @param initLevel the initialization level of this component
     */
    public void setInitLevel(Integer initLevel) {
        this.initLevel = initLevel;
    }

    /**
     * Returns a live Map of the {@link ComponentReference targets} configured by this component definition.
     *
     * @return the reference targets configured by this component
     */
    public Map<String, ComponentReference> getReferences() {
        return references;
    }

    /**
     * Add a reference target configuration to this component. Any existing configuration for the reference named in the target is replaced.
     *
     * @param target the target to add
     */
    public void add(ComponentReference target) {
        references.put(target.getName(), target);
    }

    /**
     * Returns a live Map of the {@link ComponentService}s configured by this component definition.
     *
     * @return the services configured by this component
     */
    public Map<String, ComponentService> getServices() {
        return services;
    }

    /**
     * Add a service configuration to this component. Any existing configuration for the service is replaced.
     *
     * @param service the service to add
     */
    public void add(ComponentService service) {
        services.put(service.getName(), service);
    }

    /**
     * Returns a live Map of {@link PropertyValue property values} configured by this component definition.
     *
     * @return the property values configured by this component
     */
    public Map<String, PropertyValue> getPropertyValues() {
        return propertyValues;
    }

    /**
     * Add a property value configuration to this component. Any existing configuration for the property names in the property value is replaced.
     *
     * @param value the property value to add
     */
    public void add(PropertyValue value) {
        propertyValues.put(value.getName(), value);
    }

    /**
     * Returns the key to be used if this component is wired to a map of references.
     *
     * @return The value of the key.
     */
    public Document getKey() {
        return key;
    }

    /**
     * Returns the key to be used if this component is wired to a map of references.
     *
     * @param key The value of the key.
     */
    public void setKey(Document key) {
        this.key = key;
    }

    /**
     * Gets the component type.
     *
     * @return Component type.
     */
    public AbstractComponentType<?, ?, ?, ?> getComponentType() {
        return getImplementation().getComponentType();
    }

    /**
     * Returns the URI of the contribution the component definition is contained in.
     *
     * @return the URI of the contribution the component definition is contained in.
     */
    public URI getContributionUri() {
        return contributionUri;
    }

    /**
     * Sets the URI of the contribution the component definition is contained in.
     *
     * @param contributionUri the URI of the contribution the component definition is contained in.
     */
    public void setContributionUri(URI contributionUri) {
        this.contributionUri = contributionUri;
    }

}
