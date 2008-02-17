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
package org.fabric3.groovy;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.instancefactory.InstanceFactoryBuildHelper;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilder;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderException;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.reflection.ReflectiveInstanceFactoryProvider;
import org.fabric3.scdl.ConstructorInjectionSite;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.ValueSource;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class GroovyInstanceFactoryBuilder<T>
        implements InstanceFactoryBuilder<ReflectiveInstanceFactoryProvider<T>, GroovyInstanceFactoryDefinition> {

    private final InstanceFactoryBuilderRegistry registry;
    private final InstanceFactoryBuildHelper helper;

    public GroovyInstanceFactoryBuilder(@Reference InstanceFactoryBuilderRegistry registry,
                                        @Reference InstanceFactoryBuildHelper helper) {
        this.registry = registry;
        this.helper = helper;
    }

    @Init
    public void init() {
        registry.register(GroovyInstanceFactoryDefinition.class, this);
    }

    public ReflectiveInstanceFactoryProvider<T> build(GroovyInstanceFactoryDefinition ifpd, ClassLoader cl)
            throws InstanceFactoryBuilderException {

        GroovyClassLoader gcl = new GroovyClassLoader(cl);
        try {
            Class<T> implClass = getImplClass(ifpd, gcl);
            
            Map<ValueSource, InjectionSite> injectionSites = ifpd.getInjectionSites();

            Constructor<T> ctr = helper.getConstructor(implClass, ifpd.getConstructor());
            ValueSource[] cdiSources = new ValueSource[ctr.getParameterTypes().length];
            for (Map.Entry<ValueSource, InjectionSite> entry : injectionSites.entrySet()) {
                InjectionSite injectionSite = entry.getValue();
                if (injectionSite.getElementType() == ElementType.CONSTRUCTOR) {
                    ConstructorInjectionSite constructorSite = (ConstructorInjectionSite) injectionSite;
                    cdiSources[constructorSite.getParam()] = entry.getKey();
                }
            }
            for (int i = 0; i < cdiSources.length; i++) {
                if (cdiSources[i] == null) {
                    throw new InstanceFactoryBuilderException("No source for constructor parameter " + i, ctr.getName());
                }
            }

            Method initMethod = helper.getMethod(implClass, ifpd.getInitMethod());
            Method destroyMethod = helper.getMethod(implClass, ifpd.getDestroyMethod());

            return new ReflectiveInstanceFactoryProvider<T>(ctr,
                                                            Arrays.asList(cdiSources),
                                                            injectionSites,
                                                            initMethod,
                                                            destroyMethod,
                                                            gcl);
        } catch (ClassNotFoundException e) {
            throw new InstanceFactoryBuilderException(e);
        } catch (NoSuchMethodException ex) {
            throw new InstanceFactoryBuilderException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<T> getImplClass(GroovyInstanceFactoryDefinition ifpd, GroovyClassLoader gcl)
            throws ClassNotFoundException, InstanceFactoryBuilderException {
        if (ifpd.getImplementationClass() != null) {
            try {
                return (Class<T>) helper.loadClass(gcl, ifpd.getImplementationClass());
            } catch (ClassNotFoundException e) {
                throw new InstanceFactoryBuilderException(e);
            }
        } else if (ifpd.getScriptName() != null) {
            try {
                URL script = gcl.getResource(ifpd.getScriptName());
                GroovyCodeSource source = new GroovyCodeSource(script);
                return gcl.parseClass(source);
            } catch (IOException e) {
                throw new InstanceFactoryBuilderException(e.getMessage(), ifpd.getScriptName(), e);
            }
        } else {
            throw new AssertionError();
        }
    }
}
