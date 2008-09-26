/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
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
