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
package org.fabric3.pojo.instancefactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.provision.InstanceFactoryDefinition;
import org.fabric3.pojo.reflection.ReflectiveInstanceFactoryProvider;
import org.fabric3.scdl.ConstructorInjectionSite;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectionSite;

/**
 * Builds a reflection-based instance factory provider.
 *
 * @version $Date$ $Revision$
 */
@EagerInit
public class ReflectiveInstanceFactoryBuilder<T> implements InstanceFactoryBuilder<ReflectiveInstanceFactoryProvider<T>, InstanceFactoryDefinition> {

    private final InstanceFactoryBuilderRegistry registry;
    private final InstanceFactoryBuildHelper helper;

    public ReflectiveInstanceFactoryBuilder(@Reference InstanceFactoryBuilderRegistry registry, @Reference InstanceFactoryBuildHelper helper) {
        this.registry = registry;
        this.helper = helper;
    }

    @Init
    public void init() {
        registry.register(InstanceFactoryDefinition.class, this);
    }

    public ReflectiveInstanceFactoryProvider<T> build(InstanceFactoryDefinition ifpd, ClassLoader cl) throws InstanceFactoryBuilderException {

        try {
            @SuppressWarnings("unchecked")
            Class<T> implClass = (Class<T>) helper.loadClass(cl, ifpd.getImplementationClass());
            Constructor<T> ctr = helper.getConstructor(implClass, ifpd.getConstructor());

            Map<InjectionSite, InjectableAttribute> injectionSites = ifpd.getConstruction();
            InjectableAttribute[] cdiSources = new InjectableAttribute[ctr.getParameterTypes().length];
            for (Map.Entry<InjectionSite, InjectableAttribute> entry : injectionSites.entrySet()) {
                InjectionSite site = entry.getKey();
                InjectableAttribute attribute = entry.getValue();
                ConstructorInjectionSite constructorSite = (ConstructorInjectionSite) site;
                cdiSources[constructorSite.getParam()] = attribute;
            }
            for (int i = 0; i < cdiSources.length; i++) {
                if (cdiSources[i] == null) {
                    String clazz = ctr.getName();
                    throw new InstanceFactoryBuilderException("No injection value for constructor parameter " + i + " in class " + clazz, clazz);
                }
            }

            Method initMethod = helper.getMethod(implClass, ifpd.getInitMethod());
            Method destroyMethod = helper.getMethod(implClass, ifpd.getDestroyMethod());

            Map<InjectionSite, InjectableAttribute> postConstruction = ifpd.getPostConstruction();
            List<InjectableAttribute> list = Arrays.asList(cdiSources);
            boolean reinjectable = ifpd.isReinjectable();

            return new ReflectiveInstanceFactoryProvider<T>(ctr, list, postConstruction, initMethod, destroyMethod, reinjectable, cl);
        } catch (ClassNotFoundException ex) {
            throw new InstanceFactoryBuilderException(ex);
        } catch (NoSuchMethodException ex) {
            throw new InstanceFactoryBuilderException(ex);
        }
    }
}
