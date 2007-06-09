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
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.InstanceFactory;
import org.fabric3.spi.component.InstanceFactoryProvider;
import org.fabric3.spi.model.instance.ValueSource;

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
    private final List<ValueSource> constructorNames;
    private final Map<ValueSource, Member> injectionSites;
    private final EventInvoker<T> initInvoker;
    private final EventInvoker<T> destroyInvoker;
    private final Map<ValueSource, ObjectFactory<?>> factories = new HashMap<ValueSource, ObjectFactory<?>>();

    public ReflectiveInstanceFactoryProvider(Constructor<T> constructor,
                                             List<ValueSource> constructorNames,
                                             Map<ValueSource, Member> injectionSites,
                                             Method initMethod,
                                             Method destroyMethod) {
        this.implementationClass = constructor.getDeclaringClass();
        this.constructor = constructor;
        this.constructorNames = constructorNames;
        this.injectionSites = injectionSites;
        this.initInvoker = initMethod == null ? null : new MethodEventInvoker<T>(initMethod);
        this.destroyInvoker = destroyMethod == null ? null : new MethodEventInvoker<T>(destroyMethod);
    }

    public void setObjectFactory(ValueSource name, ObjectFactory<?> objectFactory) {
        factories.put(name, objectFactory);
    }

    public Class<?> getMemberType(ValueSource valueSource) {
        Member member = injectionSites.get(valueSource);
        if(member != null) {
            if(member instanceof Field) {
                return ((Field) member).getType();
            } else {
                return ((Method) member).getParameterTypes()[0];
            }
        } else {
            int index = constructorNames.indexOf(valueSource);
            if(index >= 0) {
                return constructor.getParameterTypes()[index];
            }
        }
        return null;
    }

    public Class<T> getImplementationClass() {
        return implementationClass;
    }

    public InstanceFactory<T> createFactory() {
        ObjectFactory<?>[] initArgs = getConstructorArgs();
        Injector<T>[] injectors = getInjectors();
        return new ReflectiveInstanceFactory<T>(constructor, initArgs, injectors, initInvoker, destroyInvoker);
    }

    protected ObjectFactory<?>[] getConstructorArgs() {
        ObjectFactory<?>[] initArgs = new ObjectFactory<?>[constructorNames.size()];
        for (int i = 0; i < initArgs.length; i++) {
            ValueSource name = constructorNames.get(i);
            ObjectFactory<?> factory = factories.get(name);
            if (factory == null) {
                factory = NULL_FACTORY;
            }
            initArgs[i] = factory;
        }
        return initArgs;
    }
                 
    protected Injector<T>[] getInjectors() {
        // work around JDK1.5 issue with allocating generic arrays
        @SuppressWarnings("unchecked")
        Injector<T>[] injectors = (Injector<T>[]) new Injector[injectionSites.size()];

        int i = 0;
        for (Map.Entry<ValueSource, Member> entry : injectionSites.entrySet()) {
            ValueSource name = entry.getKey();
            Member site = entry.getValue();
            ObjectFactory<?> factory = factories.get(name);
            assert factory != null;
            if (site instanceof Field) {
                injectors[i++] = new FieldInjector<T>((Field) site, factory);
            } else if (site instanceof Method) {
                injectors[i++] = new MethodInjector<T>((Method) site, factory);
            } else {
                throw new AssertionError(String.valueOf(site));
            }
        }
        return injectors;
    }
}
