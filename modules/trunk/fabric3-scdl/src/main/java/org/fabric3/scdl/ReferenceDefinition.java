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
 * Represents a component reference
 *
 * @version $Rev$ $Date$
 */
public class ReferenceDefinition extends PolicyAware {
    
    private final String name;
    private ServiceContract<?> serviceContract;
    private Multiplicity multiplicity;
    private boolean required;
    private List<URI> promoted;
    private List<BindingDefinition> bindings;

    /**
     * @param name
     * @param serviceContract
     */
    public ReferenceDefinition(String name, ServiceContract<?> serviceContract) {
        this(name, serviceContract, Multiplicity.ONE_ONE);
        bindings = new ArrayList<BindingDefinition>();
        promoted = new ArrayList<URI>();
    }

    /**
     * @param name
     * @param serviceContract
     * @param multiplicity
     */
    public ReferenceDefinition(String name, ServiceContract<?> serviceContract, Multiplicity multiplicity) {
        this.name = name;
        this.serviceContract = serviceContract;
        this.multiplicity = multiplicity;
        bindings = new ArrayList<BindingDefinition>();
        promoted = new ArrayList<URI>();
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @return
     */
    public ServiceContract<?> getServiceContract() {
        return serviceContract;
    }

    /**
     * @param serviceContract
     */
    public void setServiceContract(ServiceContract<?> serviceContract) {
        this.serviceContract = serviceContract;
    }

    /**
     * @return
     */
    public Multiplicity getMultiplicity() {
        return multiplicity;
    }

    /**
     * @param multiplicity
     */
    public void setMultiplicity(Multiplicity multiplicity) {
        this.multiplicity = multiplicity;
    }

    /**
     * @return
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @param required
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @return
     */
    public List<URI> getPromoted() {
        return Collections.unmodifiableList(promoted);
    }

    /**
     * @param uri
     */
    public void addPromoted(URI uri) {
        promoted.add(uri);
    }

    /**
     * @return
     */
    public List<BindingDefinition> getBindings() {
        return Collections.unmodifiableList(bindings);
    }

    /**
     * @param binding
     */
    public void addBinding(BindingDefinition binding) {
        this.bindings.add(binding);
    }

}
