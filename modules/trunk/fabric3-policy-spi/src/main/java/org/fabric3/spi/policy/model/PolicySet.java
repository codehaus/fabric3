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
package org.fabric3.spi.policy.model;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.fabric3.spi.model.type.ModelObject;

/**
 * Model object that represents a policy set.
 *
 * @version $Revision$ $Date$
 */
public class PolicySet extends ModelObject {
    
    /** Qualified name of the policy set. */
    private QName name;
    
    /** SCA artifacts to which this policy set applies. */
    private Set<QName> appliesTo = new HashSet<QName>();
    
    /** Intents provided by this policy set. */
    private Set<QName> provides = new HashSet<QName>();

    /**
     * @return Qualified name of the intent.
     */
    public QName getName() {
        return name;
    }

    /**
     * @param name Qualified name of the intent.
     */
    public void setQname(QName name) {
        this.name = name;
    }

    /**
     * @return SCA artifacts to which this policy set applies.
     */
    public Set<QName> getAppliesTo() {
        return appliesTo;
    }

    /**
     * @param appliesTo SCA artifacts to which this policy set applies.
     */
    public void setAppliesTo(Set<QName> appliesTo) {
        this.appliesTo = appliesTo;
    }

    /**
     * @return Intents provided by this policy set.
     */
    public Set<QName> getProvides() {
        return provides;
    }

    /**
     * @param provides Intents provided by this policy set.
     */
    public void setProvides(Set<QName> provides) {
        this.provides = provides;
    }
    
    /**
     * Checks whether this policy set applies to the specified artifact.
     * @param scaArtifact SCA artifact that needs to be checked.
     * @return True if this policy set applies to the specified SCA artifact.
     */
    public boolean doesApplyTo(QName scaArtifact) {
        return appliesTo.contains(scaArtifact);
    }
    
    /**
     * Checks whether the specified intent is provided by this policy set.
     * @param intent Intent that needs to be checked.
     * @return True if this policy set provides to the specified intent.
     */
    public boolean doesProvide(QName intent) {
        return provides.contains(intent);
    }

}
