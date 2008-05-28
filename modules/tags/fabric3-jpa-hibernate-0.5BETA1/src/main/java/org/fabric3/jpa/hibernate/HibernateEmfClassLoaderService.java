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
package org.fabric3.jpa.hibernate;

import java.util.Set;
import java.util.HashSet;
import java.net.URI;

import org.osoa.sca.annotations.Reference;

import org.fabric3.jpa.spi.classloading.EmfClassLoaderService;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.classloader.FilteringMultiparentClassLoader;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;

/**
 * Implements the strategy for creating a classloader network to load Hibernate EntityManagerFactories. Hibernate uses CGLIB to proxy classes for lazy
 * loading. When it generates these proxies, Hibernate adds an interface, HibernateProxy, and uses the classloader of the user class the proxy
 * implements. This which results in the user classloader requiring visibility to the HibernateProxy classloader. To accomodate this, this
 * implementation adds the classloader for the Hibernate extension as a parent to the user classloader. In order to avoid exposing other system
 * classes to the user classloader, a FilteringMultiparentClassLoader is interposed between the application and extension classloaders.
 *
 * @version $Revision$ $Date$
 */
public class HibernateEmfClassLoaderService implements EmfClassLoaderService {
    public static final URI CLASSLOADER_URI = URI.create("HibernateFilteringClassloader");

    private ClassLoaderRegistry classLoaderRegistry;

    public HibernateEmfClassLoaderService(@Reference ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public ClassLoader getEmfClassLoader(URI classLoaderUri) {
        ClassLoader systemCl = getClass().getClassLoader();
        ClassLoader classloader = classLoaderRegistry.getClassLoader(classLoaderUri);
        assert classloader instanceof MultiParentClassLoader;
        MultiParentClassLoader appCl = (MultiParentClassLoader) classloader;
        Set<String> filters = new HashSet<String>();
        filters.add("org.hibernate.*");   // allow visibility to only hibernate classes.
        FilteringMultiparentClassLoader cl = new FilteringMultiparentClassLoader(CLASSLOADER_URI, systemCl, filters);
        if (!appCl.getParents().contains(systemCl)) {
            appCl.addParent(cl);
        }
        return appCl;
    }
}
