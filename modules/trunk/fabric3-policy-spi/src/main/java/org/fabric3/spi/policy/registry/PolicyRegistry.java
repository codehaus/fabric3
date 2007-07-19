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

import javax.xml.namespace.QName;

import org.fabric3.spi.model.instance.LogicalScaArtifact;
import org.fabric3.spi.policy.model.Intent;
import org.fabric3.spi.policy.model.PolicySet;

/**
 * Abstraction for the service that keeps track of the domain-wide
 * policies and intents that are registered.
 * 
 * @version $Revision$ $Date$
 */
public interface PolicyRegistry {
    
    /**
     * @param policySet Policy set that needs to be registered.
     */
    void registerPolicySet(PolicySet policySet);
    
    /**
     * @param intent Intent that needs to be registered.
     */
    void registerIntent(Intent intent);
    
    /**
     * Finds the interceptor qnames for the SCA artifact based on the rules 
     * specified by the SCA policy specification.
     * 
     * @param scaArtifact SCA artifact.
     * @return List of interceptors.
     */
    Set<QName> getInterceptorBuilders(LogicalScaArtifact<?> scaArtifact);

}
