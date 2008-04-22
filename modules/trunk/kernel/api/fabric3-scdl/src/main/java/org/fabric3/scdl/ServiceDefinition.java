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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a service offered by a component
 *
 * @version $Rev$ $Date$
 */
public class ServiceDefinition extends AbstractPolicyAware {
    private String name;
    private ServiceContract<?> serviceContract;
    private boolean management;
    private final List<BindingDefinition> bindings = new ArrayList<BindingDefinition>();
    private final List<BindingDefinition> callbackBindings = new ArrayList<BindingDefinition>();
    private final List<OperationDefinition> operations = new ArrayList<OperationDefinition>();

    public ServiceDefinition(String name) {
        this(name, null);
    }

    public ServiceDefinition(String name, ServiceContract<?> serviceContract) {
        this.name = name;
        this.serviceContract = serviceContract;
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
     * Returns true if this is a management service.
     * <p/>
     * A management service is intended for administration of a component rather than normal interactions with it. As such, it is not considered a
     * valid target for wiring (including autowire).
     *
     * @return true if this is a management service
     */
    public boolean isManagement() {
        return management;
    }

    /**
     * Sets whether this is a management service
     *
     * @param management true if this is a management service
     */
    public void setManagement(boolean management) {
        this.management = management;
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
    public void setServiceContract(ServiceContract<?> contract) {
        this.serviceContract = contract;
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
     * @return List of callback bindings defined against the reference.
     */
    public List<BindingDefinition> getCallbackBindings() {
        return Collections.unmodifiableList(callbackBindings);
    }

    /**
     * @param binding callback binding to be added.
     */
    public void addCallbackBinding(BindingDefinition binding) {
        this.callbackBindings.add(binding);
    }


    /**
     * @return Get the list of operations defined against the reference.
     */
    public List<OperationDefinition> getOperations() {
        return Collections.unmodifiableList(operations);
    }

    /**
     * @param operation Operation definition to be added.
     */
    public void addOperation(OperationDefinition operation) {
        operations.add(operation);
    }
}
