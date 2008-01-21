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
package org.fabric3.spi.generator;

import java.util.List;

import javax.xml.namespace.QName;

import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.ResourceDefinition;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * A registry for {@link ComponentGenerator}s, {@link BindingGenerator}s, {@link InterceptorDefinitionGenerator}s and
 * {@link ResourceGenerator}s. Generators are responsible for producing physical model objects that are provisioned to
 * service nodes from their logical counterparts.
 *
 * @version $Rev$ $Date$
 */
public interface GeneratorRegistry {

    /**
     * Registers a component generator.
     *
     * @param clazz the implementation type the generator handles
     * @param generator the generator to register
     */
    <T extends Implementation<?>> void register(Class<T> clazz, ComponentGenerator<LogicalComponent<T>> generator);

    /**
     * Unregisters a component generator.
     *
     * @param clazz the implementation type the generator handles
     * @param generator the generator to unregister
     */
    <T extends Implementation<?>> void unregister(Class<T> clazz, ComponentGenerator<LogicalComponent<T>> generator);

    /**
     * Gets a component generator for the specified implementation.
     * 
     * @param clazz the implementation type the generator handles.
     * @return a the component generator for that implementation type
     * @throws GeneratorNotFoundException if no generator is registered for the implementation type
     */
    <T extends Implementation<?>> ComponentGenerator<LogicalComponent<T>> getComponentGenerator(Class<T> clazz) 
        throws GeneratorNotFoundException;

    /**
     * Registers a binding generator.
     *
     * @param clazz the binding type the generator handles
     * @param generator the generator to register
     */
    <T extends BindingDefinition> void register(Class<T> clazz, BindingGenerator<?, ?, T> generator);

    /**
     * Unregisters a binding generator
     * @param clazz the binding type the generator handles
     * @param generator the generator to unregister
     */
    <T extends BindingDefinition> void unregister(Class<T> clazz, BindingGenerator<?, ?, T> generator);

    /**
     * Gets a binding generator for the specified binding class.
     * 
     * @param clazz The binding type type the generator handles.
     * @return The registered binding generator.
     * @throws GeneratorNotFoundException if no generator is registered for the binding type
     */
    <T extends BindingDefinition> BindingGenerator<?, ?, T> getBindingGenerator(Class<T> clazz) 
        throws GeneratorNotFoundException;

    /**
     * Registers a resource wire generator.
     *
     * @param clazz The resource type the generator handles.
     * @param generator The generator to register.
     */
    <T extends ResourceDefinition> void register(Class<T> clazz, ResourceWireGenerator<?, T> generator);

    /**
     * Unregisters a resource wire generator.
     *
     * @param clazz the resource type the generator handles
     * @param generator the generator to register
     */
    <T extends ResourceDefinition> void unregister(Class<T> clazz, ResourceWireGenerator<?, T> generator);

    /**
     * Gets the resource wire generator for the resource type.
     * 
     * @param clazz the resource type the generator handles
     * @return the registered resource wire generator
     * @throws GeneratorNotFoundException if no generator is registered for the resource type
     */
    <T extends ResourceDefinition> ResourceWireGenerator<?, T> getResourceWireGenerator(Class<T> clazz) 
        throws GeneratorNotFoundException;
    
    /**
     * Registers an interceptor generator by type.
     * 
     * @param extensionName fully qualified name of the extension
     * @param generator interceptor generator to register
     */
    void register(QName extensionName, InterceptorDefinitionGenerator generator);

    /**
     * Registers an interceptor generator by type.
     *
     * @param extensionName fully qualified name of the extension
     * @param generator interceptor generator to register
     */
    void unregister(QName extensionName, InterceptorDefinitionGenerator generator);

    /**
     * Gets the interceptor definition generator for the qualified name.
     * 
     * @param extensionName qualified name of the policy extension
     * @return Interceptor definition generator
     * @throws GeneratorNotFoundException if no generator is registered for the policy extension type
     */
    InterceptorDefinitionGenerator getInterceptorDefinitionGenerator(QName extensionName) 
        throws GeneratorNotFoundException;
    
    /**
     * Registers a command generator.
     *
     * @param generator the generator to register
     */
    void register(CommandGenerator generator);

    /**
     * Unregisters a command generator.
     *
     * @param generator the generator to unregister
     */
    void unregister(CommandGenerator generator);

    /**
     * Gets all the registered command generators.
     * 
     * @return All the registered command generators.
     */
    List<CommandGenerator> getCommandGenerators();

}
