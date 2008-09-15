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
package org.fabric3.resource.model;

import java.lang.reflect.Type;

import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ResourceDefinition;

/**
 *
 * @version $Revision$ $Date$
 */
public class SystemSourcedResource extends ResourceDefinition {
    private String mappedName;

    public SystemSourcedResource(String name, boolean optional, String mappedName, ServiceContract<Type> serviceContract) {
        super(name, serviceContract, optional);
        this.mappedName = mappedName;
    }
    
    public String getMappedName() {
        return this.mappedName;
    }

}
