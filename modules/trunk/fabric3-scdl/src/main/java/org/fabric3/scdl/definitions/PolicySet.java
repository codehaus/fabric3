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
package org.fabric3.scdl.definitions;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.fabric3.scdl.ModelObject;

/**
 * Model object that represents a policy set.
 *
 * @version $Revision$ $Date$
 */
public final class PolicySet extends ModelObject {
    
    /** Qualified name of the policy set. */
    private QName name;
    
    /** Intents provided by this policy set. */
    private Set<QName> provides = new HashSet<QName>();
    
    /** Policy set extension */
    private PolicySetExtension extension;
    
    /** XPath expression for the apples to attribute. */
    private String appliesTo;

    /**
     * Initializes the state for the policy set.
     * 
     * @param name Name of the policy set.
     * @param provides Intents provided by this policy set.
     * @param interceptorBuilders Builders for the interceptors that implement this policy set.
     * @param appliesTo XPath expression for the apples to attribute.
     */
    public PolicySet(QName name, Set<QName> provides, String appliesTo) {
        this.name = name;
        this.provides.addAll(provides);
        this.appliesTo = appliesTo;
    }
    
    /**
     * Name of the policy set.
     * 
     * @return Qualified name of the policy set.
     */
    public QName getName() {
        return name;
    }
    
    /**
     * XPath expression to the element to which the policy set applies.
     * 
     * @return The apples to XPath expression.
     */
    public String getAppliesTo() {
        return appliesTo;
    }
    
    /**
     * Checks whether the specified intent is provided by this policy set.
     * 
     * @param intent Intent that needs to be checked.
     * @return True if this policy set provides to the specified intent.
     */
    public boolean doesProvide(QName intent) {
        return provides.contains(intent);
    }
    
    /**
     * Checks whether the specified intents is provided by this policy set.
     * 
     * @param intents Intents that need to be checked.
     * @return True if this policy set provides to the specified intent.
     */
    public boolean doesProvide(Set<QName> intents) {
        return provides.containsAll(intents);
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof PolicySet && ((PolicySet) other).name.equals(name);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name.toString();
    }

    /**
     * @return Extension for the policy set.
     */
    public PolicySetExtension getExtension() {
        return extension;
    }

    /**
     * @param extension Extension for the policy set.
     */
    public void setExtension(PolicySetExtension extension) {
        this.extension = extension;
    }

}
