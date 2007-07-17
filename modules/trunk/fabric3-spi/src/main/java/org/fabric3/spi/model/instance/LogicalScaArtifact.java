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
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * Super class for all logical SCA artifacts.
 *
 * @version $Revision$ $Date$
 */
public class LogicalScaArtifact<P extends LogicalScaArtifact<?>> extends Referenceable {
    
    private Set<QName> intents = new HashSet<QName>();
    private Set<QName> policySets = new HashSet<QName>();
    private P parent;

    /**
     * @param uri URI of the SCA artifact.
     * @param parent Parent of the SCA artifact.
     */
    public LogicalScaArtifact(URI uri, P parent) {
        super(uri);
        this.parent = parent;
    }
    
    /**
     * @return Parent of this SCA artifact.
     */
    public P getParent() {
        return parent;
    }

    /**
     * @return Intents declared on the SCA artifact.
     */
    public Set<QName> getIntents() {
        return intents;
    }

    /**
     * @param intents Intents declared on the SCA artifact.
     */
    public void setIntents(Set<QName> intents) {
        this.intents = intents;
    }

    /**
     * @return Policy sets declared on the SCA artifact.
     */
    public Set<QName> getPolicySets() {
        return policySets;
    }

    /**
     * @param policySets Policy sets declared on the SCA artifact.
     */
    public void setPolicySets(Set<QName> policySets) {
        this.policySets = policySets;
    }
    
}
