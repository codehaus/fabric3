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
package org.fabric3.fabric.services.instancefactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.scdl.InjectionSiteMapping;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuildHelper;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilder;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderException;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.pojo.reflection.ReflectiveInstanceFactoryProvider;
import org.fabric3.scdl.ValueSource;

/**
 * Builds a reflection-based instance factory provider.
 *
 * @version $Date$ $Revision$
 */
@EagerInit
public class ReflectiveInstanceFactoryBuilder<T>
        implements InstanceFactoryBuilder<ReflectiveInstanceFactoryProvider<T>, InstanceFactoryDefinition> {

    private final InstanceFactoryBuilderRegistry registry;
    private final InstanceFactoryBuildHelper helper;

    public ReflectiveInstanceFactoryBuilder(@Reference InstanceFactoryBuilderRegistry registry,
                                            @Reference InstanceFactoryBuildHelper helper) {
        this.registry = registry;
        this.helper = helper;
    }

    @Init
    public void init() {
        registry.register(InstanceFactoryDefinition.class, this);
    }

    public ReflectiveInstanceFactoryProvider<T> build(InstanceFactoryDefinition ifpd, ClassLoader cl)
            throws InstanceFactoryBuilderException {

        try {
            @SuppressWarnings("unchecked")
            Class<T> implClass = (Class<T>) helper.loadClass(cl, ifpd.getImplementationClass());

            Constructor<T> ctr = helper.getConstructor(implClass, ifpd.getConstructor());

            Method initMethod = helper.getMethod(implClass, ifpd.getInitMethod());

            Method destroyMethod = helper.getMethod(implClass, ifpd.getDestroyMethod());

            List<ValueSource> ctrInjectSites = ifpd.getCdiSources();

            List<InjectionSiteMapping> mappings = ifpd.getInjectionSites();
            Map<ValueSource, Member> injectionSites = helper.getInjectionSites(implClass, mappings);
            return new ReflectiveInstanceFactoryProvider<T>(ctr,
                                                            ctrInjectSites,
                                                            injectionSites,
                                                            initMethod,
                                                            destroyMethod,
                                                            cl);

        } catch (ClassNotFoundException ex) {
            throw new InstanceFactoryBuilderException(ex);
        } catch (NoSuchMethodException ex) {
            throw new InstanceFactoryBuilderException(ex);
        } catch (NoSuchFieldException ex) {
            throw new InstanceFactoryBuilderException(ex);
        }
    }
}
