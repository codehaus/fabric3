/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
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
 * @version $Rev$ $Date$
 */
public class PolicyImpl implements Policy {

    private final Map<LogicalOperation, List<Intent>> intentMap = new HashMap<LogicalOperation, List<Intent>>();
    private final Map<LogicalOperation, List<PolicySet>> policySetMap = new HashMap<LogicalOperation, List<PolicySet>>();

    /**
     * Intents that are provided by the binding or implemenenation for all operations.
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
     * Policy sets that are provided by the binding or implemenenation for all operations.
     *
     * @return Resolved policy sets that are provided.
     */
    public Map<LogicalOperation, List<PolicySet>> getProvidedPolicySets() {
        return policySetMap;
    }

    /**
     * Gets the intents that are provided by the component or binding types that are requested by the operation.
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
     * Gets all the policy sets that are provided by the component implementation or binding type that were resolved against the intents requested
     * against the operation.
     *
     * @param operation Operation against which the intent was requested.
     * @return Resolved policy sets.
     */
    public List<PolicySet> getProvidedPolicySets(LogicalOperation operation) {
        return policySetMap.get(operation);
    }

    /**
     * Adds an intent that is requested on the operation and provided by the component implementation or binding type.
     *
     * @param operation Operation against which the intent was requested.
     * @param intents   Intents that are provided.
     */
    void addIntents(LogicalOperation operation, Set<Intent> intents) {

        if (!intentMap.containsKey(operation)) {
            intentMap.put(operation, new ArrayList<Intent>());
        }

        intentMap.get(operation).addAll(intents);

    }

    /**
     * Adds a policy set mapped to the intent that is requested on the operation and provided by the component implementation or binding type.
     *
     * @param operation  Operation against which the intent was requested.
     * @param policySets Resolved policy sets.
     */
    void addPolicySets(LogicalOperation operation, Set<PolicySet> policySets) {

        if (!policySetMap.containsKey(operation)) {
            policySetMap.put(operation, new ArrayList<PolicySet>());
        }

        policySetMap.get(operation).addAll(policySets);
    }

}
