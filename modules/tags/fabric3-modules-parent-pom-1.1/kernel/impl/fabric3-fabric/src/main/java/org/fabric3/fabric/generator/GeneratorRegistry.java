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
package org.fabric3.fabric.generator;

import javax.xml.namespace.QName;

import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.ResourceDefinition;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.generator.GeneratorNotFoundException;
import org.fabric3.spi.generator.ResourceWireGenerator;

/**
 * A registry for {@link ComponentGenerator}s, {@link BindingGenerator}s, and {@link InterceptorDefinitionGenerator}s . Generators are responsible for
 * producing physical model objects that are provisioned to service nodes from their logical counterparts.
 *
 * @version $Rev$ $Date$
 */
public interface GeneratorRegistry {

    /**
     * Gets a component generator for the specified implementation.
     *
     * @param clazz the implementation type the generator handles.
     * @return a the component generator for that implementation type
     * @throws GeneratorNotFoundException if no generator is registered for the implementation type
     */
    <T extends Implementation<?>> ComponentGenerator<LogicalComponent<T>> getComponentGenerator(Class<T> clazz) throws GeneratorNotFoundException;

    /**
     * Gets a binding generator for the specified binding class.
     *
     * @param clazz The binding type type the generator handles.
     * @return The registered binding generator.
     * @throws GeneratorNotFoundException if no generator is registered for the binding type
     */
    <T extends BindingDefinition> BindingGenerator<T> getBindingGenerator(Class<T> clazz) throws GeneratorNotFoundException;

    /**
     * Gets the resource wire generator for the resource type.
     *
     * @param clazz the resource type the generator handles
     * @return the registered resource wire generator
     * @throws GeneratorNotFoundException if no generator is registered for the resource type
     */
    <T extends ResourceDefinition> ResourceWireGenerator<T> getResourceWireGenerator(Class<T> clazz) throws GeneratorNotFoundException;

    /**
     * Gets the interceptor definition generator for the qualified name.
     *
     * @param extensionName qualified name of the policy extension
     * @return Interceptor definition generator
     * @throws GeneratorNotFoundException if no generator is registered for the policy extension type
     */
    InterceptorDefinitionGenerator getInterceptorDefinitionGenerator(QName extensionName) throws GeneratorNotFoundException;

}
