/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
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
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.Set;
import javax.xml.namespace.QName;

import org.osoa.sca.Constants;

/**
 * Representation of a wire from a reference to a service.
 *
 * @version $Rev: 59 $ $Date: 2007-05-19 08:21:09 +0100 (Sat, 19 May 2007) $
 */
public final class LogicalWire extends LogicalScaArtifact<LogicalComponent<?>> {
    private static final long serialVersionUID = -643283191171197255L;

    private static final QName TYPE = new QName(Constants.SCA_NS, "wire");

    private final LogicalReference source;
    private final URI targetUri;
    private LogicalState state = LogicalState.NEW;
    private QName deployable;

    /**
     * Instantiates a logical wire.
     *
     * @param parent    component within which the wire is defined.
     * @param source    the source reference of the wire
     * @param targetUri the uri of the target service
     */
    public LogicalWire(LogicalComponent<?> parent, LogicalReference source, URI targetUri) {
        super(null, parent, TYPE);
        this.source = source;
        this.targetUri = targetUri;
    }

    public LogicalWire(LogicalComponent<?> parent, LogicalReference source, URI targetUri, QName deployable) {
        super(null, parent, TYPE);
        this.source = source;
        this.targetUri = targetUri;
        this.deployable = deployable;
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
     */
    @Override
    public final Set<QName> getPolicySets() {
        throw new UnsupportedOperationException("Policy sets are not supported on wires");
    }

    /**
     * Intents are not supported on wires.
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
     * Returns the wire state.
     *
     * @return the wire state
     */
    public LogicalState getState() {
        return state;
    }

    /**
     * Sets the wire state.
     *
     * @param state the wire state
     */
    public void setState(LogicalState state) {
        this.state = state;
    }

    /**
     * Returns the deployable of the target for this wire. A source of a wire may be deployed via a different deployable thant its target. This value
     * is used to track the target deployable so the wire may be undeployed along wih the target even if the source is not.
     *
     * @return the deployable that provisioned the wire.
     */
    public QName getTargetDeployable() {
        return deployable;
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


}
