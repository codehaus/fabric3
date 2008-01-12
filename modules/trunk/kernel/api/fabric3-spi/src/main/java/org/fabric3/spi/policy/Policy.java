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

import java.util.Set;

import org.fabric3.scdl.Operation;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;

/**
 * @version $Revision$ $Date$
 */
public interface Policy {

    /**
     * Intents that are provided by the binding or implemenenation for the
     * requested operation.
     * 
     * @param operation Operation for which requested intents are provided.
     * @return Requested intents that are provided.
     */
    public Set<Intent> getProvidedIntents(Operation<?> operation);

    /**
     * Policy sets that are provided by the binding or implemenenation for the
     * requested operation.
     * 
     * @param operation Operation for which requested intents are provided.
     * @return Resolved policy sets that are provided.
     */
    public Set<PolicySet> getProvidedPolicySets(Operation<?> operation);

}
