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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.implementation.java.ConstructorDefinition;
import org.fabric3.extension.implementation.java.ImplementationProcessorExtension;
import org.fabric3.spi.implementation.java.ImplementationProcessorService;
import org.fabric3.spi.implementation.java.PojoComponentType;
import org.fabric3.spi.implementation.java.ProcessingException;


/**
 * Handles processing of a constructor decorated with {@link org.osoa.sca.annotations.Constructor}
 *
 * @version $Rev$ $Date$
 */
@SuppressWarnings("unchecked")
public class ConstructorProcessor extends ImplementationProcessorExtension {

    private ImplementationProcessorService service;

    public ConstructorProcessor(@Reference ImplementationProcessorService service) {
        this.service = service;
    }

    public <T> void visitClass(Class<T> clazz,
                               PojoComponentType type,
                               LoaderContext context) throws ProcessingException {
        Constructor[] ctors = clazz.getConstructors();
        boolean found = false;
        for (Constructor constructor : ctors) {
            if (constructor.getAnnotation(org.osoa.sca.annotations.Constructor.class) != null) {
                if (found) {
                    String name = constructor.getDeclaringClass().getName();
                    throw new DuplicateConstructorException("Multiple constructors marked with @Constructor", name);
                }
                found = true;
            }
        }
    }

    public <T> void visitConstructor(Constructor<T> constructor,
                                     PojoComponentType type,
                                     LoaderContext context) throws ProcessingException {
        org.osoa.sca.annotations.Constructor annotation =
            constructor.getAnnotation(org.osoa.sca.annotations.Constructor.class);
        if (annotation == null) {
            return;
        }
        ConstructorDefinition<?> definition = type.getConstructorDefinition();
        if (definition != null && !definition.getConstructor().equals(constructor)) {
            String name = constructor.getDeclaringClass().getName();
            throw new DuplicateConstructorException("Multiple constructor definitions found", name);
        } else if (definition == null) {
            definition = new ConstructorDefinition(constructor);
        }
        Class<?>[] params = constructor.getParameterTypes();
        String[] names = annotation.value();
        Annotation[][] annotations = constructor.getParameterAnnotations();
        List<String> injectionNames = definition.getInjectionNames();
        for (int i = 0; i < params.length; i++) {
            Class<?> param = params[i];
            Annotation[] paramAnnotations = annotations[i];
            try {
                if (!service.processParam(param,
                    constructor.getGenericParameterTypes()[i],
                    paramAnnotations,
                    names,
                    i,
                    type,
                    injectionNames)) {
                    String name = (i < names.length) ? names[i] : "";
                    service.addName(injectionNames, i, name);
                }
            } catch (ProcessingException e) {
                e.setMember(constructor);
                throw e;
            }
        }
        if (names.length != 0 && names[0].length() != 0 && names.length != params.length) {
            throw new InvalidConstructorException("Names in @Constructor do not match number of parameters");
        }
        type.setConstructorDefinition(definition);
    }
}
