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
package org.fabric3.spi.policy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fabric3.scdl.Operation;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;

/**
 * Result of resolving intents and policy sets on a wire. The policies are resolved for 
 * the source and target bindings as well as the source and target component types. A wire 
 * can be between two components or between a component and a binding. 
 * 
 * For a wire between two components, the result will include,
 * 
 * 1. Implementation intents that are requested for each operation on the source side and may be 
 * provided by the source component implementation type.
 * 2. Implementation intents that are requested for each operation on the target side and may be 
 * provided by the target component implementation type.
 * 3. Policy sets that map to implementation intents on each operation on the source side and 
 * understood by the source component implementation type.
 * 4. Policy sets that map to implementation intents on each operation on the target side and 
 * understood by the target component implementation type.
 * 5. Policy sets that map to implementation intents on each operation on the source and target 
 * side that are implemented using interceptors.
 * 
 * For a wire between a binding and a component (service binding), the result will include
 * 
 * 1. Interaction intents that are requested for each operation and may be provided by the 
 * service binding type.
 * 2. Implementation intents that are requested for each operation and may be  provided by 
 * the target component implementation type.
 * 3. Policy sets that map to implementation intents on each operation and understood by the 
 * component implementation type.
 * 4. Policy sets that map to interaction intents on each operation on the source side and 
 * understood by the service binding type.
 * 5. Policy sets that map to implementation and interaction intents on each operation that 
 * are implemented using interceptors.
 * 
 * For a wire between a component and a binding (reference binding), the result will include
 * 
 * 1. Interaction intents that are requested for each operation and may be provided by the 
 * reference binding type.
 * 2. Implementation intents that are requested for each operation and may be provided by the 
 * component implementation type.
 * 3. Policy sets that map to implementation intents on each operation and understood by the 
 * component implementation type.
 * 4. Policy sets that map to interaction intents on each operation and understood by the 
 * service binding type.
 * 5. Policy sets that map to implementation and interaction intents on each operation that 
 * are implemented using interceptors.
 * 
 * @version $Revision$ $Date$
 */
public class PolicyResult {
    
    private final Map<Operation<?>, Set<Intent>> sourceIntents = new HashMap<Operation<?>, Set<Intent>>();
    private final Map<Operation<?>, Set<Intent>> targetIntents = new HashMap<Operation<?>, Set<Intent>>();
    
    private final Map<Operation<?>, Set<PolicySet>> sourcePolicySets = new HashMap<Operation<?>, Set<PolicySet>>();
    private final Map<Operation<?>, Set<PolicySet>> targetPolicySets = new HashMap<Operation<?>, Set<PolicySet>>();
    
    private final Map<Operation<?>, Set<PolicySet>> interceptedPolicySets = new HashMap<Operation<?>, Set<PolicySet>>();
    
    /**
     * Adds an intent that is requested on the operation and provided by the source 
     * component implementation or binding type.
     * 
     * @param operation Operation against which the intent was requested.
     * @param intents Intents that are provided.
     */
    public void addSourceIntent(Operation<?> operation, Set<Intent> intents) {
        addIntents(sourceIntents, operation, intents);
    }
    
    /**
     * Gets the intents that are provided by the source component or binding types 
     * that are requested by the operation.
     * 
     * @param operation Operation against which the intent was requested.
     * @return All intents that are provided.
     */
    public Set<Intent> getSourceIntents(Operation<?> operation) {
        return sourceIntents.get(operation);
    }
    
    /**
     * Adds an intent that is requested on the operation and provided by the target 
     * component implementation or binding type.
     * 
     * @param operation Operation against which the intent was requested.
     * @param intents Intents that are provided.
     */
    public void addTargetIntent(Operation<?> operation, Set<Intent> intents) {
        addIntents(targetIntents, operation, intents);
    }
    
    /**
     * Gets the intents that are provided by the target component or binding types 
     * that are requested by the operation.
     * 
     * @param operation Operation against which the intent was requested.
     * @return All intents that are provided.
     */
    public Set<Intent> getTargetIntents(Operation<?> operation) {
        return targetIntents.get(operation);
    }
    
    /**
     * Adds a policy set mapped to the inetnt that is requested on the operation 
     * and provided by the source component implementation or binding type.
     * 
     * @param operation Operation against which the intent was requested.
     * @param policySets Resolved policy sets.
     */
    public void addSourcePolicySet(Operation<?> operation, Set<PolicySet> policySets) {
        addPolicySets(sourcePolicySets, operation, policySets);
    }
    
    /**
     * Gets all the policy sets that are provided by the source component 
     * implementation or binding type that were resolved against the intents 
     * requested against the operation.
     * 
     * @param operation Operation against which the intent was requested.
     * @return Resolved policy sets.
     */
    public Set<PolicySet> getSourcePolicySets(Operation<?> operation) {
        return sourcePolicySets.get(operation);
    }
    
    /**
     * Adds a policy set mapped to the inetnt that is requested on the operation 
     * and provided by the target component implementation or binding type.
     * 
     * @param operation Operation against which the intent was requested.
     * @param policySets Resolved policy sets.
     */
    public void addTargetPolicySet(Operation<?> operation, Set<PolicySet> policySets) {
        addPolicySets(targetPolicySets, operation, policySets);
    }
    
    /**
     * Gets all the policy sets that are provided by the target component 
     * implementation or binding type that were resolved against the intents 
     * requested against the operation.
     * 
     * @param operation Operation against which the intent was requested.
     * @return Resolved policy sets.
     */
    public Set<PolicySet> getTargetPolicySets(Operation<?> operation) {
        return targetPolicySets.get(operation);
    }
    
    /**
     * Adds a policy set mapped to the intent that is requested on the operation 
     * and is implemented as an interceptor.
     * 
     * @param operation Operation against which the intent was requested.
     * @param policySets Resolved policy sets.
     */
    public void addInterceptedPolicySet(Operation<?> operation, Set<PolicySet> policySets) {
        addPolicySets(interceptedPolicySets, operation, policySets);
    }
    
    /**
     * Gets all the policy sets that are implemented as interceptors that were 
     * resolved against the intents requested against the operation.
     * 
     * @param operation Operation against which the intent was requested.
     * @return Resolved policy sets.
     */
    public Set<PolicySet> getInterceptedPolicySets(Operation<?> operation) {
        return interceptedPolicySets.get(operation);
    }
    
    /*
     * Adds an intent to an intent map.
     */
    private void addIntents(Map<Operation<?>, Set<Intent>> intentMap, Operation<?> operation, Set<Intent> intents) {
        
        if (!intentMap.containsKey(operation)) {
            intentMap.put(operation, new HashSet<Intent>());
        }
        
        intentMap.get(operation).addAll(intents);
        
    }
    
    /*
     * Adds a policy set to a policy set map.
     */
    private void addPolicySets(Map<Operation<?>, Set<PolicySet>> policySetMap, Operation<?> operation, Set<PolicySet> policySet) {
        
        if (!policySetMap.containsKey(operation)) {
            policySetMap.put(operation, new HashSet<PolicySet>());
        }
        
        policySetMap.get(operation).addAll(policySet);
        
    }

}
