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

import org.fabric3.scdl.Implementation;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Interface for the policy resolver.
 * 
 * @version $Revision$ $Date$
 */
public interface PolicyResolver {
    
    /**
     * Resolves interaction intents defined against a binding.
     * 
     * @param logicalBinding Binding against which intents are declared.
     * @return Policy resolution result.
     * @throws PolicyResolutionException If unable to resolve the intents.
     */
    PolicyResolutionResult resolveIntents(LogicalBinding<?> logicalBinding) throws PolicyResolutionException;
    
    /**
     * Resolves implementation intents defined against an implementation.
     * 
     * @param logicalComponent Logical component against which intents are declared.
     * @return Policy resolution result.
     * @throws PolicyResolutionException If unable to resolve the intents.
     */
    PolicyResolutionResult resolveIntents(LogicalComponent<?> logicalComponent) throws PolicyResolutionException;

}
