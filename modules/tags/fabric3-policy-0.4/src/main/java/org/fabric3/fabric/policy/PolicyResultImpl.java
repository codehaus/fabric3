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
import org.fabric3.spi.policy.PolicyResult;

/**
 * 
 * @version $Revision$ $Date$
 */
public class PolicyResultImpl implements PolicyResult {
    
    private final PolicyImpl sourcePolicy = new PolicyImpl();
    private final PolicyImpl targetPolicy = new PolicyImpl();
    
    private final Map<Operation<?>, Set<PolicySet>> interceptedPolicySets = new HashMap<Operation<?>, Set<PolicySet>>();
    
    /**
     * @return Policies and intents provided at the source end.
     */
    public Policy getSourcePolicy() {
        return sourcePolicy;
    }
    
    /**
     * @return Policies and intents provided at the target end.
     */
    public Policy getTargetPolicy() {
        return targetPolicy;
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
    
    /**
     * Adds an intent that is requested on the operation and provided by the source 
     * component implementation or binding type.
     * 
     * @param operation Operation against which the intent was requested.
     * @param intents Intents that are provided.
     */
    void addSourceIntents(Operation<?> operation, Set<Intent> intents) {
        sourcePolicy.addIntents(operation, intents);
    }
    
    /**
     * Adds an intent that is requested on the operation and provided by the target 
     * component implementation or binding type.
     * 
     * @param operation Operation against which the intent was requested.
     * @param intents Intents that are provided.
     */
    void addTargetIntents(Operation<?> operation, Set<Intent> intents) {
        targetPolicy.addIntents(operation, intents);
    }
    
    /**
     * Adds a policy set mapped to the inetnt that is requested on the operation 
     * and provided by the source component implementation or binding type.
     * 
     * @param operation Operation against which the intent was requested.
     * @param policySets Resolved policy sets.
     */
    void addSourcePolicySets(Operation<?> operation, Set<PolicySet> policySets) {
        sourcePolicy.addPolicySets(operation, policySets);
    }
    
    /**
     * Adds a policy set mapped to the inetnt that is requested on the operation 
     * and provided by the target component implementation or binding type.
     * 
     * @param operation Operation against which the intent was requested.
     * @param policySets Resolved policy sets.
     */
    void addTargetPolicySets(Operation<?> operation, Set<PolicySet> policySets) {
        targetPolicy.addPolicySets(operation, policySets);
    }
    
    /**
     * Adds a policy set mapped to the intent that is requested on the operation 
     * and is implemented as an interceptor.
     * 
     * @param operation Operation against which the intent was requested.
     * @param policySets Resolved policy sets.
     */
    void addInterceptedPolicySets(Operation<?> operation, Set<PolicySet> policySets) {
        
        if (!interceptedPolicySets.containsKey(operation)) {
            interceptedPolicySets.put(operation, new HashSet<PolicySet>());
        }
        
        interceptedPolicySets.get(operation).addAll(policySets);
        
    }

}
