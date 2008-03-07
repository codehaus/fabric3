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

import java.util.Set;

import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Remotable;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.java.InvalidServiceContractException;
import org.fabric3.pojo.processor.ImplementationProcessorExtension;
import org.fabric3.pojo.processor.ImplementationProcessorService;
import static org.fabric3.pojo.processor.JavaIntrospectionHelper.getAllInterfaces;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ServiceDefinition;

/**
 * Processes an {@link org.osoa.sca.annotations.Service} annotation and updates the component type with corresponding {@link ServiceDefinition}s. Also
 * processes related {@link org.osoa.sca.annotations.Callback} annotations.
 *
 * @version $Rev$ $Date$
 */
public class ServiceProcessor extends ImplementationProcessorExtension {

    private ImplementationProcessorService implService;

    public ServiceProcessor(@Reference ImplementationProcessorService implService) {
        this.implService = implService;
    }

    public <T> void visitClass(Class<T> clazz,
                               PojoComponentType type,
                               IntrospectionContext context) throws ProcessingException {
        org.osoa.sca.annotations.Service annotation = clazz.getAnnotation(org.osoa.sca.annotations.Service.class);
        if (annotation == null) {
            Class<?> superClass = clazz.getSuperclass();
            while (superClass != null && !superClass.equals(Object.class)) {
                // scan superclasses
                annotation = superClass.getAnnotation(org.osoa.sca.annotations.Service.class);
                if (annotation != null) {
                    break;
                }
                superClass = superClass.getSuperclass();
            }
            if (annotation == null) {
                // scan intefaces for remotable
                Set<Class> interfaces = getAllInterfaces(clazz);
                for (Class<?> interfaze : interfaces) {
                    if (interfaze.isAnnotationPresent(Remotable.class) || interfaze.isAnnotationPresent(Callback.class)) {
                        ServiceDefinition service;
                        try {
                            service = implService.createService(interfaze, context.getTypeMapping());
                        } catch (InvalidServiceContractException e) {
                            throw new ProcessingException(e);
                        }
                        type.getServices().put(service.getName(), service);
                    }
                }
                return;
            }
        }
        Class<?>[] interfaces = annotation.interfaces();
        if (interfaces.length == 0) {
            Class<?> interfaze = annotation.value();
            if (Void.class.equals(interfaze)) {
                throw new IllegalServiceDefinitionException("No interfaces specified");
            } else {
                interfaces = new Class<?>[1];
                interfaces[0] = interfaze;
            }
        }
        for (Class<?> interfaze : interfaces) {
            if (!interfaze.isInterface()) {
                throw new InvalidServiceType("Service must be an interface", interfaze.getName());
            }
            ServiceDefinition service;
            try {
                service = implService.createService(interfaze, context.getTypeMapping());
            } catch (InvalidServiceContractException e) {
                throw new ProcessingException(e);
            }
            type.getServices().put(service.getName(), service);
        }
    }

}
