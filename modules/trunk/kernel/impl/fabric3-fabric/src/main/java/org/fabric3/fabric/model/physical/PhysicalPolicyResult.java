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
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

/**
 * @version $Revision$ $Date$
 */
public class PhysicalPolicyResult {
    
    private final Set<Intent> intentsProvidedBySource;
    private final Set<Intent> intentsProvidedByTarget;
    private final Set<PolicySet> policySetsProvidedBySource;
    private final Set<PolicySet> policySetsProvidedByTarget;
    private final PhysicalWireDefinition physicalWireDefinition;
    
    public PhysicalPolicyResult(Set<Intent> intentsProvidedBySource, 
                                Set<Intent> intentsProvidedByTarget,
                                Set<PolicySet> policySetsProvidedBySource, 
                                Set<PolicySet> policySetsProvidedByTarget,
                                PhysicalWireDefinition physicalWireDefinition) {
        this.intentsProvidedBySource = intentsProvidedBySource;
        this.intentsProvidedByTarget = intentsProvidedByTarget;
        this.policySetsProvidedBySource = policySetsProvidedBySource;
        this.policySetsProvidedByTarget = policySetsProvidedByTarget;
        this.physicalWireDefinition = physicalWireDefinition;
    }

    public Set<Intent> getIntentsProvidedBySource() {
        return Collections.unmodifiableSet(intentsProvidedBySource);
    }

    public Set<Intent> getIntentsProvidedByTarget() {
        return Collections.unmodifiableSet(intentsProvidedByTarget);
    }

    public Set<PolicySet> getPolicySetsProvidedBySource() {
        return Collections.unmodifiableSet(policySetsProvidedBySource);
    }

    public Set<PolicySet> getPolicySetsProvidedByTarget() {
        return Collections.unmodifiableSet(policySetsProvidedByTarget);
    }

    public PhysicalWireDefinition getPhysicalWireDefinition() {
        return physicalWireDefinition;
    }

}
