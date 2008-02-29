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
package org.fabric3.pojo.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.pojo.scdl.PojoComponentType;

/**
 * A convenience class for annotation processors which alleviates the need to implement unused callbacks
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public abstract class ImplementationProcessorExtension implements ImplementationProcessor {

    // Introspector registry
    private IntrospectionRegistry registry;

    /**
     * Injects the introspection registry.
     *
     * @param registry Introspection registry.
     */
    @Reference
    public void setRegistry(IntrospectionRegistry registry) {
        this.registry = registry;
    }

    /**
     * Registers with the registry.
     */
    @Init
    public void init() {
        registry.registerProcessor(this);
    }

    /**
     * Unregisters with the registry.
     */
    @Destroy
    public void destroy() {
        registry.unregisterProcessor(this);
    }

    public <T> void visitClass(Class<T> clazz, PojoComponentType type, IntrospectionContext context) throws ProcessingException {
    }

    public <T> void visitSuperClass(Class<T> clazz, PojoComponentType type, IntrospectionContext context) throws ProcessingException {
    }

    public void visitMethod(Method method, PojoComponentType type, IntrospectionContext context) throws ProcessingException {
    }

    public <T> void visitConstructor(Constructor<T> constructor, PojoComponentType type, IntrospectionContext context) throws ProcessingException {
    }

    public void visitField(Field field, PojoComponentType type, IntrospectionContext context) throws ProcessingException {
    }

    public <T> void visitEnd(Class<T> clazz, PojoComponentType type, IntrospectionContext context) throws ProcessingException {
    }

}
