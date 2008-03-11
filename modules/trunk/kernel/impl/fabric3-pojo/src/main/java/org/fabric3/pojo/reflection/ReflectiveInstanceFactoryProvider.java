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
import org.fabric3.scdl.InjectableAttribute;
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
    private final List<InjectableAttribute> cdiSources;
    private final Map<InjectableAttribute, InjectionSite> injectionSites;
    private final EventInvoker<T> initInvoker;
    private final EventInvoker<T> destroyInvoker;
    private final Map<InjectableAttribute, ObjectFactory<?>> factories = new HashMap<InjectableAttribute, ObjectFactory<?>>();
    private final ClassLoader cl;

    public ReflectiveInstanceFactoryProvider(Constructor<T> constructor,
                                             List<InjectableAttribute> cdiSources,
                                             Map<InjectableAttribute, InjectionSite> injectionSites,
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

    public void setObjectFactory(InjectableAttribute name, ObjectFactory<?> objectFactory) {
        factories.put(name, objectFactory);
    }

    public Class<?> getMemberType(InjectableAttribute injectableAttribute) {
        InjectionSite site = injectionSites.get(injectableAttribute);
        if (site == null) {
            throw new AssertionError("No injection site for " + injectableAttribute + " in " + implementationClass);
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

    public Type getGenericType(InjectableAttribute injectableAttribute) {
        InjectionSite site = injectionSites.get(injectableAttribute);
        if (site == null) {
            throw new AssertionError("No injection site for " + injectableAttribute);
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

    protected ObjectFactory<?>[] getArgumentFactories(List<InjectableAttribute> sources) {
        ObjectFactory<?>[] argumentFactories = new ObjectFactory<?>[sources.size()];
        for (int i = 0; i < argumentFactories.length; i++) {
            InjectableAttribute source = sources.get(i);
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
        for (Map.Entry<InjectableAttribute, InjectionSite> entry : injectionSites.entrySet()) {
            InjectableAttribute name = entry.getKey();
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
