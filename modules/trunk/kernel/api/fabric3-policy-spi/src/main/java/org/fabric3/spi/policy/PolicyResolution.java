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
import org.fabric3.util.closure.Closure;
import org.fabric3.util.closure.CollectionUtils;
import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
public class PolicyResolution {
    
    private static Closure<PolicyResult, Element> TRANSFORMER = new Closure<PolicyResult, Element>() {
        public Element execute(PolicyResult object) {
            return object.getPolicyDefinition();
        }
    };
    
    private final Set<Intent> intentsProvidedBySource = new HashSet<Intent>();
    private final Set<Intent> intentsProvidedByTarget = new HashSet<Intent>();
    private final Set<PolicyResult> policiesProvidedBySource = new HashSet<PolicyResult>();
    private final Set<PolicyResult> policiesProvidedByTarget = new HashSet<PolicyResult>();
    private final Map<Operation<?>, Set<PolicyResult>> interceptedPolicies = 
        new HashMap<Operation<?>, Set<PolicyResult>>();
    
    public PolicyResolution() {
    }
    
    public PolicyResolution(Set<Intent> intentsProvidedBySource, 
                            Set<Intent> intentsProvidedByTarget,
                            Set<PolicyResult> policiesProvidedBySource, 
                            Set<PolicyResult> policiesProvidedByTarget,
                            Map<Operation<?>, Set<PolicyResult>> interceptedPolicies) {
        this.intentsProvidedBySource.addAll(intentsProvidedBySource);
        this.intentsProvidedByTarget.addAll(intentsProvidedByTarget);
        this.policiesProvidedBySource.addAll(policiesProvidedBySource);
        this.policiesProvidedByTarget.addAll(policiesProvidedByTarget);
        this.interceptedPolicies.putAll(interceptedPolicies);
    }

    public Set<Intent> getSourceIntents() {
        return Collections.unmodifiableSet(intentsProvidedBySource);
    }

    public Set<Intent> getTargetIntents() {
        return Collections.unmodifiableSet(intentsProvidedByTarget);
    }

    public Set<Element> getSourcePolicies() {
        return CollectionUtils.transform(policiesProvidedBySource, TRANSFORMER);
    }

    public Set<Element> getTargetPolicies() {
        return CollectionUtils.transform(policiesProvidedByTarget, TRANSFORMER);
    }

    public Map<Operation<?>, Set<PolicyResult>> getInterceptedPolicies() {
        return Collections.unmodifiableMap(interceptedPolicies);
    }

}
