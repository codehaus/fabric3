/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.scdl.definitions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.fabric3.scdl.ModelObject;

/**
 * @version $Revision$ $Date$
 *
 */
public class Definitions extends ModelObject {
    
    private Set<PolicySet> policySets = new HashSet<PolicySet>();
    private Set<Intent> intents = new HashSet<Intent>();
    
    /**
     * @return Policy sets in this definition.
     */
    public Set<PolicySet> getPolicySets() {
        return Collections.unmodifiableSet(policySets);
    }
    
    /**
     * @return Intents in this definition.
     */
    public Set<Intent> getIntents() {
        return Collections.unmodifiableSet(intents);
    }
    
    /**
     * @param policySet Policy set to be added.
     */
    public void addPolicySet(PolicySet policySet) {
        policySets.add(policySet);
    }
    
    /**
     * @param intent Intent set to be added.
     */
    public void addIntent(Intent intent) {
        intents.add(intent);
    }

}
