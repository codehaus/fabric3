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
package org.fabric3.fabric.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.ResourceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GeneratorNotFoundException;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.generator.ResourceWireGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Rev$ $Date$
 */
public class GeneratorRegistryImpl implements GeneratorRegistry {

    private Map<Class<?>, ComponentGenerator<? extends LogicalComponent<? extends Implementation<?>>>> componentGenerators =
        new ConcurrentHashMap<Class<?>, ComponentGenerator<? extends LogicalComponent<? extends Implementation<?>>>>();
    
    private Map<Class<? extends BindingDefinition>, BindingGenerator<?, ?, ? extends BindingDefinition>> bindingGenerators =
        new ConcurrentHashMap<Class<? extends BindingDefinition>, BindingGenerator<?, ?, ? extends BindingDefinition>>();
    
    private List<CommandGenerator> commandGenerators = new ArrayList<CommandGenerator>();
    
    private Map<QName, InterceptorDefinitionGenerator> interceptorDefinitionGenerators = 
        new ConcurrentHashMap<QName, InterceptorDefinitionGenerator>();
    
    private Map<Class<? extends ResourceDefinition>, ResourceWireGenerator<?, ? extends ResourceDefinition>> resourceWireGenerators =
        new ConcurrentHashMap<Class<? extends ResourceDefinition>, ResourceWireGenerator<?, ? extends ResourceDefinition>>();
    
    @Reference(required = false)
    public void setBindingGenerators(Map<Class<? extends BindingDefinition>, BindingGenerator<?, ?, ? extends BindingDefinition>> bindingGenerators) {
        this.bindingGenerators = bindingGenerators;
    }

    public <T extends Implementation<?>> void register(Class<T> clazz, ComponentGenerator<LogicalComponent<T>> generator) {
        componentGenerators.put(clazz, generator);
    }

    public <T extends Implementation<?>> void unregister(Class<T> clazz, ComponentGenerator<LogicalComponent<T>> generator) {
        componentGenerators.remove(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T extends Implementation<?>> ComponentGenerator<LogicalComponent<T>> getComponentGenerator(Class<T> clazz)  
        throws GeneratorNotFoundException {
        if (!componentGenerators.containsKey(clazz)) {
            throw new GeneratorNotFoundException(clazz);
        }
        return (ComponentGenerator<LogicalComponent<T>>) componentGenerators.get(clazz);
    }
    
    public <T extends BindingDefinition> void register(Class<T> clazz, BindingGenerator<?, ?, T> generator) {
        bindingGenerators.put(clazz, generator);
    }

    public <T extends BindingDefinition> void unregister(Class<T> clazz, BindingGenerator<?, ?, T> generator) {
        bindingGenerators.remove(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T extends BindingDefinition> BindingGenerator<?, ?, T> getBindingGenerator(Class<T> clazz) 
        throws GeneratorNotFoundException {        
        if (!bindingGenerators.containsKey(clazz)) {
            throw new GeneratorNotFoundException(clazz);
        }
        return (BindingGenerator<?, ?, T>) bindingGenerators.get(clazz);
    }

    public <T extends ResourceDefinition> void register(Class<T> clazz, ResourceWireGenerator<?, T> generator) {
        resourceWireGenerators.put(clazz, generator);
    }

    public <T extends ResourceDefinition> void unregister(Class<T> clazz, ResourceWireGenerator<?, T> generator) {
        resourceWireGenerators.remove(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T extends ResourceDefinition> ResourceWireGenerator<?, T> getResourceWireGenerator(Class<T> clazz) 
        throws GeneratorNotFoundException {      
        if (!resourceWireGenerators.containsKey(clazz)) {
            throw new GeneratorNotFoundException(clazz);
        }
        return (ResourceWireGenerator<?, T>) resourceWireGenerators.get(clazz);
    }
    
    public void register(QName extensionName, InterceptorDefinitionGenerator interceptorDefinitionGenerator) {
        interceptorDefinitionGenerators.put(extensionName, interceptorDefinitionGenerator);
    }

    public void unregister(QName extensionName, InterceptorDefinitionGenerator generator) {
        interceptorDefinitionGenerators.remove(extensionName);
    }

    public InterceptorDefinitionGenerator getInterceptorDefinitionGenerator(QName extensionName)
        throws GeneratorNotFoundException {
        if (!interceptorDefinitionGenerators.containsKey(extensionName)) {
            throw new GeneratorNotFoundException(extensionName);
        }
        return interceptorDefinitionGenerators.get(extensionName);
    }
    
    public void register(CommandGenerator generator) {
        commandGenerators.add(generator);
    }

    public void unregister(CommandGenerator generator) {
        commandGenerators.remove(generator);
    }

    public List<CommandGenerator> getCommandGenerators() {
        return commandGenerators;
    }

}
