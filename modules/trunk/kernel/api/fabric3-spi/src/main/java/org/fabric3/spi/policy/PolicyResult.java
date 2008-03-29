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

import java.util.List;

import org.fabric3.scdl.Operation;
import org.fabric3.scdl.definitions.PolicySet;

/**
 * Result of resolving intents and policy sets on a wire. The policies are resolved for 
 * the source and target bindings as well as the source and target component types. A wire 
 * can be between two components or between a component and a binding. 
 * 
 * For a wire between two components, the result will include,
 * 
 * 1. Implementation intents that are requested for each operation on the source side and may be 
 * provided by the source component implementation type.
 * 2. Implementation intents that are requested for each operation on the target side and may be 
 * provided by the target component implementation type.
 * 3. Policy sets that map to implementation intents on each operation on the source side and 
 * understood by the source component implementation type.
 * 4. Policy sets that map to implementation intents on each operation on the target side and 
 * understood by the target component implementation type.
 * 5. Policy sets that map to implementation intents on each operation on the source and target 
 * side that are implemented using interceptors.
 * 
 * For a wire between a binding and a component (service binding), the result will include
 * 
 * 1. Interaction intents that are requested for each operation and may be provided by the 
 * service binding type.
 * 2. Implementation intents that are requested for each operation and may be  provided by 
 * the target component implementation type.
 * 3. Policy sets that map to implementation intents on each operation and understood by the 
 * component implementation type.
 * 4. Policy sets that map to interaction intents on each operation on the source side and 
 * understood by the service binding type.
 * 5. Policy sets that map to implementation and interaction intents on each operation that 
 * are implemented using interceptors.
 * 
 * For a wire between a component and a binding (reference binding), the result will include
 * 
 * 1. Interaction intents that are requested for each operation and may be provided by the 
 * reference binding type.
 * 2. Implementation intents that are requested for each operation and may be provided by the 
 * component implementation type.
 * 3. Policy sets that map to implementation intents on each operation and understood by the 
 * component implementation type.
 * 4. Policy sets that map to interaction intents on each operation and understood by the 
 * service binding type.
 * 5. Policy sets that map to implementation and interaction intents on each operation that 
 * are implemented using interceptors.
 * 
 * @version $Revision$ $Date$
 */
public interface PolicyResult {
    
    /**
     * @return Policies and intents provided at the source end.
     */
    public Policy getSourcePolicy();
    
    /**
     * @return Policies and intents provided at the target end.
     */
    public Policy getTargetPolicy();
    
    /**
     * @param operation Operation against which interceptors are defined.
     * @return Interceptors that are defined against the operation.
     */
    public List<PolicySet> getInterceptedPolicySets(Operation<?> operation);

}
