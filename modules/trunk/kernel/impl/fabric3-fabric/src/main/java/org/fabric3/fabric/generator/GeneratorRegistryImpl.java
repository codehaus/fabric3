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
package org.fabric3.fabric.generator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.ResourceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GeneratorNotFoundException;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.generator.ResourceWireGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * @version $Rev$ $Date$
 */
public class GeneratorRegistryImpl implements GeneratorRegistry {

    private Map<Class<?>, ComponentGenerator<?>> componentGenerators = new ConcurrentHashMap<Class<?>, ComponentGenerator<?>>();

    private Map<Class<?>, BindingGenerator<?>> bindingGenerators = new ConcurrentHashMap<Class<?>, BindingGenerator<?>>();

    private Map<QName, InterceptorDefinitionGenerator> interceptorGenerators =  new ConcurrentHashMap<QName, InterceptorDefinitionGenerator>();

    private Map<Class<?>, ResourceWireGenerator<?>> resourceGenerators = new ConcurrentHashMap<Class<?>, ResourceWireGenerator<?>>();

    @Reference(required = false)
    public void setBindingGenerators(Map<Class<?>, BindingGenerator<?>> bindingGenerators) {
        this.bindingGenerators = bindingGenerators;
    }

    @Reference(required = false)
    public void setComponentGenerators(Map<Class<?>, ComponentGenerator<?>> componentGenerators) {
        this.componentGenerators = componentGenerators;
    }

    @Reference(required = false)
    public void setResourceGenerators(Map<Class<?>, ResourceWireGenerator<?>> resourceGenerators) {
        this.resourceGenerators = resourceGenerators;
    }

    @Reference(required = false)
    public void setInterceptorGenerators(Map<QName, InterceptorDefinitionGenerator> interceptorGenerators) {
        this.interceptorGenerators = interceptorGenerators;
    }

    public <T extends Implementation<?>> void register(Class<T> clazz, ComponentGenerator<LogicalComponent<T>> generator) {
        componentGenerators.put(clazz, generator);
    }

    public <T extends ResourceDefinition> void register(Class<T> clazz, ResourceWireGenerator<T> generator) {
        resourceGenerators.put(clazz, generator);
    }

    public <T extends BindingDefinition> void register(Class<T> clazz, BindingGenerator<T> generator) {
        bindingGenerators.put(clazz, generator);
    }

    @SuppressWarnings("unchecked")
    public <T extends Implementation<?>> ComponentGenerator<LogicalComponent<T>> getComponentGenerator(Class<T> clazz)
            throws GeneratorNotFoundException {
        if (!componentGenerators.containsKey(clazz)) {
            throw new GeneratorNotFoundException(clazz);
        }
        return (ComponentGenerator<LogicalComponent<T>>) componentGenerators.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T extends BindingDefinition> BindingGenerator<T> getBindingGenerator(Class<T> clazz)
            throws GeneratorNotFoundException {
        if (!bindingGenerators.containsKey(clazz)) {
            throw new GeneratorNotFoundException(clazz);
        }
        return (BindingGenerator<T>) bindingGenerators.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T extends ResourceDefinition> ResourceWireGenerator<T> getResourceWireGenerator(Class<T> clazz)
            throws GeneratorNotFoundException {
        if (!resourceGenerators.containsKey(clazz)) {
            throw new GeneratorNotFoundException(clazz);
        }
        return (ResourceWireGenerator<T>) resourceGenerators.get(clazz);
    }

    public InterceptorDefinitionGenerator getInterceptorDefinitionGenerator(QName extensionName)
            throws GeneratorNotFoundException {
        if (!interceptorGenerators.containsKey(extensionName)) {
            throw new GeneratorNotFoundException(extensionName);
        }
        return interceptorGenerators.get(extensionName);
    }

}
