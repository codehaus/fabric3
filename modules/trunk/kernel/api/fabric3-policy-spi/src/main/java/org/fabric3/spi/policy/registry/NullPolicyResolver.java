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

import java.util.Collections;
import java.util.Set;

import org.fabric3.scdl.Operation;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * @version $Revision$ $Date$
 */
public class NullPolicyResolver implements PolicyResolver {

    /**
     * @see org.fabric3.spi.policy.registry.PolicyResolver#getImplementationIntentsToBeProvided(org.fabric3.spi.model.instance.LogicalComponent)
     */
    public Set<Intent> getImplementationIntentsToBeProvided(LogicalComponent<?> logicalComponent) throws PolicyResolutionException {
        return Collections.emptySet();
    }

    /**
     * @see org.fabric3.spi.policy.registry.PolicyResolver#getInteractionIntentsToBeProvided(org.fabric3.spi.model.instance.LogicalBinding)
     */
    public Set<Intent> getInteractionIntentsToBeProvided(LogicalBinding<?> logicalBinding) throws PolicyResolutionException {
        return Collections.emptySet();
    }

    /**
     * @see org.fabric3.spi.policy.registry.PolicyResolver#resolveImplementationIntents(org.fabric3.spi.model.instance.LogicalComponent)
     */
    public Set<PolicyResult> resolveImplementationIntents(LogicalComponent<?> logicalComponent) throws PolicyResolutionException {
        return Collections.emptySet();
    }

    /**
     * @see org.fabric3.spi.policy.registry.PolicyResolver#resolveInteractionIntents(org.fabric3.spi.model.instance.LogicalBinding)
     */
    public Set<PolicyResult> resolveInteractionIntents(LogicalBinding<?> binding, Operation<?> operation) throws PolicyResolutionException {
        return Collections.emptySet();
    }

}
