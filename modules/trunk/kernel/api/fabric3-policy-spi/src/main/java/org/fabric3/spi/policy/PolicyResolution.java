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
import org.fabric3.util.closure.Closure;
import org.fabric3.util.closure.CollectionUtils;
import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
public class PolicyResolution {
    
    private static Closure<PolicySet, Element> TRANSFORMER = new Closure<PolicySet, Element>() {
        public Element execute(PolicySet object) {
            return object.getExtension();
        }
    };
    
    private final Set<Intent> intentsProvidedBySource = new HashSet<Intent>();
    private final Set<Intent> intentsProvidedByTarget = new HashSet<Intent>();
    private final Set<Element> policiesProvidedBySource = new HashSet<Element>();
    private final Set<Element> policiesProvidedByTarget = new HashSet<Element>();
    private final Map<Operation<?>, Set<Element>> interceptedPolicies = new HashMap<Operation<?>, Set<Element>>();
    
    public PolicyResolution() {
    }
    
    public PolicyResolution(Set<Intent> intentsProvidedBySource, 
                            Set<Intent> intentsProvidedByTarget,
                            Set<PolicySet> policiesProvidedBySource, 
                            Set<PolicySet> policiesProvidedByTarget,
                            Map<Operation<?>, Set<PolicySet>> interceptedPolicies) {
        
        this.intentsProvidedBySource.addAll(intentsProvidedBySource);
        this.intentsProvidedByTarget.addAll(intentsProvidedByTarget);
        
        this.policiesProvidedBySource.addAll(CollectionUtils.transform(policiesProvidedBySource, TRANSFORMER));
        this.policiesProvidedByTarget.addAll(CollectionUtils.transform(policiesProvidedByTarget, TRANSFORMER));
        
        for (Map.Entry<Operation<?>, Set<PolicySet>> policy : interceptedPolicies.entrySet()) {
            this.interceptedPolicies.put(policy.getKey(), CollectionUtils.transform(policy.getValue(), TRANSFORMER));
        }
        
    }

    public Set<Intent> getSourceIntents() {
        return Collections.unmodifiableSet(intentsProvidedBySource);
    }

    public Set<Intent> getTargetIntents() {
        return Collections.unmodifiableSet(intentsProvidedByTarget);
    }

    public Set<Element> getSourcePolicies() {
        return policiesProvidedBySource;
    }

    public Set<Element> getTargetPolicies() {
        return policiesProvidedByTarget;
    }

    public Map<Operation<?>, Set<Element>> getInterceptedPolicies() {
        return Collections.unmodifiableMap(interceptedPolicies);
    }

}
