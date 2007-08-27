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

import java.util.Set;

import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySetExtension;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Interface for the policy resolver.
 * 
 * @version $Revision$ $Date$
 */
public interface PolicyResolver {

    /**
     * Returns the set of intents that need to be explictly provided by the binding. These are the 
     * intents requested by the use and available in the <code>mayProvide</code> list of intents 
     * declared by the binding type.
     * 
     * @param logicalBinding Logical binding for which intents are to be resolved.
     * @return Set of intents that need to be explictly provided by the binding.
     * @throws PolicyResolutionException If there are any unidentified intents.
     */
    Set<Intent> getInteractionIntentsToBeProvided(LogicalBinding<?> logicalBinding) throws PolicyResolutionException;
    
    /**
     * Returns the set of intents that need to be explictly provided by the implementation. These 
     * are the intents requested by the use and available in the <code>mayProvide</code> list of intents 
     * declared by the implementation type.
     * 
     * @param logicalComponent Logical component for which intents are to be resolved.
     * @return Set of intents that need to be explictly provided by the implementation.
     * @throws PolicyResolutionException If there are any unidentified intents.
     */
    Set<Intent> getImplementationIntentsToBeProvided(LogicalComponent<?> logicalComponent) throws PolicyResolutionException;
    
    /**
     * Returns the set of policies that will address the intents that are not provided by the binding type.
     * 
     * @param binding Binding for which policies are to be resolved.
     * @return Set of resolved policies.
     * @throws PolicyResolutionException If all intents cannot be resolved.
     */
    Set<PolicySetExtension> resolveInteractionIntents(LogicalBinding<?> binding) throws PolicyResolutionException;
    
    /**
     * Returns the set of policies that will address the intents that are not provided by the implementation type.
     * 
     * @param logicalComponent Logical component for which policies are to be resolved.
     * @return Set of resolved policies.
     * @throws PolicyResolutionException If all intents cannot be resolved.
     */
    Set<PolicySetExtension> resolveImplementationIntents(LogicalComponent<?> logicalComponent) throws PolicyResolutionException;

}
