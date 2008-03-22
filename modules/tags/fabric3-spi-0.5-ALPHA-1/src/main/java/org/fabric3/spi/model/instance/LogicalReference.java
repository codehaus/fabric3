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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.fabric3.scdl.ComponentReference;
import org.fabric3.scdl.ReferenceDefinition;
import org.osoa.sca.Constants;

/**
 * Represents a resolved reference
 *
 * @version $Rev$ $Date$
 */
public class LogicalReference extends Bindable {

    private static final QName TYPE = new QName(Constants.SCA_NS, "reference");
    
    private final ReferenceDefinition definition;
    private List<URI> promotedUris;

    /**
     * @param uri
     * @param definition
     * @param parent
     */
    public LogicalReference(URI uri, ReferenceDefinition definition, LogicalComponent<?> parent) {
        super(uri, parent, TYPE);
        this.definition = definition;
        promotedUris = new ArrayList<URI>();
    }

    /**
     * @return
     */
    public ReferenceDefinition getDefinition() {
        return definition;
    }

    /**
     * @return
     */
    public Set<LogicalWire> getWires() {
        return getComposite().getWires(this);
    }

    /**
     * @param uri
     */
    public void addTargetUri(URI uri) {
        getComposite().addWire(this, new LogicalWire(getComposite(), this, uri));
    }

    /**
     * @param targetUris
     */
    public void overrideTargets(List<URI> targetUris) {
        
        Set<LogicalWire> logicalWires = new LinkedHashSet<LogicalWire>();
        for (URI targetUri : targetUris) {
            logicalWires.add(new LogicalWire(getComposite(), this, targetUri));
        }
        getComposite().overrideWires(this, logicalWires);
    }

    /**
     * @return
     */
    public List<URI> getPromotedUris() {
        return Collections.unmodifiableList(promotedUris);
    }

    /**
     * @param uri
     */
    public void addPromotedUri(URI uri) {
        promotedUris.add(uri);
    }
    
    /**
     * @param index
     * @param uri
     */
    public void setPromotedUri(int index, URI uri) {
        promotedUris.set(index, uri);
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
    
    /**
     * Gets the explicit referenceassociated with this logical reference.
     * @return Component reference if defined, otherwise null.
     */
    public ComponentReference getComponentReference() {
        return getParent().getDefinition().getReferences().get(getDefinition().getName());
    }
    
    private LogicalCompositeComponent getComposite() {
        
        LogicalComponent<?> parent = getParent();
        LogicalCompositeComponent composite = parent.getParent();
        
        return composite != null ? composite : (LogicalCompositeComponent) parent;
        
    }
    
}
