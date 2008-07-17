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

import org.fabric3.scdl.ResourceDefinition;
import org.fabric3.spi.Constants;

/**
 * Represents a resource in the logical model.
 * 
 * @version $Revision$ $Date$
 */
public class LogicalResource<RD extends ResourceDefinition> extends LogicalScaArtifact<LogicalComponent<?>> {
    
    private static final QName TYPE = new QName(Constants.FABRIC3_NS, "resource");
    
    private final RD resourceDefinition;
    private URI target;

    /**
     * Initializes the URI and the resource definition.
     * 
     * @param uri URI of the resource.
     * @param resourceDefinition Definition of the resource.
     */
    public LogicalResource(URI uri, RD resourceDefinition, LogicalComponent<?> parent) {
        super(uri, parent, TYPE);
        this.resourceDefinition = resourceDefinition;
    }
    
    /**
     * Gets the definition for this resource.
     * 
     * @return Definition for this resource.
     */
    public final RD getResourceDefinition() {
        return resourceDefinition;
    }
    
    /**
     * Gets the target for the resource.
     * 
     * @return Resource target.
     */
    public URI getTarget() {
        return target;
    }
    
    /**
     * Sets the target for the resource.
     * 
     * @param target Resource target.
     */
    public void setTarget(URI target) {
        this.target = target;
    }

    /**
     * @return Intents declared on the SCA artifact.
     */
    public Set<QName> getIntents() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * @param intents Intents declared on the SCA artifact.
     */
    public void setIntents(Set<QName> intents) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return Policy sets declared on the SCA artifact.
     */
    public Set<QName> getPolicySets() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param policySets Policy sets declared on the SCA artifact.
     */
    public void setPolicySets(Set<QName> policySets) {
        throw new UnsupportedOperationException();
    }

}
