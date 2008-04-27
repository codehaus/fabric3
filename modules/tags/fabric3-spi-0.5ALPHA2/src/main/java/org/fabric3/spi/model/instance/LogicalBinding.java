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

import java.util.Set;

import javax.xml.namespace.QName;

import org.fabric3.scdl.BindingDefinition;
import org.osoa.sca.Constants;

/**
 * Represents a resolved binding
 *
 * @version $Rev$ $Date$
 */
public class LogicalBinding<BD extends BindingDefinition> extends LogicalScaArtifact<Bindable> {
    
    private static final QName TYPE = new QName(Constants.SCA_NS, "binding");
    
    private final BD binding;
    private boolean provisioned;

    /**
     * @param binding
     * @param parent
     */
    public LogicalBinding(BD binding, Bindable parent) {
        super(null, parent, TYPE);
        this.binding = binding;
    }

    /**
     * Returns the binding definition.
     *
     * @return the binding definition
     */
    public BD getBinding() {
        return binding;
    }

    /**
     * @return Intents declared on the SCA artifact.
     */
    public Set<QName> getIntents() {
        return binding.getIntents();
    }
    
    /**
     * @param intents Intents declared on the SCA artifact.
     */
    public void setIntents(Set<QName> intents) {
        binding.setIntents(intents);
    }

    /**
     * @return Policy sets declared on the SCA artifact.
     */
    public Set<QName> getPolicySets() {
        return binding.getPolicySets();
    }

    /**
     * @param policySets Policy sets declared on the SCA artifact.
     */
    public void setPolicySets(Set<QName> policySets) {
        binding.setPolicySets(policySets);
    }

    public boolean isProvisioned() {
        return provisioned;
    }

    public void setProvisioned(boolean provisioned) {
        this.provisioned = provisioned;
    }
}
