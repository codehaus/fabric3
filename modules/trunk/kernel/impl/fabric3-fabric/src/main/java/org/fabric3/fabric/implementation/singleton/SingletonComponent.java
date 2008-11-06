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
 * --- Original Apache License ---
 *
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
package org.fabric3.fabric.implementation.singleton;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.osoa.sca.ComponentContext;

import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.scdl.PropertyValue;
import org.fabric3.spi.AbstractLifecycle;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.SingletonObjectFactory;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceInitializationException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.invocation.WorkContext;

/**
 * Wraps an object intended to service as a system component provided to the Fabric3 runtime by the host environment.
 *
 * @version $$Rev$$ $$Date$$
 */
public class SingletonComponent<T> extends AbstractLifecycle implements AtomicComponent<T> {
    private final URI uri;
    private T instance;
    private Map<Member, InjectableAttribute> sites;
    private InstanceWrapper<T> wrapper;
    private Map<String, PropertyValue> defaultPropertyValues;
    private Map<ObjectFactory, InjectableAttribute> reinjectionValues;

    public SingletonComponent(URI componentId, T instance, Map<InjectionSite, InjectableAttribute> mappings) {
        this.uri = componentId;
        this.instance = instance;
        this.wrapper = new SingletonWrapper<T>(instance);
        this.reinjectionValues = new HashMap<ObjectFactory, InjectableAttribute>();
        initializeInjectionSites(instance, mappings);
    }

    public String getKey() {
        return null;
    }

    public URI getUri() {
        return uri;
    }

    public QName getDeployable() {
        return null;
    }

    public boolean isEagerInit() {
        return false;
    }

    public int getInitLevel() {
        return 0;
    }

    public long getMaxIdleTime() {
        return -1;
    }

    public long getMaxAge() {
        return -1;
    }

    public InstanceWrapper<T> createInstanceWrapper(WorkContext workContext) throws ObjectCreationException {
        return wrapper;
    }

    public ObjectFactory<T> createObjectFactory() {
        return new SingletonObjectFactory<T>(instance);
    }

    public ComponentContext getComponentContext() {
        // singleton components do not provide a component context
        return null;
    }

    public Map<String, PropertyValue> getDefaultPropertyValues() {
        return defaultPropertyValues;
    }

    public void setDefaultPropertyValues(Map<String, PropertyValue> defaultPropertyValues) {
        this.defaultPropertyValues = defaultPropertyValues;
    }

    /**
     * Adds an ObjectFactory to be reinjected
     *
     * @param attribute    the InjectableAttribute describing the site to reinject
     * @param paramFactory the object factory responsible for supplying a value to reinject
     */
    public void addObjectFactory(InjectableAttribute attribute, ObjectFactory paramFactory) {
        reinjectionValues.put(paramFactory, attribute);
    }

    public String toString() {
        return "[" + uri.toString() + "] in state [" + super.toString() + ']';
    }

    /**
     * Obtain the fields and methods for injection sites associated with the instance
     *
     * @param instance the instance this component wraps
     * @param mappings the mappings of injection sites
     */
    private void initializeInjectionSites(T instance, Map<InjectionSite, InjectableAttribute> mappings) {
        this.sites = new HashMap<Member, InjectableAttribute>();
        for (Map.Entry<InjectionSite, InjectableAttribute> entry : mappings.entrySet()) {
            InjectionSite site = entry.getKey();
            if (site instanceof FieldInjectionSite) {
                try {
                    Field field = getField(((FieldInjectionSite) site).getName());
                    sites.put(field, entry.getValue());
                } catch (NoSuchFieldException e) {
                    // programming error
                    throw new AssertionError(e);
                }
            } else if (site instanceof MethodInjectionSite) {
                MethodInjectionSite methodInjectionSite = (MethodInjectionSite) site;
                try {
                    Method method = methodInjectionSite.getSignature().getMethod(instance.getClass());
                    sites.put(method, entry.getValue());
                } catch (ClassNotFoundException e) {
                    // programming error
                    throw new AssertionError(e);
                } catch (NoSuchMethodException e) {
                    // programming error
                    throw new AssertionError(e);
                }

            } else {
                // ignore other injection sites
            }
        }
    }

    private Field getField(String name) throws NoSuchFieldException {
        Class<?> clazz = instance.getClass();
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private class SingletonWrapper<T> implements InstanceWrapper<T> {

        private final T instance;

        private SingletonWrapper(T instance) {
            this.instance = instance;
        }

        public T getInstance() {
            return instance;
        }

        public boolean isStarted() {
            return true;
        }

        public void start() throws InstanceInitializationException {
        }

        public void stop() throws InstanceDestructionException {
        }

        public void reinject() {
            for (Map.Entry<ObjectFactory, InjectableAttribute> entry : reinjectionValues.entrySet()) {
                try {
                    inject(entry.getValue(), entry.getKey());
                } catch (ObjectCreationException e) {
                    throw new AssertionError(e);
                }
            }
            reinjectionValues.clear();
        }

        /**
         * Injects a new value on a field or method of the instance.
         *
         * @param attribute the InjectableAttribute defining the field or method
         * @param factory   the ObjectFactory that returns the value to inject
         * @throws ObjectCreationException if an error occurs during injection
         */
        private void inject(InjectableAttribute attribute, ObjectFactory factory) throws ObjectCreationException {
            for (Map.Entry<Member, InjectableAttribute> entry : sites.entrySet()) {
                if (entry.getValue().equals(attribute)) {
                    Member member = entry.getKey();
                    if (member instanceof Field) {
                        try {
                            Object param = factory.getInstance();
                            ((Field) member).set(instance, param);
                        } catch (IllegalAccessException e) {
                            // should not happen as accessibility is already set
                            throw new ObjectCreationException(e);
                        }
                    } else if (member instanceof Method) {
                        try {
                            Object param = factory.getInstance();
                            ((Method) member).invoke(instance, param);
                        } catch (IllegalAccessException e) {
                            // should not happen as accessibility is already set
                            throw new ObjectCreationException(e);
                        } catch (InvocationTargetException e) {
                            throw new ObjectCreationException(e);
                        }
                    } else {
                        // programming error
                        throw new ObjectCreationException("Unsupported member type" + member);
                    }
                }
            }
        }

        public void addObjectFactory(String referenceName, ObjectFactory<?> factory, Object key) {
            // no-op
        }

    }
}
