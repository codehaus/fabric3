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
package org.fabric3.fabric.policy.registry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.fabric3.spi.model.instance.LogicalScaArtifact;
import org.fabric3.spi.policy.model.Intent;
import org.fabric3.spi.policy.model.PolicySet;
import org.fabric3.spi.policy.registry.PolicyRegistry;

/**
 *
 * @version $Revision$ $Date$
 */
public class DefaultPolicyRegistry implements PolicyRegistry {

    private Set<PolicySet> policySets = new HashSet<PolicySet>();
    private Map<QName, Intent> intents = new HashMap<QName, Intent>();
    
    /**
     * @see org.fabric3.spi.policy.registry.PolicyRegistry#getInterceptors(org.fabric3.spi.model.instance.LogicalScaArtifact)
     */
    public Set<QName> getInterceptorBuilders(final LogicalScaArtifact<?> scaArtifact) {
        
        LogicalScaArtifact<?> temp = scaArtifact;
        
        Set<QName> intentNames = new HashSet<QName>();
        while(temp != null) {
            intentNames.addAll(scaArtifact.getIntents());
            temp = temp.getParent();
        }
        
        for(QName intentName : intentNames) {
            Intent intent = intents.get(intentName);
            if(intent == null || !intent.doesConstrain(scaArtifact.getType())) {
                intentNames.remove(intentName);
            }
        }
        
        for(PolicySet policySet : policySets) {
            if(policySet.doesProvide(intentNames)) {
                return policySet.getInterceptorBuilders();
            }
        }

        
        return null;
        
    }

    /**
     * @see org.fabric3.spi.policy.registry.PolicyRegistry#registerIntent(org.fabric3.spi.policy.model.Intent)
     */
    public void registerIntent(Intent intent) {
        intents.put(intent.getName(), intent);
    }

    /**
     * @see org.fabric3.spi.policy.registry.PolicyRegistry#registerPolicySet(org.fabric3.spi.policy.model.PolicySet)
     */
    public void registerPolicySet(PolicySet policySet) {
        policySets.add(policySet);
    }

}
