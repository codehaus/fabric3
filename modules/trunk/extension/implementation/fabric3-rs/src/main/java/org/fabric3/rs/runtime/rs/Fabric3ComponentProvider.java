/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.rs.runtime.rs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import com.sun.jersey.spi.service.ComponentProvider;
import java.util.concurrent.ConcurrentHashMap;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;

/**
 * @version $Rev$ $Date$
 */
public class Fabric3ComponentProvider implements ComponentProvider {

    ConcurrentHashMap<Class<?>, ObjectFactory<?>> resources;
    ConcurrentHashMap<Class<?>, ObjectFactory<?>> providers;

    public Fabric3ComponentProvider() {
        resources = new ConcurrentHashMap<Class<?>, ObjectFactory<?>>();
        providers = new ConcurrentHashMap<Class<?>, ObjectFactory<?>>();

    }

    public Object getInstance(Scope scope, Class c) throws InstantiationException, IllegalAccessException {
        ObjectFactory factory = resources.get(c);
        if (factory == null) {
            factory = providers.get(c);
        }
        if (factory != null) {
            try {
                return factory.getInstance();
            } catch (ObjectCreationException ex) {
                ex.printStackTrace();
                InstantiationException ie = new InstantiationException("unable to create instance");
                ie.initCause(ex);
                throw ie;
            }
        }
        return null;
    }

    public Object getInstance(Scope scope, Constructor constructor, Object[] parameters) throws InstantiationException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addResource(Class<?> resource, ObjectFactory<?> factory) {
        resources.put(resource, factory);
    }

    public void addProvider(Class<?> resource, ObjectFactory<?> factory) {
        providers.put(resource, factory);
    }

    public Set<Class<?>> getResourceClasses() {
        return resources.keySet();
    }

    public Set<Class<?>> getProviderClasses() {
        return providers.keySet();
    }

    public <T> T getInjectableInstance(T instance) {
        return instance;
    }

    public void inject(Object instance) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
