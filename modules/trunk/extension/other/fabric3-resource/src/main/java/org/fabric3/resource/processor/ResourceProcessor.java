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

import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.processor.DuplicateResourceException;
import org.fabric3.pojo.processor.ImplementationProcessorExtension;
import static org.fabric3.pojo.processor.JavaIntrospectionHelper.toPropertyName;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.resource.model.SystemSourcedResource;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.ContractProcessor;
import org.fabric3.introspection.InvalidServiceContractException;

/**
 * Processes an {@link @Resource} annotation, updating the component type with corresponding {@link
 * org.fabric3.pojo.scdl.JavaMappedResource}
 *
 * @version $Rev$ $Date$
 */
public class ResourceProcessor extends ImplementationProcessorExtension {
    
    private final ContractProcessor contractProcessor;

    public ResourceProcessor(@Reference ContractProcessor contractProcessor) {
        this.contractProcessor = contractProcessor;
    }

    public void visitMethod(Method method, PojoComponentType type, IntrospectionContext context) throws ProcessingException {
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

        SystemSourcedResource resource = createResource(name, resourceType, new MethodInjectionSite(method, 0), annotation.optional(), annotation.mappedName());

        type.add(resource);
        
    }

    public void visitField(Field field, PojoComponentType type, IntrospectionContext context) throws ProcessingException {

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

        SystemSourcedResource resource = createResource(name, fieldType, new FieldInjectionSite(field), annotation.optional(), annotation.mappedName());

        type.add(resource);
        
    }

    private SystemSourcedResource createResource(String name, Class<?> type, InjectionSite injectionSite, boolean optional, String mappedName)
            throws ProcessingException {
        
        try {
            ServiceContract<Type> serviceContract = contractProcessor.introspect(type);
            return new SystemSourcedResource(name, injectionSite, optional, mappedName, serviceContract);
        }  catch (InvalidServiceContractException e) {
            throw new ProcessingException(e);
        }
        
    }

}
