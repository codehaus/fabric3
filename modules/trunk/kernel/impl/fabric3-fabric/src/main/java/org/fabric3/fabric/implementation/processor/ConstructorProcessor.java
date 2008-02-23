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

import java.lang.reflect.Constructor;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.pojo.processor.ImplementationProcessorExtension;
import org.fabric3.pojo.processor.ImplementationProcessorService;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.scdl.Signature;


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

    public <T> void visitConstructor(Constructor<T> constructor,
                                     PojoComponentType type,
                                     IntrospectionContext context) throws ProcessingException {
        org.osoa.sca.annotations.Constructor annotation = constructor.getAnnotation(org.osoa.sca.annotations.Constructor.class);
        if (annotation == null) {
            return;
        }

        if (type.getConstructor() != null) {
            String name = constructor.getDeclaringClass().getName();
            throw new DuplicateConstructorException("Multiple constructor definitions found", name);
        }

        String[] names = annotation.value();
        // check that if names have been provided then the number matches the number of parameters
        if (names.length != 1 || names[0].length() != 0) {
            if (names.length != constructor.getParameterTypes().length) {
                throw new InvalidConstructorException("Names in @Constructor do not match number of parameters");
            }
        }

        type.setConstructor(new Signature(constructor));

        service.processParameters(constructor, type, context);

/*
        Class<?>[] params = constructor.getParameterTypes();
        Annotation[][] annotations = constructor.getParameterAnnotations();
        List<String> injectionNames = definition.getInjectionNames();
        for (int i = 0; i < params.length; i++) {
            Class<?> param = params[i];
            Annotation[] paramAnnotations = annotations[i];
            try {
                if (!service.processParam(param,
                    paramTypes[i],
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
*/
    }
}
