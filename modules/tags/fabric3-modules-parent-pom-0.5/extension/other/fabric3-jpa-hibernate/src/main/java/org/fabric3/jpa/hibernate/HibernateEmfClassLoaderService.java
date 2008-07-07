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

import java.net.URI;

import org.osoa.sca.annotations.Reference;

import org.fabric3.jpa.spi.classloading.EmfClassLoaderService;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;

/**
 * Returns the previously constructed classloader. The Hibernate extension implicitly imports the Hibernate extension contribution into any
 * application using it, so nothing is required to be done here.
 *
 * @version $Revision$ $Date$
 */
public class HibernateEmfClassLoaderService implements EmfClassLoaderService {
    private ClassLoaderRegistry classLoaderRegistry;

    public HibernateEmfClassLoaderService(@Reference ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public ClassLoader getEmfClassLoader(URI classLoaderUri) {
        return classLoaderRegistry.getClassLoader(classLoaderUri);
    }
}
