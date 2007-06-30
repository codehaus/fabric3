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
package org.fabric3.pojo.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.InstanceFactory;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.WorkContext;

/**
 * @version $Rev$ $Date$
 */
public class ReflectiveInstanceFactory<T> implements InstanceFactory<T> {
    private final Constructor<T> ctr;
    private final ObjectFactory<?>[] ctrArgs;
    private final Injector<T>[] injectors;
    private final EventInvoker<T> initInvoker;
    private final EventInvoker<T> destroyInvoker;
    private final ClassLoader cl;

    public ReflectiveInstanceFactory(Constructor<T> ctr,
                                     ObjectFactory<?>[] ctrArgs,
                                     Injector<T>[] injectors,
                                     EventInvoker<T> initInvoker,
                                     EventInvoker<T> destroyInvoker, ClassLoader cl) {
        this.ctr = ctr;
        this.ctrArgs = ctrArgs;
        this.injectors = injectors;
        this.initInvoker = initInvoker;
        this.destroyInvoker = destroyInvoker;
        this.cl = cl;
    }

    public InstanceWrapper<T> newInstance(WorkContext workContext) throws ObjectCreationException {
        // push the work context onto the thread when calling the user object
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
        WorkContext oldContext = PojoWorkContextTunnel.setThreadWorkContext(workContext);
        try {
            T instance;
            if (ctrArgs == null) {
                // use no-arg constructor
                instance = ctr.newInstance();
            } else {
                // create constructor values
                Object[] args = new Object[ctrArgs.length];
                for (int i = 0; i < args.length; i++) {
                    args[i] = ctrArgs[i].getInstance();
                }
                instance = ctr.newInstance(args);
            }

            if (injectors != null) {
                for (Injector<T> injector : injectors) {
                    injector.inject(instance);
                }
            }
            return new ReflectiveInstanceWrapper<T>(instance, initInvoker, destroyInvoker);
        } catch (InstantiationException e) {
            String name = ctr.getDeclaringClass().getName();
            throw new AssertionError("Class is not instantiable [" + name + "]");
        } catch (IllegalAccessException e) {
            String name = ctr.getDeclaringClass().getName();
            throw new AssertionError("Constructor is not accessible [" + name + "]");
        } catch (InvocationTargetException e) {
            String name = ctr.getDeclaringClass().getName();
            throw new ObjectCreationException("Exception thrown by constructor", name, e.getCause());
        } finally {
            PojoWorkContextTunnel.setThreadWorkContext(oldContext);
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }
}
