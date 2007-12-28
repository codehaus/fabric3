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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fabric3.scdl.Operation;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;

/**
 * @version $Revision$ $Date$
 */
public class PolicyResult {
    
    private final Set<Intent> sourceIntents = new HashSet<Intent>();
    private final Set<Intent> targetIntents = new HashSet<Intent>();
    private final Set<PolicySet> sourcePolicies = new HashSet<PolicySet>();
    private final Set<PolicySet> targetPolicies = new HashSet<PolicySet>();
    private final Map<Operation<?>, Set<PolicySet>> interceptedPolicies = new HashMap<Operation<?>, Set<PolicySet>>();
    
    public PolicyResult() {
    }
    
    public PolicyResult(Set<Intent> sourceIntents, 
                        Set<Intent> targetIntents,
                        Set<PolicySet> sourcePolicies, 
                        Set<PolicySet> targetPolicies,
                        Map<Operation<?>, Set<PolicySet>> interceptedPolicies) {
        
        this.sourceIntents.addAll(sourceIntents);
        this.targetIntents.addAll(targetIntents);
        
        this.sourcePolicies.addAll(sourcePolicies);
        this.targetPolicies.addAll(targetPolicies);
        
        this.interceptedPolicies.putAll(interceptedPolicies);
        
    }

    public Set<Intent> getSourceIntents() {
        return Collections.unmodifiableSet(sourceIntents);
    }

    public Set<Intent> getTargetIntents() {
        return Collections.unmodifiableSet(targetIntents);
    }

    public Set<PolicySet> getSourcePolicies() {
        return sourcePolicies;
    }

    public Set<PolicySet> getTargetPolicies() {
        return targetPolicies;
    }

    public Map<Operation<?>, Set<PolicySet>> getInterceptedPolicies() {
        return Collections.unmodifiableMap(interceptedPolicies);
    }

}
