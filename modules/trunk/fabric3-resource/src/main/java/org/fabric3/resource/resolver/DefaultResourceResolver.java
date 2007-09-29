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

import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.ResourceDefinition;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.resource.ResourceResolutionException;
import org.fabric3.spi.resource.ResourceResolver;

/**
 * Default implementation of the resource resolver that maps the requested 
 * resources to components from the system tree. The implementation expects 
 * the <code>mappedName</code> of the resource to the name of the component
 * in the system tree.
 * 
 * @version $Revision$ $Date$
 */
public class DefaultResourceResolver implements ResourceResolver {

    /**
     * @see org.fabric3.spi.resource.ResourceResolver#resolve(org.fabric3.spi.model.instance.LogicalComponent, java.net.URI)
     */
    public void resolve(LogicalComponent<? extends Implementation<AbstractComponentType<?,?,?,?>>> component, URI domainUri) throws ResourceResolutionException {
        
        AbstractComponentType<?, ?, ?, ?> componentType = component.getComponentType();
        
        for(ResourceDefinition resourceDefinition : componentType.getResources().values()) {
            
            String name = resourceDefinition.getName();
            String mappedName = resourceDefinition.getMappedName();
            
            if(mappedName == null) {
                throw new ResourceResolutionException("Mapped name is not specified for the resource: " + name);
            }

        }
        
    }


}
