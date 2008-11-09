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

import java.util.Set;
import javax.xml.namespace.QName;

import org.osoa.sca.Constants;

import org.fabric3.scdl.BindingDefinition;

/**
 * Represents a resolved binding
 *
 * @version $Rev$ $Date$
 */
public class LogicalBinding<BD extends BindingDefinition> extends LogicalScaArtifact<Bindable> {
    private static final long serialVersionUID = 8153501808553226042L;

    private static final QName TYPE = new QName(Constants.SCA_NS, "binding");

    private final BD definition;
    private LogicalState state = LogicalState.NEW;

    /**
     * @param definition
     * @param parent
     */
    public LogicalBinding(BD definition, Bindable parent) {
        super(null, parent, TYPE);
        this.definition = definition;
    }

    /**
     * Returns the binding definition.
     *
     * @return the binding definition
     */
    public BD getDefinition() {
        return definition;
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

    /**
     * Returns the binding state.
     *
     * @return the binding state
     */
    public LogicalState getState() {
        return state;
    }

    /**
     * Sets the binding state.
     *
     * @param state the binding state
     */
    public void setState(LogicalState state) {
        this.state = state;
    }

}
