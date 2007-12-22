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
package org.fabric3.fabric.services.runtime;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Map;

import org.fabric3.spi.model.topology.ClassLoaderResourceDescription;
import org.fabric3.spi.model.topology.RuntimeInfo;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.runtime.RuntimeInfoService;

/**
 * Provides runtime information during the boostrap process.
 *
 * @version $Revsion$ $Date$
 */
public class BootstrapRuntimeInfoService implements RuntimeInfoService {
    private ClassLoaderRegistry classLoaderRegistry;

    public BootstrapRuntimeInfoService(ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public RuntimeInfo getRuntimeInfo() {
        RuntimeInfo runtimeInfo = new RuntimeInfo();
        // add classloader info
        for (Map.Entry<URI, ClassLoader> entry : classLoaderRegistry.getClassLoaders().entrySet()) {
            ClassLoaderResourceDescription desc = new ClassLoaderResourceDescription(entry.getKey());
            ClassLoader loader = entry.getValue();
            desc.addParents(classLoaderRegistry.resolveParentUris(loader));
            if (loader instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) loader).getURLs();
                desc.addClassPathUrls(Arrays.asList(urls));
            }
            runtimeInfo.addResourceDescription(desc);
        }
        return runtimeInfo;
    }

}