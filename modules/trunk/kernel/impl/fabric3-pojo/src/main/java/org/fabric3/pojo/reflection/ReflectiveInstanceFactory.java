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
    private final ObjectFactory<T> constructor;
    private final Injector<T>[] injectors;
    private final EventInvoker<T> initInvoker;
    private final EventInvoker<T> destroyInvoker;
    private final ClassLoader cl;

    public ReflectiveInstanceFactory(ObjectFactory<T> constructor,
                                     Injector<T>[] injectors,
                                     EventInvoker<T> initInvoker,
                                     EventInvoker<T> destroyInvoker,
                                     ClassLoader cl) {
        this.constructor = constructor;
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
            T instance = constructor.getInstance();
            if (injectors != null) {
                for (Injector<T> injector : injectors) {
                    injector.inject(instance);
                }
            }
            return new ReflectiveInstanceWrapper<T>(instance, cl, initInvoker, destroyInvoker);
        } finally {
            PojoWorkContextTunnel.setThreadWorkContext(oldContext);
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }
}
