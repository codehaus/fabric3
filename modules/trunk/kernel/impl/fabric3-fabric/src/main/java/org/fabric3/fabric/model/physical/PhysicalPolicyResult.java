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
package org.fabric3.fabric.model.physical;

import java.util.Collections;
import java.util.Set;

import org.fabric3.scdl.definitions.Intent;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.policy.registry.PolicyResult;
import org.fabric3.util.closure.Closure;
import org.fabric3.util.closure.CollectionUtils;
import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
public class PhysicalPolicyResult {
    
    private static Closure<PolicyResult, Element> TRANSFORMER = new Closure<PolicyResult, Element>() {
        public Element execute(PolicyResult object) {
            return object.getPolicyDefinition();
        }
    };
    
    private final Set<Intent> intentsProvidedBySource;
    private final Set<Intent> intentsProvidedByTarget;
    private final Set<PolicyResult> policiesProvidedBySource;
    private final Set<PolicyResult> policiesProvidedByTarget;
    private final PhysicalWireDefinition physicalWireDefinition;
    
    public PhysicalPolicyResult(Set<Intent> intentsProvidedBySource, 
                                Set<Intent> intentsProvidedByTarget,
                                Set<PolicyResult> policiesProvidedBySource, 
                                Set<PolicyResult> policiesProvidedByTarget,
                                PhysicalWireDefinition physicalWireDefinition) {
        this.intentsProvidedBySource = intentsProvidedBySource;
        this.intentsProvidedByTarget = intentsProvidedByTarget;
        this.policiesProvidedBySource = policiesProvidedBySource;
        this.policiesProvidedByTarget = policiesProvidedByTarget;
        this.physicalWireDefinition = physicalWireDefinition;
    }

    public Set<Intent> getIntentsProvidedBySource() {
        return Collections.unmodifiableSet(intentsProvidedBySource);
    }

    public Set<Intent> getIntentsProvidedByTarget() {
        return Collections.unmodifiableSet(intentsProvidedByTarget);
    }

    public Set<PolicyResult> getPoliciesProvidedBySource() {
        return Collections.unmodifiableSet(policiesProvidedBySource);
    }

    public Set<PolicyResult> getPoliciesProvidedByTarget() {
        return Collections.unmodifiableSet(policiesProvidedByTarget);
    }

    public Set<Element> getPolicyDefsProvidedBySource() {
        return CollectionUtils.transform(policiesProvidedBySource, TRANSFORMER);
    }

    public Set<Element> getPolicyDefsProvidedByTarget() {
        return CollectionUtils.transform(policiesProvidedByTarget, TRANSFORMER);
    }

    public PhysicalWireDefinition getPhysicalWireDefinition() {
        return physicalWireDefinition;
    }

}
