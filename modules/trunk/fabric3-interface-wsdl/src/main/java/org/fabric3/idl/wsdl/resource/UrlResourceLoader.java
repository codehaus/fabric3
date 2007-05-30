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

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Loads a resource from a URL.
 *
 * @version $Revision$ $Date$
 */
public abstract class UrlResourceLoader extends TypedResourceLoader {

    /**
     * @param resourceLoaderRegistry Default resource loader registry.
     */
    public UrlResourceLoader(ResourceLoaderRegistry resourceLoaderRegistry) {
        super(resourceLoaderRegistry);
    }

    /**
     * @see org.fabric3.idl.wsdl.resource.ResourceLoader#loadResource(java.lang.String, java.lang.ClassLoader)
     */
    public URL loadResource(String resourcePath, ClassLoader cl) {

        try {
            return new URL(resourcePath);
        } catch(MalformedURLException ex) {
            throw new ResourceLoaderException("Unable to load resource " + resourcePath, ex);
        }

    }

}
