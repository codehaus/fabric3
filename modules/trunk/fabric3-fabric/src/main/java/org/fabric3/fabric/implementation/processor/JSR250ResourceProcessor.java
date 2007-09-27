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
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.pojo.processor.ImplementationProcessorExtension;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.pojo.scdl.Resource;
import org.fabric3.pojo.processor.ProcessingException;

/**
 * Processes an {@link @Resource} annotation, updating the component type with corresponding {@link
 * org.fabric3.pojo.scdl.Resource}
 *
 * @version $Rev: 751 $ $Date: 2007-08-16 14:50:14 -0500 (Thu, 16 Aug 2007) $
 */
public class JSR250ResourceProcessor extends ImplementationProcessorExtension {

    //TODO suport superclass injections
    public JSR250ResourceProcessor() {
    }

    public <T> void visitClass(Class<T> clazz, PojoComponentType type, LoaderContext context) throws ProcessingException {
        /* TODO according to the JSR250 spec if a class is annotated with the
        resource annotation it signifies that the class will look up the
        resource at runtime. Since the JSR does not define a lookup method
        method (maybe jndi?) I am unsure if this needs to be processed or not
        since we are only interested in dependency injection
         */
        javax.annotation.Resources resources = clazz.getAnnotation(javax.annotation.Resources.class);
        if (resources != null) {
            for (javax.annotation.Resource resource : resources.value()) {
                //TODO declare intent
            }
        }
        javax.annotation.Resource resource = clazz.getAnnotation(javax.annotation.Resource.class);
        if (resource != null) {
            //TODO declare intent
        }
    }

    public void visitMethod(Method method, PojoComponentType type, LoaderContext context) throws ProcessingException {
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

        String mappedName = annotation.mappedName();
        Resource<?> resource = createResource(name, declaredType, method);
        resource.setOptional(false);
        if (mappedName.length() > 0) {
            resource.setMappedName(mappedName);
        }
        //TODO test handling of super methods, support sharable
        type.add(resource);
    }

    public void visitField(Field field, PojoComponentType type, LoaderContext context) throws ProcessingException {

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
        String mappedName = annotation.mappedName();

        Resource<?> resource = createResource(name, declaredType, field);
        resource.setOptional(false);
        if (mappedName.length() > 0) {
            resource.setMappedName(mappedName);
        }
        //TODO test handling of super fields, support sharable
        type.add(resource);
    }

    public <T> Resource<T> createResource(String name, Class<T> type, Member member) {
        return new Resource<T>(name, type, member);
    }

    public <T> void visitConstructor(Constructor<T> constructor, PojoComponentType type, LoaderContext context) throws ProcessingException {
    }
}
