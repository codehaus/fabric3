/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.fabric.policy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fabric3.scdl.Operation;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.policy.Policy;

/**
 * 
 * @version $Revision$ $Date$
 */
public class PolicyImpl implements Policy {
    
    private final Map<Operation<?>, Set<Intent>> intentMap = new HashMap<Operation<?>, Set<Intent>>();
    private final Map<Operation<?>, Set<PolicySet>> policySetMap = new HashMap<Operation<?>, Set<PolicySet>>();
    
    /**
     * Gets the intents that are provided by the component or binding types 
     * that are requested by the operation.
     * 
     * @param operation Operation against which the intent was requested.
     * @return All intents that are provided.
     */
    public Set<Intent> getProvidedIntents(Operation<?> operation) {
        return intentMap.get(operation);
    }
    
    /**
     * Gets all the policy sets that are provided by the component 
     * implementation or binding type that were resolved against the intents 
     * requested against the operation.
     * 
     * @param operation Operation against which the intent was requested.
     * @return Resolved policy sets.
     */
    public Set<PolicySet> getProvidedPolicySets(Operation<?> operation) {
        return policySetMap.get(operation);
    }
    
    /**
     * Adds an intent that is requested on the operation and provided by the  
     * component implementation or binding type.
     * 
     * @param operation Operation against which the intent was requested.
     * @param intents Intents that are provided.
     */
    void addIntents(Operation<?> operation, Set<Intent> intents) {
        
        if (!intentMap.containsKey(operation)) {
            intentMap.put(operation, new HashSet<Intent>());
        }
        
        intentMap.get(operation).addAll(intents);
        
    }
    
    /**
     * Adds a policy set mapped to the intent that is requested on the operation 
     * and provided by the component implementation or binding type.
     * 
     * @param operation Operation against which the intent was requested.
     * @param policySets Resolved policy sets.
     */
    void addPolicySets(Operation<?> operation, Set<PolicySet> policySets) {
        
        if (!policySetMap.containsKey(operation)) {
            policySetMap.put(operation, new HashSet<PolicySet>());
        }
        
        policySetMap.get(operation).addAll(policySets);
    }

}
