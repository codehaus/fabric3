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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fabric3.model.type.CapabilityAware;
import org.fabric3.model.type.ModelObject;

/**
 * The definition of the configurable aspects of an implementation in terms of the services it exposes, the services it references, and properties
 * that can be used to configure it.
 * <p/>
 * A service represents an addressable interface provided by the implementation. Such a service may be the target of a wire from another component.
 * <p/>
 * A reference represents a requirement that an implementation has on a service provided by another component or by a resource outside the SCA system.
 * Such a reference may be the source of a wire to another component.
 * <p/>
 * A property allows the behaviour of the implementation to be configured through externally set values.
 * <p/>
 * A component type may also declare that it wishes to be initialized upon activation of the scope that contains it and may specify an order relative
 * to other eagerly initializing components. For example, an implementation that pre-loads some form of cache could declare that it should be eagerly
 * initialized at the start of the scope so that the cache load occured on startup rather than first use.
 *
 * @version $Rev: 5481 $ $Date: 2008-09-26 02:36:30 -0700 (Fri, 26 Sep 2008) $
 */
public abstract class AbstractComponentType<S extends ServiceDefinition,
        R extends ReferenceDefinition,
        P extends Property,
        RD extends ResourceDefinition>
        extends ModelObject implements CapabilityAware {
    private static final long serialVersionUID = 5302580019263119837L;
    private String scope;
    private int initLevel;
    private long maxAge;
    private long maxIdleTime;
    private final Map<String, S> services = new HashMap<String, S>();
    private final Map<String, R> references = new HashMap<String, R>();
    private final Map<String, P> properties = new HashMap<String, P>();
    private final Map<String, RD> resources = new HashMap<String, RD>();
    private final Set<String> requiredCapabilities = new HashSet<String>();

    protected AbstractComponentType() {
    }

    /**
     * Returns the lifecycle scope for the component.
     *
     * @return the lifecycle scope for the component
     */
    public String getScope() {
        return scope;
    }

    /**
     * Sets the lifecycle scope for the component.
     *
     * @param scope the lifecycle scope for the component
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * Returns the default initialization level for components of this type. A value greater than zero indicates that components should be eagerly
     * initialized.
     *
     * @return the default initialization level
     */
    public int getInitLevel() {
        return initLevel;
    }

    /**
     * Sets the default initialization level for components of this type. A value greater than zero indicates that components should be eagerly
     * initialized.
     *
     * @param initLevel default initialization level for components of this type
     */
    public void setInitLevel(int initLevel) {
        this.initLevel = initLevel;
    }

    /**
     * Returns true if this component should be eagerly initialized.
     *
     * @return true if this component should be eagerly initialized
     */
    public boolean isEagerInit() {
        return initLevel > 0;
    }

    /**
     * Returns the idle time allowed between operations in milliseconds if the implementation is conversational
     *
     * @return the idle time allowed between operations in milliseconds if the implementation is conversational
     */
    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    /**
     * Sets the idle time allowed between operations in milliseconds if the implementation is conversational.
     *
     * @param maxIdleTime the idle time allowed between operations in milliseconds if the implementation is conversational
     */
    public void setMaxIdleTime(long maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    /**
     * Returns the maximum age a conversation may remain active in milliseconds if the implementation is conversational
     *
     * @return the maximum age a conversation may remain active in milliseconds if the implementation is conversational
     */
    public long getMaxAge() {
        return maxAge;
    }

    /**
     * Sets the maximum age a conversation may remain active in milliseconds if the implementation is conversational.
     *
     * @param maxAge the maximum age a conversation may remain active in milliseconds if the implementation is conversational
     */
    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * Returns a live Map of the services provided by the implementation.
     *
     * @return a live Map of the services provided by the implementation
     */
    public Map<String, S> getServices() {
        return services;
    }

    /**
     * Add a service to those provided by the implementation. Any existing service with the same name is replaced.
     *
     * @param service a service provided by the implementation
     */
    public void add(S service) {
        services.put(service.getName(), service);
    }

    /**
     * Checks if this component type has a service with a certain name.
     *
     * @param name the name of the service to check
     * @return true if there is a service defined with that name
     */
    public boolean hasService(String name) {
        return services.containsKey(name);
    }

    /**
     * Returns a live Map of references to services consumed by the implementation.
     *
     * @return a live Map of references to services consumed by the implementation
     */
    public Map<String, R> getReferences() {
        return references;
    }

    /**
     * Add a reference to a service consumed by the implementation. Any existing reference with the same name is replaced.
     *
     * @param reference a reference to a service consumed by the implementation
     */
    public void add(R reference) {
        references.put(reference.getName(), reference);
    }

    /**
     * Checks if this component type has a reference with a certain name.
     *
     * @param name the name of the reference to check
     * @return true if there is a reference defined with that name
     */
    public boolean hasReference(String name) {
        return references.containsKey(name);
    }

    /**
     * Returns a live Map of properties that can be used to configure the implementation.
     *
     * @return a live Map of properties that can be used to configure the implementation
     */
    public Map<String, P> getProperties() {
        return properties;
    }

    /**
     * Add a property that can be used to configure the implementation. Any existing property with the same name is replaced.
     *
     * @param property a property that can be used to configure the implementation
     */
    public void add(P property) {
        properties.put(property.getName(), property);
    }

    /**
     * Checks if this component type has a property with a certain name.
     *
     * @param name the name of the property to check
     * @return true if there is a property defined with that name
     */
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    /**
     * Returns a live Map of resoures that can be used to configure the implementation.
     *
     * @return a live Map of resources that can be used to configure the implementation
     */
    public Map<String, RD> getResources() {
        return resources;
    }

    /**
     * Add a resource that can be used to configure the implementation. Any existing resource with the same name is replaced.
     *
     * @param resource a resource that can be used to configure the implementation
     */
    public void add(RD resource) {
        resources.put(resource.getName(), resource);
    }

    /**
     * Checks if this component type has a resource with a certain name.
     *
     * @param name the name of the resource to check
     * @return true if there is a resource defined with that name
     */
    public boolean hasResource(String name) {
        return resources.containsKey(name);
    }

    public Set<String> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    public void addRequiredCapability(String capability) {
        requiredCapabilities.add(capability);
    }
}
