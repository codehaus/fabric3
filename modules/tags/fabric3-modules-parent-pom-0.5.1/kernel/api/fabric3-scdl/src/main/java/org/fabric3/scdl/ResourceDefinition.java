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
package org.fabric3.scdl;

/**
 * Represents a resource definition. Resources are provisioned to runtime nodes.
 *
 * @version $Rev$ $Date$
 */
public class ResourceDefinition extends ModelObject {

    private String name;
    private boolean optional;
    private ServiceContract<?> serviceContract;
    
    public ResourceDefinition(String name, ServiceContract<?> serviceContract, boolean optional) {
        this.name = name;
        this.serviceContract = serviceContract;
        this.optional = optional;
    }

    /**
     * The name of the resource
     * 
     * @return the name of the resource
     */
    public String getName() {
        return name;
    }

    /**
     * If true, the resource is optional
     * 
     * @return true if the resource is optional
     */
    public boolean isOptional() {
        return optional;
    }
    
    /**
     * Returned the service contract for the resource.
     */
    public ServiceContract<?> getServiceContract() {
        return serviceContract;
    }
    
}
