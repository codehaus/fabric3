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

import static org.fabric3.pojo.processor.JavaIntrospectionHelper.toPropertyName;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.fabric3.pojo.processor.ImplementationProcessorExtension;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.JavaMappedResource;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.resource.model.SystemSourcedResource;
import org.fabric3.spi.loader.LoaderContext;

/**
 * Processes an {@link @Resource} annotation, updating the component type with corresponding {@link
 * org.fabric3.pojo.scdl.JavaMappedResource}
 *
 * @version $Rev$ $Date$
 */
public class ResourceProcessor extends ImplementationProcessorExtension {

    public ResourceProcessor() {
    }

    public void visitMethod(Method method, PojoComponentType type, LoaderContext context) throws ProcessingException {
        org.fabric3.api.annotation.Resource annotation = method.getAnnotation(org.fabric3.api.annotation.Resource.class);
        if (annotation == null) {
            return;
        }
        if (method.getParameterTypes().length != 1) {
            throw new IllegalResourceException("Resource setter must have one parameter", method.toString());
        }
        Class<?> resourceType = method.getParameterTypes()[0];

        String name = annotation.name();
        if (name.length() < 1) {
            name = toPropertyName(method.getName());
        }
        if (type.getResources().get(name) != null) {
            throw new DuplicateResourceException(name);
        }

        SystemSourcedResource<?> resource = createResource(name, resourceType, method, annotation.optional(), annotation.mappedName());

        type.add(resource);
        
    }

    public void visitField(Field field, PojoComponentType type, LoaderContext context) throws ProcessingException {

        org.fabric3.api.annotation.Resource annotation = field.getAnnotation(org.fabric3.api.annotation.Resource.class);
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
        String mappedName = annotation.mappedName();

        JavaMappedResource<?> resource = createResource(name, fieldType, field, annotation.optional(), annotation.mappedName());

        type.add(resource);
        
    }

    private <T> SystemSourcedResource<T> createResource(String name, Class<T> type, Member member, boolean optional, String mappedName) {
        return new SystemSourcedResource<T>(name, type, member, optional, mappedName);
    }

}
