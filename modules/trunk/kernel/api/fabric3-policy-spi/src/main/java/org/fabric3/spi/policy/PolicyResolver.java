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
package org.fabric3.spi.policy;

import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;


/**
 * Interface for the policy resolver.
 * 
 * @version $Revision$ $Date$
 */
public interface PolicyResolver {

    /**
     * Resolves all the interaction and implementation intents for the wire.
     * 
     * @param serviceContract Service contract for the wire.
     * @param sourceBinding Source binding.
     * @param targetBinding Target binding.
     * @param source Source component.
     * @param target Target component.
     * @return Policy resolution result.
     * 
     * @throws PolicyResolutionException If unable to resolve any policies.
     */
    public abstract PolicyResult resolvePolicies(ServiceContract<?> serviceContract,
                                                     LogicalBinding<?> sourceBinding, 
                                                     LogicalBinding<?> targetBinding, 
                                                     LogicalComponent<?> source, 
                                                     LogicalComponent<?> target) throws PolicyResolutionException;

}
