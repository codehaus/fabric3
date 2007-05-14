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
package org.fabric3.fabric.implementation.processor;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.implementation.java.ImplementationProcessorExtension;
import org.fabric3.spi.implementation.java.PojoComponentType;
import org.fabric3.spi.implementation.java.ProcessingException;
import org.fabric3.spi.model.type.Scope;
import org.fabric3.spi.component.ScopeRegistry;
import org.osoa.sca.annotations.Reference;

/**
 * Processes the {@link org.fabric3.spi.model.type.Scope} annotation and updates the component type with the corresponding implmentation scope
 *
 * @version $Rev$ $Date$
 */
public class ScopeProcessor extends ImplementationProcessorExtension {
    private final ScopeRegistry scopeRegistry;

    public ScopeProcessor(@Reference ScopeRegistry scopeRegistry) {
        this.scopeRegistry = scopeRegistry;
    }

    public <T> void visitClass(Class<T> clazz,
                               PojoComponentType type,
                               LoaderContext context)
        throws ProcessingException {
        org.osoa.sca.annotations.Scope annotation = clazz.getAnnotation(org.osoa.sca.annotations.Scope.class);
        if (annotation == null) {
            return;
        }
        
        String name = annotation.value();
        Scope<?> scope = scopeRegistry.getScope(name);
        if (scope == null) {
            throw new UnknownScopeException("Unknown scope in @Scope annotation on " + clazz.getSimpleName(),
                    clazz.getName());
        }
        type.setImplementationScope(scope);
    }
}
