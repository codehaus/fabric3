/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
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
package org.fabric3.pojo.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.pojo.injection.ListMultiplicityObjectFactory;
import org.fabric3.pojo.injection.MapMultiplicityObjectFactory;
import org.fabric3.pojo.injection.MultiplicityObjectFactory;
import org.fabric3.pojo.injection.SetMultiplicityObjectFactory;
import org.fabric3.scdl.ConstructorInjectionSite;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectableAttributeType;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.MethodInjectionSite;
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
    private final Map<InjectionSite, InjectableAttribute> postConstruction;
    private final EventInvoker<T> initInvoker;
    private final EventInvoker<T> destroyInvoker;
    private final Map<InjectableAttribute, ObjectFactory<?>> factories = new HashMap<InjectableAttribute, ObjectFactory<?>>();
    private final ClassLoader cl;
    private final boolean reinjectable;
    public ReflectiveInstanceFactoryProvider(Constructor<T> constructor,
                                             List<InjectableAttribute> cdiSources,
                                             Map<InjectionSite, InjectableAttribute> postConstruction,
                                             Method initMethod,
                                             Method destroyMethod,
                                             boolean reinjectable,
                                             ClassLoader cl) {
        this.implementationClass = constructor.getDeclaringClass();
        this.constructor = constructor;
        this.cdiSources = cdiSources;
        this.postConstruction = postConstruction;
        this.initInvoker = initMethod == null ? null : new MethodEventInvoker<T>(initMethod);
        this.destroyInvoker = destroyMethod == null ? null : new MethodEventInvoker<T>(destroyMethod);
        this.reinjectable = reinjectable;
        this.cl = cl;

    }

    public void setObjectFactory(InjectableAttribute name, ObjectFactory<?> objectFactory) {
        setObjectFactory(name, objectFactory, null);
    }

    public void setObjectFactory(InjectableAttribute name, ObjectFactory<?> objectFactory, Object key) {
        if (InjectableAttributeType.REFERENCE == name.getValueType() || InjectableAttributeType.CALLBACK == name.getValueType()) {
            setUpdateableFactory(name, objectFactory, key);
        } else {
            // the factory corresponds to a property or context, which will override previous values if reinjected
            factories.put(name, objectFactory);
        }
    }

    public Class<?> getMemberType(InjectableAttribute injectableAttribute) {
        InjectionSite site = findInjectionSite(injectableAttribute);
        if (site == null) {
            throw new AssertionError("No injection site for " + injectableAttribute + " in " + implementationClass);
        }
        if (site instanceof FieldInjectionSite) {
            try {
                FieldInjectionSite fieldSite = (FieldInjectionSite) site;
                Field field = getField(fieldSite.getName());
                return field.getType();
            } catch (NoSuchFieldException e) {
                throw new AssertionError(e);
            }
        } else if (site instanceof MethodInjectionSite) {

            try {
                MethodInjectionSite methodSite = (MethodInjectionSite) site;
                Method method = methodSite.getSignature().getMethod(implementationClass);
                return method.getParameterTypes()[methodSite.getParam()];
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            }
        } else if (site instanceof ConstructorInjectionSite) {
            try {
                ConstructorInjectionSite methodSite = (ConstructorInjectionSite) site;
                Constructor<T> method = methodSite.getSignature().getConstructor(implementationClass);
                return method.getParameterTypes()[methodSite.getParam()];
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            }
        } else {
            throw new AssertionError("Invalid injection site type: " + site.getClass());
        }
    }

    public Type getGenericType(InjectableAttribute injectableAttribute) {
        InjectionSite site = findInjectionSite(injectableAttribute);
        if (site == null) {
            throw new AssertionError("No injection site for " + injectableAttribute + " in " + implementationClass);
        }
        if (site instanceof FieldInjectionSite) {
            try {
                FieldInjectionSite fieldSite = (FieldInjectionSite) site;
                Field field = getField(fieldSite.getName());
                return field.getGenericType();
            } catch (NoSuchFieldException e) {
                throw new AssertionError(e);
            }
        } else if (site instanceof MethodInjectionSite) {
            try {
                MethodInjectionSite methodSite = (MethodInjectionSite) site;
                Method method = methodSite.getSignature().getMethod(implementationClass);
                return method.getGenericParameterTypes()[methodSite.getParam()];
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            }
        } else if (site instanceof ConstructorInjectionSite) {
            try {
                ConstructorInjectionSite methodSite = (ConstructorInjectionSite) site;
                Constructor<T> method = methodSite.getSignature().getConstructor(implementationClass);
                return method.getGenericParameterTypes()[methodSite.getParam()];
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            }
        } else {
            throw new AssertionError("Invalid injection site type " + site.getClass());
        }
    }

    public Class<T> getImplementationClass() {
        return implementationClass;
    }

    @SuppressWarnings({"unchecked"})
    public InstanceFactory<T> createFactory() {
        ObjectFactory<T> factory = new ReflectiveObjectFactory<T>(constructor, getConstructorParameterFactories(cdiSources));
        Map<InjectableAttribute, Injector<T>> mappings = createInjectorMappings();

        InjectableAttribute[] attributes = mappings.keySet().toArray(new InjectableAttribute[mappings.size()]);
        Injector<T>[] injectors = mappings.values().toArray(new Injector[mappings.size()]);

        return new ReflectiveInstanceFactory<T>(factory, attributes, injectors, initInvoker, destroyInvoker, reinjectable, cl);
    }

    /**
     * Returns an ordered array of object factories for the parameters of the constructor used to instantiate an instance.
     *
     * @param sources the ordered list of InjectableAttributes corresponding to the constructor parameter
     * @return the object factories for the constructor
     */
    protected ObjectFactory<?>[] getConstructorParameterFactories(List<InjectableAttribute> sources) {
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

    /**
     * Returns a map of injectors for all post-construction (i.e. field and method) sites. The injectors inject reference proxies, properties,
     * callback proxies, and context objects on an instance when it is initialized.
     *
     * @return a map of injectors keyed by InjectableAttribute.
     */
    protected Map<InjectableAttribute, Injector<T>> createInjectorMappings() {
        Map<InjectableAttribute, Injector<T>> injectors = new LinkedHashMap<InjectableAttribute, Injector<T>>(postConstruction.size());
        for (Map.Entry<InjectionSite, InjectableAttribute> entry : postConstruction.entrySet()) {
            InjectionSite site = entry.getKey();
            InjectableAttribute attribute = entry.getValue();
            InjectableAttributeType attributeType = attribute.getValueType();
            ObjectFactory<?> factory = factories.get(attribute);
            if (factory == null && attributeType == InjectableAttributeType.REFERENCE) {
                // The reference is not configured, i.e. wired. Set an empty, updateable ObjectFactory as it may be wired later.
                factory = createObjectFactory(site.getType());
                factories.put(attribute, factory);
            }
            if (factory != null) {
                if (site instanceof FieldInjectionSite) {

                    try {
                        FieldInjectionSite fieldSite = (FieldInjectionSite) site;
                        Field field = getField(fieldSite.getName());
                        injectors.put(attribute, new FieldInjector<T>(field, factory));
                    } catch (NoSuchFieldException e) {
                        throw new AssertionError(e);
                    }
                } else if (site instanceof MethodInjectionSite) {
                    try {
                        MethodInjectionSite methodSite = (MethodInjectionSite) site;
                        Method method = methodSite.getSignature().getMethod(implementationClass);
                        injectors.put(attribute, new MethodInjector<T>(method, factory));
                    } catch (ClassNotFoundException e) {
                        throw new AssertionError(e);
                    } catch (NoSuchMethodException e) {
                        throw new AssertionError(e);
                    }
                }
            }
        }
        return injectors;
    }

    private void setUpdateableFactory(InjectableAttribute name, ObjectFactory<?> objectFactory, Object key) {
        // determine if object factory is present. if so, must be updated.
        ObjectFactory<?> factory = factories.get(name);
        if (factory == null) {
            Class<?> type = getMemberType(name);
            if (Map.class.equals(type)) {
                MapMultiplicityObjectFactory mapFactory = new MapMultiplicityObjectFactory();
                mapFactory.addObjectFactory(objectFactory, key);
                factories.put(name, mapFactory);
            } else if (Set.class.equals(type)) {
                SetMultiplicityObjectFactory setFactory = new SetMultiplicityObjectFactory();
                setFactory.addObjectFactory(objectFactory, key);
                factories.put(name, setFactory);
            } else if (List.class.equals(type)) {
                ListMultiplicityObjectFactory listFactory = new ListMultiplicityObjectFactory();
                listFactory.addObjectFactory(objectFactory, key);
                factories.put(name, listFactory);
            } else if (Collection.class.equals(type)) {
                ListMultiplicityObjectFactory listFactory = new ListMultiplicityObjectFactory();
                listFactory.addObjectFactory(objectFactory, key);
                factories.put(name, listFactory);
            } else {
                factories.put(name, objectFactory);
            }
        } else if (factory instanceof MultiplicityObjectFactory) {
            MultiplicityObjectFactory<?> multiplicityObjectFactory = (MultiplicityObjectFactory<?>) factory;
            multiplicityObjectFactory.addObjectFactory(objectFactory, key);
        } else {
            //update / overwrite  the factory
            factories.put(name, objectFactory);
        }
    }

    // FIXME this is a hack until can replace getMemberType/getGenericType as they assume a single injection site
    private InjectionSite findInjectionSite(InjectableAttribute attribute) {
        // try constructor
        for (int i = 0; i < cdiSources.size(); i++) {
            InjectableAttribute injectableAttribute = cdiSources.get(i);
            if (attribute.equals(injectableAttribute)) {
                return new ConstructorInjectionSite(constructor, i);
            }
        }
        // try postConstruction
        for (Map.Entry<InjectionSite, InjectableAttribute> entry : postConstruction.entrySet()) {
            if (entry.getValue().equals(attribute)) {
                return entry.getKey();
            }
        }
        return null;
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

    private ObjectFactory<?> createObjectFactory(String referenceType) {
        if ("java.util.Map".equals(referenceType)) {
            return new MapMultiplicityObjectFactory();
        } else if ("java.util.Set".equals(referenceType)) {
            return new SetMultiplicityObjectFactory();
        } else if ("java.util.List".equals(referenceType)) {
            return new ListMultiplicityObjectFactory();
        } else if ("java.util.Collection".equals(referenceType)) {
            return new ListMultiplicityObjectFactory();
        } else {
            return NULL_FACTORY;
        }
    }
}
