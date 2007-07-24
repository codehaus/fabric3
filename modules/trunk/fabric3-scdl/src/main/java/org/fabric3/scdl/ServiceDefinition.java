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
import java.util.List;

/**
 * Represents a service offered by a component
 *
 * @version $Rev$ $Date$
 */
public class ServiceDefinition extends ModelObject {
    private String name;
    private ServiceContract serviceContract;
    private boolean remotable;
    private String callbackRefName;
    private final List<BindingDefinition> bindings;
    private URI target;

    public ServiceDefinition(String name, ServiceContract<?> serviceContract, boolean remotable) {
        this.name = name;
        this.serviceContract = serviceContract;
        this.remotable = remotable;
        bindings = new ArrayList<BindingDefinition>();
    }

    public ServiceDefinition(String name, ServiceContract<?> serviceContract, boolean remotable, String callbackRefName) {
        this.name = name;
        this.serviceContract = serviceContract;
        this.remotable = remotable;
        this.callbackRefName = callbackRefName;
        bindings = new ArrayList<BindingDefinition>();
    }

    @Deprecated
    public ServiceDefinition() {
        bindings = new ArrayList<BindingDefinition>();
    }

    /**
     * Return the name of this service definition.
     *
     * @return the name of this service definition
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the service contract
     *
     * @return the service contract
     */
    public ServiceContract<?> getServiceContract() {
        return serviceContract;
    }

    /**
     * Sets the service contract
     *
     * @param contract the service contract
     */
    public void setServiceContract(ServiceContract contract) {
        this.serviceContract = contract;
    }

    /**
     * Returns true if the service is remotable
     *
     * @return true if the service is remotable
     */
    public boolean isRemotable() {
        return remotable;
    }

    /**
     * Sets if the service is remotable
     *
     * @param remotable if the service is remotable
     */
    public void setRemotable(boolean remotable) {
        this.remotable = remotable;
    }

    /**
     * Returns the callback name.
     */
    public String getCallbackReferenceName() {
        return callbackRefName;
    }

    /**
     * Sets the callback name
     */
    public void setCallbackReferenceName(String name) {
        this.callbackRefName = name;
    }

    /**
     * Returns the bindings configured for the service
     *
     * @return the bindings configured for the service
     */
    public List<BindingDefinition> getBindings() {
        return Collections.unmodifiableList(bindings);
    }

    /**
     * Configures the service with a binding
     *
     * @param binding the binding
     */
    public void addBinding(BindingDefinition binding) {
        this.bindings.add(binding);
    }

    /**
     * Returns the target URI the service is wired to
     *
     * @return the target URI the service is wired to
     */
    public URI getTarget() {
        return target;
    }

    /**
     * Sets the target URI the service is wired to
     *
     * @param target the target URI the service is wired to
     */
    public void setTarget(URI target) {
        this.target = target;
    }


}
