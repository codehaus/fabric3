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
package org.fabric3.spring;

import java.util.HashMap;
import java.util.Map;

import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.Property;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ResourceDefinition;
import org.fabric3.scdl.ServiceDefinition;

/**
 * A component type specialization for Spring implementations
 *
 * @version $$Rev$$ $$Date$$
 */
public class SpringComponentType extends AbstractComponentType<ServiceDefinition, ReferenceDefinition, Property<?>, ResourceDefinition> {

    // override super class's object since we need to change introspected
    // serviceName to declared serviceName, which is equal to beanId
    private final Map<String, ServiceDefinition> services = new HashMap<String, ServiceDefinition>();

    /**
     * Returns a live Map of the services provided by the implementation.
     *
     * @return a live Map of the services provided by the implementation
     */
    @Override
    public Map<String, ServiceDefinition> getServices() {
        return services;
    }

    /**
     * Add a service to those provided by the implementation. Any existing service with the same name is replaced.
     *
     * @param service a service provided by the implementation
     */
    public void add(String serviceName, ServiceDefinition service) {
        services.put(serviceName, service);
    }

}
