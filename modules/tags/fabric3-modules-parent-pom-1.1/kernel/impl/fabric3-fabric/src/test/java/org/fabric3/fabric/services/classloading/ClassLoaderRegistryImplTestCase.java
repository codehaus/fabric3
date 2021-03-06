/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.fabric.services.classloading;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import junit.framework.TestCase;

import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.fabric.classloader.ClassLoaderRegistryImpl;

/**
 * @version $Rev$ $Date$
 */
public class ClassLoaderRegistryImplTestCase extends TestCase {
    private ClassLoaderRegistry registry = new ClassLoaderRegistryImpl();

    public void testResolveParentUris() throws Exception {
        URL[] urls = new URL[0];
        ClassLoader parent = new URLClassLoader(urls, null);
        ClassLoader loader = new URLClassLoader(urls, parent);
        URI parentId = URI.create("parent");
        registry.register(parentId, parent);
        URI loaderId = URI.create("loader");
        registry.register(loaderId, loader);
        List<URI> parents = registry.resolveParentUris(loader);
        assertEquals(1, parents.size());
        assertEquals(parentId, parents.get(0));
    }
}
