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
import java.util.concurrent.ConcurrentHashMap;

import com.sun.jersey.spi.service.ComponentProvider;

/**
 * @version $Rev$ $Date$
 */
public class Fabric3ComponentProvider implements ComponentProvider {

    ConcurrentHashMap<Class<?>, Object> instances = new ConcurrentHashMap<Class<?>, Object>();

    public Object getInstance(Scope scope, Class c) throws InstantiationException, IllegalAccessException {
        return instances.get(c);
    }

    public Object getInstance(Scope scope, Constructor constructor, Object[] parameters) throws InstantiationException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return null;
    }

    public void addServiceHandler(Class<?> resource, Object instance) {
        instances.put(resource, instance);
    }

    public Set<Class<?>> getClasses() {
        return instances.keySet();
    }

    public <T> T getInjectableInstance(T instance) {
        return instance;
    }

    public void inject(Object instance) {
    }

    public <T> T getInstance(com.sun.jersey.spi.service.ComponentContext context , ComponentProvider.Scope scope, Class<T> clazz) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
