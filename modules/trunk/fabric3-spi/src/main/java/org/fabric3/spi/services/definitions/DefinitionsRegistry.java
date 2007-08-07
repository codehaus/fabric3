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
package org.fabric3.spi.services.definitions;

import java.util.Set;

import javax.xml.namespace.QName;

import org.fabric3.scdl.definitions.BindingType;
import org.fabric3.scdl.definitions.ImplementationType;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;

/**
 * Registry of binding types, implementation types, intents and policy sets 
 * within an SCA domain.
 * 
 * @version $Revision$ $Date$
 */
public interface DefinitionsRegistry {
    
    /**
     * @param policySet Policy set that needs to be registered.
     */
    void registerPolicySet(PolicySet policySet);
    
    /**
     * @param intent Intent that needs to be registered.
     */
    void registerIntent(Intent intent);
    
    /**
     * @param bindingType Binding type to be registered.
     */
    void registerBindingType(BindingType bindingType);
    
    /**
     * @param implementationType Implementation type to be registered.
     */
    void registerImplementationType(ImplementationType implementationType);
    
    /**
     * @return All the policy sets available in the domain.
     */
    Set<PolicySet> getAllPolicySets();
    
    /**
     * @param name Name of the policy set queried.
     * @return Queried policy set if available, otherwise null.
     */
    PolicySet getPolicySet(QName name);
    
    /**
     * @return All the policy sets available in the domain.
     */
    Set<Intent> getAllIntents();
    
    /**
     * @param name Name of the intent queried.
     * @return Queried intent if available, otherwise null.
     */
    Intent getIntent(QName name);
    
    /**
     * @return All the implementation types available in the domain.
     */
    Set<ImplementationType> getAllImplementationTypes();
    
    /**
     * @param name Name of the implementation type queried.
     * @return Implementation type if available, otherwise null.
     */
    ImplementationType getImplementationType(QName name);
    
    /**
     * @return All the binding types available in the domain.
     */
    Set<BindingType> getAllBindingTypes();
    
    /**
     * @param name Name of the binding type queried.
     * @return Binding type if available, otherwise null.
     */
    BindingType getBindingType(QName name);

}
