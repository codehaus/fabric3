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
 */
package org.fabric3.policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.policy.Policy;

/**
 * 
 * @version $Revision$ $Date$
 */
public class PolicyImpl implements Policy {
    
    private final Map<LogicalOperation, List<Intent>> intentMap = new HashMap<LogicalOperation, List<Intent>>();
    private final Map<LogicalOperation, List<PolicySet>> policySetMap = new HashMap<LogicalOperation, List<PolicySet>>();

    /**
     * Intents that are provided by the binding or implemenenation for 
     * all operations.
     * 
     * @return Requested intents that are provided.
     */
    public List<QName> getProvidedIntents() {
        List<QName> ret = new LinkedList<QName>();
        for (LogicalOperation operation : intentMap.keySet()) {
            ret.addAll(getProvidedIntents(operation));
        }
        return ret;
    }

    /**
     * Policy sets that are provided by the binding or implemenenation for 
     * all operations.
     * 
     * @return Resolved policy sets that are provided.
     */
    public List<PolicySet> getProvidedPolicySets() {
        List<PolicySet> ret = new LinkedList<PolicySet>();
        for (LogicalOperation operation : intentMap.keySet()) {
            ret.addAll(getProvidedPolicySets(operation));
        }
        return ret;
    }
    
    /**
     * Gets the intents that are provided by the component or binding types 
     * that are requested by the operation.
     * 
     * @param operation Operation against which the intent was requested.
     * @return All intents that are provided.
     */
    public List<QName> getProvidedIntents(LogicalOperation operation) {
        List<QName> ret = new LinkedList<QName>();
        for (Intent intent : intentMap.get(operation)) {
            ret.add(intent.getName());
        }
        return ret;
    }
    
    /**
     * Gets all the policy sets that are provided by the component 
     * implementation or binding type that were resolved against the intents 
     * requested against the operation.
     * 
     * @param operation Operation against which the intent was requested.
     * @return Resolved policy sets.
     */
    public List<PolicySet> getProvidedPolicySets(LogicalOperation operation) {
        return policySetMap.get(operation);
    }
    
    /**
     * Adds an intent that is requested on the operation and provided by the  
     * component implementation or binding type.
     * 
     * @param operation Operation against which the intent was requested.
     * @param intents Intents that are provided.
     */
    void addIntents(LogicalOperation operation, Set<Intent> intents) {
        
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
    void addPolicySets(LogicalOperation operation, Set<PolicySet> policySets) {
        
        if (!policySetMap.containsKey(operation)) {
            policySetMap.put(operation, new ArrayList<PolicySet>());
        }
        
        policySetMap.get(operation).addAll(policySets);
    }

}
