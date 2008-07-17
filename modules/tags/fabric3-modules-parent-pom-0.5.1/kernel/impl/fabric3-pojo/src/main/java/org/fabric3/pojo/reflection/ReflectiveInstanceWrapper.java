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

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceInitializationException;
import org.fabric3.spi.component.InstanceLifecycleException;

/**
 * @version $Rev$ $Date$
 */
public class ReflectiveInstanceWrapper<T> implements InstanceWrapper<T> {
    private final T instance;
    private final ClassLoader cl;
    private final EventInvoker<T> initInvoker;
    private final EventInvoker<T> destroyInvoker;
    private boolean started;
    private final Map<InjectableAttribute, Injector<T>> injectors;
    private final Set<Injector<T>> updatedInjectors;

    public ReflectiveInstanceWrapper(T instance,
                                     ClassLoader cl,
                                     EventInvoker<T> initInvoker,
                                     EventInvoker<T> destroyInvoker,
                                     Map<InjectableAttribute, Injector<T>> injectors) {
        this.instance = instance;
        this.cl = cl;
        this.initInvoker = initInvoker;
        this.destroyInvoker = destroyInvoker;
        this.started = false;
        this.injectors = injectors;
        this.updatedInjectors = new HashSet<Injector<T>>();
    }

    public T getInstance() {
        assert started;
        return instance;
    }

    public boolean isStarted() {
        return started;
    }

    public void start() throws InstanceInitializationException {
        assert !started;
        if (initInvoker != null) {
            ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(cl);
                initInvoker.invokeEvent(instance);
            } catch (ObjectCallbackException e) {
                throw new InstanceInitializationException(e.getMessage(), e);
            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
            }
        }
        started = true;
    }


    public void stop() throws InstanceDestructionException {
        assert started;
        try {
            if (destroyInvoker != null) {
                ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(cl);
                    destroyInvoker.invokeEvent(instance);
                } finally {
                    Thread.currentThread().setContextClassLoader(oldCl);
                }
            }
        } catch (ObjectCallbackException e) {
            throw new InstanceDestructionException(e.getMessage(), e);
        } finally {
            started = false;
        }
    }

    public void reinject() throws InstanceLifecycleException {
        try {
            for (Injector<T> injector : updatedInjectors) {
                injector.inject(instance);
            }
            updatedInjectors.clear();
        } catch (ObjectCreationException ex) {
            throw new InstanceLifecycleException("Unable to inject", ex);
        }
    }

    public void addObjectFactory(String referenceName, ObjectFactory<?> factory, Object key) {
        for (InjectableAttribute attribute : injectors.keySet()) {
            if (attribute.getName().equals(referenceName)) {
                Injector<T> injector = injectors.get(attribute);
                injector.setObjectFactory(factory, key);
                if (instance != null) {
                    updatedInjectors.add(injector);
                }
            }
        }

    }

}
