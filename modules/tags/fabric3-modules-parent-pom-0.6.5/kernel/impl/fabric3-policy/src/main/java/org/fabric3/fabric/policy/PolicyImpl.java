/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.fabric.policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    
    private final Map<Operation<?>, List<Intent>> intentMap = new HashMap<Operation<?>, List<Intent>>();
    private final Map<Operation<?>, List<PolicySet>> policySetMap = new HashMap<Operation<?>, List<PolicySet>>();
    
    /**
     * Gets the intents that are provided by the component or binding types 
     * that are requested by the operation.
     * 
     * @param operation Operation against which the intent was requested.
     * @return All intents that are provided.
     */
    public List<Intent> getProvidedIntents(Operation<?> operation) {
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
    public List<PolicySet> getProvidedPolicySets(Operation<?> operation) {
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
            intentMap.put(operation, new ArrayList<Intent>());
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
            policySetMap.put(operation, new ArrayList<PolicySet>());
        }
        
        policySetMap.get(operation).addAll(policySets);
    }

}
