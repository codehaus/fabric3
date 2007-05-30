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

package org.fabric3.idl.wsdl.resource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation for the resource loader. This tries to load from 
 */
public class DefaultResourceLoader implements ResourceLoader {

    /**
     * Cache of resource loaders.
     */
    private Map<String, ResourceLoader> resourceLoaders = new HashMap<String, ResourceLoader>();
    
    /**
     * @see org.fabric3.idl.wsdl.resource.ResourceLoader#loadResource(java.lang.String, java.lang.ClassLoader)
     */
    public InputStream loadResource(String resourcePath, ClassLoader cl) throws ResourceLoaderException {
        
        String type = resourcePath.split(":")[0];
        if(!resourceLoaders.containsKey(type)) {
            throw new ResourceLoaderException("No loader specified for type" + type);
        }
        return resourceLoaders.get(type).loadResource(resourcePath, cl);
        
    }

    /**
     * Registers a resource loader.
     * 
     * @param type Resource loader type.
     * @param loader Resource loader.
     */
    public void register(String type, ResourceLoader loader) {
        resourceLoaders.put(type, loader);
    }

}
