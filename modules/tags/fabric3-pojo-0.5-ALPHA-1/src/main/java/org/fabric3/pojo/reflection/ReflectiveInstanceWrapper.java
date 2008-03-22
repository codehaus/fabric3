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

import java.util.List;
import java.util.Map;

import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.TargetDestructionException;
import org.fabric3.spi.component.TargetInitializationException;
import org.fabric3.spi.component.TargetResolutionException;

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
        
    }

    public T getInstance() {
        assert started;
        return instance;
    }

    public boolean isStarted() {
        return started;
    }

    public void start() throws TargetInitializationException {
        assert !started;
        if (initInvoker != null) {
            ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(cl);
                initInvoker.invokeEvent(instance);
            } catch (ObjectCallbackException e) {
                throw new TargetInitializationException(e.getMessage(), e);
            } finally {
                Thread.currentThread().setContextClassLoader(oldCl);
            }
        }
        started = true;
    }


    public void stop() throws TargetDestructionException {
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
            throw new TargetDestructionException(e.getMessage(), e);
        } finally {
            started = false;
        }
    }
    
    public void reinject() throws TargetResolutionException {
        
        try {
            if (injectors != null) {
                for (Injector<T> injector : injectors.values()) {
                    injector.inject(instance);
                }
            }
        } catch (ObjectCreationException ex) {
            throw new TargetResolutionException("Unable to inject", ex);
        }
    }

    public void addObjectFactory(String referenceName, ObjectFactory<?> factory, Object key) {
        
        for (InjectableAttribute attribute : injectors.keySet()) {
            if (attribute.getName().equals(referenceName)) {
                injectors.get(attribute).setObectFactory(factory, key);
            }
        }
        // TODO Auto-generated method stub
        
    }
    
}
