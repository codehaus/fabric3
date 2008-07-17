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
import java.util.Set;

import javax.xml.namespace.QName;

import org.osoa.sca.Constants;

import org.fabric3.scdl.ServiceDefinition;

/**
 * Represents a resolved service
 *
 * @version $Rev$ $Date$
 */
public class LogicalService extends Bindable {

    private static final QName TYPE = new QName(Constants.SCA_NS, "service");

    private final ServiceDefinition definition;
    private URI promote;

    /**
     * Default constructor
     *
     * @param uri        the service uri
     * @param definition the service definition
     * @param parent     the service parent component
     */
    public LogicalService(URI uri, ServiceDefinition definition, LogicalComponent<?> parent) {
        super(uri, parent, TYPE);
        this.definition = definition;
    }

    /**
     * Returns the service definition for the logical service.
     *
     * @return the service definition for the logical service
     */
    public ServiceDefinition getDefinition() {
        return definition;
    }

    /**
     * Returns the component service uri promoted by this service.
     *
     * @return the component service uri promoted by this service
     */
    public URI getPromotedUri() {
        return promote;
    }

    /**
     * Sets the component service uri promoted by this service
     *
     * @param uri the component service uri promoted by this service
     */
    public void setPromotedUri(URI uri) {
        this.promote = uri;
    }

    /**
     * @return Intents declared on the SCA artifact.
     */
    public Set<QName> getIntents() {
        return definition.getIntents();
    }
    
    /**
     * @param intents Intents declared on the SCA artifact.
     */
    public void setIntents(Set<QName> intents) {
        definition.setIntents(intents);
    }
    
    /**
     * @param intents Adds intents to the definition.
     */
    public void addIntents(Set<QName> intents) {
        definition.addIntents(intents);
    }

    /**
     * @return Policy sets declared on the SCA artifact.
     */
    public Set<QName> getPolicySets() {
        return definition.getPolicySets();
    }

    /**
     * @param policySets Policy sets declared on the SCA artifact.
     */
    public void setPolicySets(Set<QName> policySets) {
        definition.setPolicySets(policySets);
    }
    
    /**
     * @param policySets Adds policy sets to the definition.
     */
    public void addPolicySets(Set<QName> policySets) {
        definition.addPolicySets(policySets);
    }

}
