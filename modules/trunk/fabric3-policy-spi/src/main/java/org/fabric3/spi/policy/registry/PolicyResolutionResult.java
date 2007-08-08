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
package org.fabric3.spi.policy.registry;

import java.util.Map;

import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySetExtension;

/**
 * Result of resolving all the intents.
 * 
 * @version $Revision$ $Date$
 */
public interface PolicyResolutionResult {
    
    /**
     * Return a map of resolved policy to a flag indicating 
     * whether the policy was expilictly requested by the user or
     * impliitly mapped from the intents requested by the user.
     * 
     * @return A map of resolved policies.
     */
    Map<PolicySetExtension, Boolean> getResolvedPolicies();
    
    /**
     * Return a map of intents that were not mapped to policies. These 
     * intents are either implictly supported by the binding or 
     * implementation type using the <code>alwaysProvide</cde> attribute 
     * or needs to be ecplicitly povided by them based on the intents 
     * declared in the <code>mayProvide</code> attribute.
     * 
     * @return A map of intents that were not mapped to policies, but 
     * provided by the binding or implementation type.
     */
    Map<Intent, Boolean> getProvidedIntents();

}
