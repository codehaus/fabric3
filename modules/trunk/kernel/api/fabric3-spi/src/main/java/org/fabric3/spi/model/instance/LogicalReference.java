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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.osoa.sca.Constants;

import org.fabric3.scdl.ComponentReference;
import org.fabric3.scdl.ReferenceDefinition;

/**
 * Represents a resolved reference
 *
 * @version $Rev$ $Date$
 */
public class LogicalReference extends Bindable {
    private static final long serialVersionUID = 2308698868251298609L;

    private static final QName TYPE = new QName(Constants.SCA_NS, "reference");

    private final ReferenceDefinition definition;
    private List<URI> promotedUris;
    private boolean resolved;

    /**
     * Constructor.
     *
     * @param uri        the reference URI
     * @param definition the reference type definition
     * @param parent     the parent component
     */
    public LogicalReference(URI uri, ReferenceDefinition definition, LogicalComponent<?> parent) {
        super(uri, parent, TYPE);
        this.definition = definition;
        promotedUris = new ArrayList<URI>();
    }

    /**
     * Returns the reference type definition.
     *
     * @return the reference type definition
     */
    public ReferenceDefinition getDefinition() {
        return definition;
    }

    /**
     * Returns the wires for the reference.
     *
     * @return the wires for the reference
     */
    public Set<LogicalWire> getWires() {
        return getComposite().getWires(this);
    }

    /**
     * Returns the URIs of component references promoted by this reference.
     *
     * @return the URIs
     */
    public List<URI> getPromotedUris() {
        return promotedUris;
    }

    /**
     * Adds the URI of a component reference promoted by this reference.
     *
     * @param uri the promoted URI
     */
    public void addPromotedUri(URI uri) {
        promotedUris.add(uri);
    }

    /**
     * Sets the  URI of the reference promoted by this reference at the given index
     *
     * @param index the index
     * @param uri   the  URI
     */
    public void setPromotedUri(int index, URI uri) {
        promotedUris.set(index, uri);
    }

    /**
     * Returns true if this reference's target (or targets) has been resolved.
     *
     * @return true if this reference's target (or targets) has been resolved
     */
    public boolean isResolved() {
        return resolved;
    }

    /**
     * Sets if this reference's target (or targets) has been resolved.
     *
     * @param resolved true if resolved.
     */
    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    /**
     * Returns the intents declared on the SCA artifact.
     *
     * @return the intents declared on the SCA artifact
     */
    public Set<QName> getIntents() {
        return definition.getIntents();
    }

    /**
     * Sets the intents declared on the SCA artifact.
     *
     * @param intents Intents declared on the SCA artifact
     */
    public void setIntents(Set<QName> intents) {
        definition.setIntents(intents);
    }

    /**
     * Adds intents to the definition.
     *
     * @param intents the intents
     */
    public void addIntents(Set<QName> intents) {
        definition.addIntents(intents);
    }

    /**
     * Returns policy sets declared on the SCA artifact.
     *
     * @return policy sets declared on the SCA artifact
     */
    public Set<QName> getPolicySets() {
        return definition.getPolicySets();
    }

    /**
     * Sets policy sets declared on the SCA artifact.
     *
     * @param policySets the policy sets
     */
    public void setPolicySets(Set<QName> policySets) {
        definition.setPolicySets(policySets);
    }

    /**
     * Sets policy sets declared on the SCA artifact.
     *
     * @param policySets the policy sets.
     */
    public void addPolicySets(Set<QName> policySets) {
        definition.addPolicySets(policySets);
    }

    /**
     * Gets the explicit referenceassociated with this logical reference.
     *
     * @return Component reference if defined, otherwise null.
     */
    public ComponentReference getComponentReference() {
        return getParent().getDefinition().getReferences().get(getDefinition().getName());
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }

        LogicalReference test = (LogicalReference) obj;
        return getUri().equals(test.getUri());

    }

    @Override
    public int hashCode() {
        return getUri().hashCode();
    }


    private LogicalCompositeComponent getComposite() {

        LogicalComponent<?> parent = getParent();
        LogicalCompositeComponent composite = parent.getParent();

        return composite != null ? composite : (LogicalCompositeComponent) parent;

    }

}
