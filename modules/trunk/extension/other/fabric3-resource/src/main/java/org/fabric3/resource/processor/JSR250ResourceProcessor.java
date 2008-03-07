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
package org.fabric3.resource.processor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.fabric3.pojo.processor.ImplementationProcessorExtension;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.processor.DuplicateResourceException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.resource.model.SystemSourcedResource;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.java.ContractProcessor;
import org.fabric3.introspection.java.InvalidServiceContractException;
import org.fabric3.introspection.java.TypeMapping;

import org.osoa.sca.annotations.Reference;

/**
 * Processes an {@link @Resource} annotation
 *
 * @version $Rev: 751 $ $Date: 2007-08-16 14:50:14 -0500 (Thu, 16 Aug 2007) $
 */
public class JSR250ResourceProcessor extends ImplementationProcessorExtension {
    
    private final ContractProcessor contractProcessor;

    public JSR250ResourceProcessor(@Reference ContractProcessor contractProcessor) {
        this.contractProcessor = contractProcessor;
    }

    public void visitMethod(Method method, PojoComponentType type, IntrospectionContext context) throws ProcessingException {
        javax.annotation.Resource annotation = method.getAnnotation(javax.annotation.Resource.class);
        if (annotation == null) {
            return;
        }
        if (method.getParameterTypes().length != 1) {
            throw new IllegalResourceException("Resource setter must have one parameter", method.toString());
        }

        String methodName = method.getName();
        if (methodName.startsWith("set") && methodName.length() > 3) {
            methodName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        } else {
            throw new IllegalResourceException("Setter method names must begin with set", method.toString());
        }

        if (void.class != method.getReturnType()) {
            throw new IllegalResourceException("Setter method must have void return type", method.toString());
        }

        String name = annotation.name();
        if (name.length() < 1) {
            name = methodName;
        }

        if (type.getResources().get(name) != null) {
            throw new DuplicateResourceException(name);
        }

        Class<?> methodParameterType = method.getParameterTypes()[0];
        Class<?> declaredType = annotation.type();
        if (declaredType != Object.class) {
            if (!methodParameterType.isAssignableFrom(declaredType)) {
                throw new IllegalResourceException("Resource type " + declaredType + " is not compatible with method parameter", method.toString());
            }
        } else {
            declaredType = methodParameterType;
        }

        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        SystemSourcedResource resource = createResource(name, declaredType, false, annotation.mappedName(), context.getTypeMapping());

        type.add(resource, site);
    }

    public void visitField(Field field, PojoComponentType type, IntrospectionContext context) throws ProcessingException {

        javax.annotation.Resource annotation = field.getAnnotation(javax.annotation.Resource.class);
        if (annotation == null) {
            return;
        }
        String name = annotation.name();
        if (name.length() < 1) {
            name = field.getName();
        }
        if (type.getResources().get(name) != null) {
            throw new DuplicateResourceException(name);
        }

        Class<?> fieldType = field.getType();
        Class<?> declaredType = annotation.type();
        if (declaredType != Object.class) {
            if (!fieldType.isAssignableFrom(declaredType)) {
                throw new IllegalResourceException("Resource type " + declaredType + " is not compatible with field ", field.toString());
            }
        } else {
            declaredType = fieldType;
        }

        FieldInjectionSite site = new FieldInjectionSite(field);
        SystemSourcedResource resource = createResource(name, declaredType, false, annotation.mappedName(), context.getTypeMapping());

        type.add(resource, site);
    }

    private SystemSourcedResource createResource(String name, Class<?> type, boolean optional, String mappedName, TypeMapping typeMapping)
            throws ProcessingException {
        
        try {
            ServiceContract<Type> serviceContract = contractProcessor.introspect(typeMapping, type);
            return new SystemSourcedResource(name, optional, mappedName, serviceContract);
        }  catch (InvalidServiceContractException e) {
            throw new ProcessingException(e);
        }
        
    }

}
