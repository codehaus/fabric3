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
package org.fabric3.pojo.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.ObjectCreationException;

/**
 * @version $Rev$ $Date$
 */
public class ReflectiveObjectFactory<T> implements ObjectFactory<T> {
    private final Constructor<T> constructor;
    private final ObjectFactory<?>[] paramFactories;

    public ReflectiveObjectFactory(Constructor<T> constructor, ObjectFactory<?>[] paramFactories) {
        this.constructor = constructor;
        this.paramFactories = paramFactories;
    }

    public T getInstance() throws ObjectCreationException {
        try {
            if (paramFactories == null) {
                return constructor.newInstance();
            } else {
                Object[] params = new Object[paramFactories.length];
                for (int i = 0; i < paramFactories.length; i++) {
                    ObjectFactory<?> paramFactory = paramFactories[i];
                    params[i] = paramFactory.getInstance();
                }
                return constructor.newInstance(params);
            }
        } catch (InstantiationException e) {
            String name = constructor.getDeclaringClass().getName();
            throw new AssertionError("Class is not instantiable [" + name + "]");
        } catch (IllegalAccessException e) {
            String name = constructor.getDeclaringClass().getName();
            throw new AssertionError("Constructor is not accessible [" + name + "]");
        } catch (InvocationTargetException e) {
            String name = constructor.getDeclaringClass().getName();
            throw new ObjectCreationException("Exception thrown by constructor", name, e.getCause());
        }
    }
}
