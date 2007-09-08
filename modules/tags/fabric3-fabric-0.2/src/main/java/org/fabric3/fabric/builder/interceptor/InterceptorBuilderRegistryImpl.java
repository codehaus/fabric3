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
package org.fabric3.fabric.builder.interceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.builder.interceptor.InterceptorBuilderRegistry;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.wire.Interceptor;

/**
 * Default implementation of an InterceptorBuilderRegistry
 *
 * @version $Rev$ $Date$
 */
public class InterceptorBuilderRegistryImpl implements InterceptorBuilderRegistry {
    
    private Map<Class<? extends PhysicalInterceptorDefinition>, InterceptorBuilder<?, ?>> builders = 
        new ConcurrentHashMap<Class<? extends PhysicalInterceptorDefinition>, InterceptorBuilder<?, ?>>();

    /**
     * @see org.fabric3.spi.builder.interceptor.InterceptorBuilderRegistry#register(java.lang.Class, org.fabric3.spi.builder.interceptor.InterceptorBuilder)
     */
    public <PID extends PhysicalInterceptorDefinition> void register(Class<PID> clazz, InterceptorBuilder<PID, ?> builder) {
        builders.put(clazz, builder);
    }

    /**
     * @see org.fabric3.spi.builder.interceptor.InterceptorBuilderRegistry#unregister(java.lang.Class)
     */
    public <PID extends PhysicalInterceptorDefinition> void unregister(Class<PID> clazz) {
        builders.remove(clazz);
    }

    /**
     * @see org.fabric3.spi.builder.interceptor.InterceptorBuilderRegistry#build(org.fabric3.spi.model.physical.PhysicalInterceptorDefinition)
     */
    public <PID extends PhysicalInterceptorDefinition> Interceptor build(PID definition) throws BuilderException {
        
        @SuppressWarnings("unchecked")
        InterceptorBuilder<PID, ?> builder = (InterceptorBuilder<PID, ?>) builders.get(definition.getClass());
        if(builder == null) {
            throw new InterceptorBuilderNotFoundException(definition.getClass());
        }
        return builder.build(definition);
        
    }
    
}
