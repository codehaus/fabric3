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
package org.fabric3.resource.resolver;

import java.net.URI;

import org.fabric3.resource.model.SystemSourcedResource;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.resource.ResourceResolutionException;
import org.fabric3.spi.resource.ResourceResolver;
import org.osoa.sca.annotations.EagerInit;

/**
 * Default implementation of the resource resolver that maps the requested 
 * resources to components from the system tree. The implementation expects 
 * the <code>mappedName</code> of the resource to the name of the component
 * in the system tree.
 * 
 * @version $Revision$ $Date$
 */
@EagerInit
public class SystemSourcedResourceResolver implements ResourceResolver<SystemSourcedResource<?>> {
    
    private static final String SYSTEM_URI = "fabric3://./runtime/";

    /**
     * @see org.fabric3.spi.resource.ResourceResolver#resolve(org.fabric3.spi.model.instance.LogicalResource)
     */
    public void resolve(LogicalResource<SystemSourcedResource<?>> logicalResource) throws ResourceResolutionException {
       
       SystemSourcedResource<?> resourceDefinition = logicalResource.getResourceDefinition();
       String mappedName = resourceDefinition.getMappedName();
            
       if(mappedName == null) {
           throw new ResourceResolutionException("Mapped name is required for system sourced resources");
       }

       URI targetUri = URI.create(SYSTEM_URI + mappedName);
       logicalResource.setTarget(targetUri);
        
    }


}
