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
 * Represents a registered intent within the domain.
 * 
 * @version $Revision$ $Date$
 *
 */
public class Intent extends ModelObject {
    
    /** Qualified name of the intent. */
    private QName name;
    
    /** Description of the intent. */
    private String description;
    
    /** SCA artifacts constrained by this intent. */
    private Set<QName> constrainedArtifacts = new HashSet<QName>();

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
     * @return Intent description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description Intent description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return SCA artifacts constrained by this intent.
     */
    public Set<QName> getConstrainedArtifacts() {
        return constrainedArtifacts;
    }

    /**
     * @param constrainedArtifacts SCA artifacts constrained by this intent.
     */
    public void setConstrainedArtifacts(Set<QName> constrainedArtifacts) {
        this.constrainedArtifacts = constrainedArtifacts;
    }
    
    /**
     * Checks whether the specified artifact can be constrained by this intent.
     * @param scaArtifact SCA artifact that needs to be checked.
     * @return True if this intent can constrain the specified SCA artifact.
     */
    public boolean doesConstrain(QName scaArtifact) {
        return constrainedArtifacts.contains(scaArtifact);
    }

}
