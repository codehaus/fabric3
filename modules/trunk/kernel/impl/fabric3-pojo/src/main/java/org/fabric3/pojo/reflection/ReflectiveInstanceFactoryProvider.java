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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.fabric3.scdl.ConstructorInjectionSite;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.scdl.ValueSource;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.InstanceFactory;
import org.fabric3.spi.component.InstanceFactoryProvider;

/**
 * @version $Rev$ $Date$
 */
public class ReflectiveInstanceFactoryProvider<T> implements InstanceFactoryProvider<T> {
    private static final ObjectFactory<?> NULL_FACTORY = new ObjectFactory<Object>() {
        public Object getInstance() {
            return null;
        }
    };

    private final Class<T> implementationClass;
    private final Constructor<T> constructor;
    private final List<ValueSource> cdiSources;
    private final Map<ValueSource, InjectionSite> injectionSites;
    private final EventInvoker<T> initInvoker;
    private final EventInvoker<T> destroyInvoker;
    private final Map<ValueSource, ObjectFactory<?>> factories = new HashMap<ValueSource, ObjectFactory<?>>();
    private final ClassLoader cl;

    public ReflectiveInstanceFactoryProvider(Constructor<T> constructor,
                                             List<ValueSource> cdiSources,
                                             Map<ValueSource, InjectionSite> injectionSites,
                                             Method initMethod,
                                             Method destroyMethod,
                                             ClassLoader cl) {
        this.implementationClass = constructor.getDeclaringClass();
        this.constructor = constructor;
        this.cdiSources = cdiSources;
        this.injectionSites = injectionSites;
        this.initInvoker = initMethod == null ? null : new MethodEventInvoker<T>(initMethod);
        this.destroyInvoker = destroyMethod == null ? null : new MethodEventInvoker<T>(destroyMethod);
        this.cl = cl;
    }

    public void setObjectFactory(ValueSource name, ObjectFactory<?> objectFactory) {
        factories.put(name, objectFactory);
    }

    public Class<?> getMemberType(ValueSource valueSource) {
        InjectionSite site = injectionSites.get(valueSource);
        if (site == null) {
            throw new AssertionError("No injection site for " + valueSource);
        }
        switch (site.getElementType()) {
        case FIELD:
            try {
                FieldInjectionSite fieldSite = (FieldInjectionSite) site;
                Field field = getField(fieldSite.getName());
                return field.getType();
            } catch (NoSuchFieldException e) {
                throw new AssertionError(e);
            }
        case METHOD:
            try {
                MethodInjectionSite methodSite = (MethodInjectionSite) site;
                Method method = methodSite.getSignature().getMethod(implementationClass);
                return method.getParameterTypes()[methodSite.getParam()];
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            }
        case CONSTRUCTOR:
            try {
                ConstructorInjectionSite methodSite = (ConstructorInjectionSite) site;
                Constructor<T> method = methodSite.getSignature().getConstructor(implementationClass);
                return method.getParameterTypes()[methodSite.getParam()];
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            }
        default:
            throw new AssertionError();
        }
    }

    public Type getGenericType(ValueSource valueSource) {
        InjectionSite site = injectionSites.get(valueSource);
        if (site == null) {
            throw new AssertionError("No injection site for " + valueSource);
        }
        switch (site.getElementType()) {
        case FIELD:
            try {
                FieldInjectionSite fieldSite = (FieldInjectionSite) site;
                Field field = getField(fieldSite.getName());
                return field.getGenericType();
            } catch (NoSuchFieldException e) {
                throw new AssertionError(e);
            }
        case METHOD:
            try {
                MethodInjectionSite methodSite = (MethodInjectionSite) site;
                Method method = methodSite.getSignature().getMethod(implementationClass);
                return method.getGenericParameterTypes()[methodSite.getParam()];
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            }
        case CONSTRUCTOR:
            try {
                ConstructorInjectionSite methodSite = (ConstructorInjectionSite) site;
                Constructor<T> method = methodSite.getSignature().getConstructor(implementationClass);
                return method.getGenericParameterTypes()[methodSite.getParam()];
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            }
        default:
            throw new AssertionError();
        }
    }

    private Field getField(String name) throws NoSuchFieldException {
        Class<?> clazz = implementationClass;
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    public Class<T> getImplementationClass() {
        return implementationClass;
    }

    public InstanceFactory<T> createFactory() {
        ObjectFactory<T> factory = new ReflectiveObjectFactory<T>(constructor, getArgumentFactories(cdiSources));
        List<Injector<T>> injectors = getInjectors();
        return new ReflectiveInstanceFactory<T>(factory, injectors, initInvoker, destroyInvoker, cl);
    }

    protected ObjectFactory<?>[] getArgumentFactories(List<ValueSource> sources) {
        ObjectFactory<?>[] argumentFactories = new ObjectFactory<?>[sources.size()];
        for (int i = 0; i < argumentFactories.length; i++) {
            ValueSource source = sources.get(i);
            ObjectFactory<?> factory = factories.get(source);
            if (factory == null) {
                factory = NULL_FACTORY;
            }
            argumentFactories[i] = factory;
        }
        return argumentFactories;
    }

    protected List<Injector<T>> getInjectors() {
        List<Injector<T>> injectors = new ArrayList<Injector<T>>(injectionSites.size());
        for (Map.Entry<ValueSource, InjectionSite> entry : injectionSites.entrySet()) {
            ValueSource name = entry.getKey();
            InjectionSite site = entry.getValue();
            ObjectFactory<?> factory = factories.get(name);
            if (factory != null) {
                switch (site.getElementType()) {
                case FIELD:
                    try {
                        FieldInjectionSite fieldSite = (FieldInjectionSite) site;
                        Field field = getField(fieldSite.getName());
                        injectors.add(new FieldInjector<T>(field, factory));
                    } catch (NoSuchFieldException e) {
                        throw new AssertionError(e);
                    }
                    break;
                case METHOD:
                    try {
                        MethodInjectionSite methodSite = (MethodInjectionSite) site;
                        Method method = methodSite.getSignature().getMethod(implementationClass);
                        injectors.add(new MethodInjector<T>(method, factory));
                    } catch (ClassNotFoundException e) {
                        throw new AssertionError(e);
                    } catch (NoSuchMethodException e) {
                        throw new AssertionError(e);
                    }
                    break;
                }
            }
        }
        return injectors;
    }
}
