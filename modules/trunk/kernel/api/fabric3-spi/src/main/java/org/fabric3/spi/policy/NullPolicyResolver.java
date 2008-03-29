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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * @version $Revision$ $Date$
 */
public class NullPolicyResolver implements PolicyResolver {
    
    public PolicyResult resolvePolicies(ServiceContract<?> serviceContract,
                                            LogicalBinding<?> sourceBinding, 
                                            LogicalBinding<?> targetBinding, 
                                            LogicalComponent<?> source, 
                                            LogicalComponent<?> target) throws PolicyResolutionException {
        return new PolicyResult() {

            public List<PolicySet> getInterceptedPolicySets(Operation<?> operation) {
                return Collections.emptyList();
            }

            public Policy getSourcePolicy() {
                return new Policy() {
                    public List<Intent> getProvidedIntents(Operation<?> operation) {
                        return Collections.emptyList();
                    }
                    public List<PolicySet> getProvidedPolicySets(Operation<?> operation) {
                        return Collections.emptyList();
                    }
                };
            }

            public Policy getTargetPolicy() {
                return new Policy() {
                    public List<Intent> getProvidedIntents(Operation<?> operation) {
                        return Collections.emptyList();
                    }
                    public List<PolicySet> getProvidedPolicySets(Operation<?> operation) {
                        return Collections.emptyList();
                    }
                };
            }
            
        };
    }

}
