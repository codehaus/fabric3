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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.fabric3.scdl.ModelObject;
import org.fabric3.scdl.Scope;

/**
 * Represents a physical component model.
 *
 * @version $Rev$ $Date$
 */
public abstract class PhysicalComponentDefinition extends ModelObject {

    private URI componentId;
    private Scope scope;
    private URI groupId;
    private int initLevel;
    private Set<QName> requestedIntents = new HashSet<QName>();;

    /**
     * Gets the component id.
     *
     * @return Component id.
     */
    public URI getComponentId() {
        return componentId;
    }

    /**
     * Sets the component id.
     *
     * @param componentId the component id
     */
    public void setComponentId(URI componentId) {
        this.componentId = componentId;
    }

    /**
     * Returns the id of the component group this component belongs to.
     * @return the id of the component group this component belongs to
     */
    public URI getGroupId() {
        return groupId;
    }

    /**
     * Sets the id of the component group this component belongs to.
     * @param groupId the id of the component group this component belongs to
     */
    public void setGroupId(URI groupId) {
        this.groupId = groupId;
    }

    public int getInitLevel() {
        return initLevel;
    }

    public void setInitLevel(int initLevel) {
        this.initLevel = initLevel;
    }

    /**
     * Gets the scope for the component.
     *
     * @return The scope for the component.
     */
    public Scope getScope() {
        return scope;
    }

    /**
     * Sets the scope for the component.
     *
     * @param scope The scope for the component.
     */
    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Set<QName> getRequestedIntents() {
        return Collections.unmodifiableSet(requestedIntents);
    }

    public void setRequestedIntents(Set<QName> requestedIntents) {
        this.requestedIntents = requestedIntents;
    }
    
    public void addRequestedIntent(QName requestedIntent) {
        requestedIntents.add(requestedIntent);
    }
}
