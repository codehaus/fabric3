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

/**
 * Representation of a wire between two components. This can correspond to one
 * of the following,
 * 
 * 1. An explicitly defined wire in the composite. 2. A wire resulting from an
 * explictly requested target on a reference. 3. A wire resulting from the
 * resolution of an autowire.
 * 
 * @version $Rev: 59 $ $Date: 2007-05-19 08:21:09 +0100 (Sat, 19 May 2007) $
 */
public final class LogicalWire extends LogicalScaArtifact<LogicalComponent<?>> {

    private static final QName TYPE = new QName(Constants.SCA_NS, "wire");

    private final LogicalReference source;
    private final URI targetUri;
    private boolean provisioned;

    /**
     * Instantiates a logical wire.
     * 
     * @param uri URI of the wire.
     * @param parent Component within which the wire is defined.
     * @param sourceUri Source URI of the wire.
     * @param targetUri Target URI of the wire.
     * @param wireType Type of the wire.
     */
    public LogicalWire(final LogicalComponent<?> parent, final LogicalReference source, final URI targetUri) {
        
        super(null, parent, TYPE);
        
        if (source == null) {
            throw new IllegalArgumentException("Source cannot be null");
        };
        
        if (targetUri == null) {
            throw new IllegalArgumentException("Target URI cannot be null");
        };
        
        this.source = source;
        this.targetUri = targetUri;
        
    }

    /**
     * Gets the source of the wire.
     * 
     * @return Source of the wire.
     */
    public final LogicalReference getSource() {
        return source;
    }

    /**
     * Gets the target URI of the wire.
     * 
     * @return Target URI of the wire.
     */
    public final URI getTargetUri() {
        return targetUri;
    }

    /**
     * Intents are not supported on wires.
     * 
     * @return Intents declared on the SCA artifact.
     */
    @Override
    public final Set<QName> getIntents() {
        throw new UnsupportedOperationException("Intents are not supported on wires");
    }

    /**
     * Policy sets are not supported on wires.
     * 
     * @param intents Intents declared on the SCA artifact.
     */
    @Override
    public final Set<QName> getPolicySets() {
        throw new UnsupportedOperationException("Policy sets are not supported on wires");
    }

    /**
     * Intents are not supported on wires.
     * 
     * @return Policy sets declared on the SCA artifact.
     */
    @Override
    public final void setIntents(Set<QName> intents) {
        throw new UnsupportedOperationException("Intents are not supported on wires");
    }

    /**
     * Policy sets are not supported on wires.
     * 
     * @param policySets Policy sets declared on the SCA artifact.
     */
    @Override
    public final void setPolicySets(Set<QName> policySets) {
        throw new UnsupportedOperationException("Policy sets are not supported on wires");
    }

    /**
     * Tests for quality whether the source and target URIs are the same.
     * 
     * @param obj Object to be tested against.
     */
    @Override
    public boolean equals(Object obj) {
        
        if (this == obj) {
            return true;
        }
        
        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }
        
        LogicalWire test = (LogicalWire) obj;
        return targetUri.equals(test.targetUri) && source.equals(test.source);
        
    }

    /**
     * Hashcode based on the source and target URIs.
     * 
     * @return Hashcode based on the source and target URIs.
     */
    public int hashCode() {
        
        int hash = 7;
        hash = 31 * hash + source.hashCode();
        hash = 31 * hash + targetUri.hashCode();
        return hash;
        
    }

    /**
     * Checks whether the wire has been provisioned.
     * 
     * @return True if the wire has been provisioned.
     */
    public boolean isProvisioned() {
        return provisioned;
    }

    /**
     * Marks thw wire as provisioned/unprovisioned.
     * 
     * @param provisioned True if the wire has been provisioned.
     */
    public void setProvisioned(boolean provisioned) {
        this.provisioned = provisioned;
    }

}
