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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.fabric3.pojo.processor.ImplementationProcessorExtension;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.pojo.scdl.Resource;
import org.fabric3.spi.loader.LoaderContext;

/**
 * Resources annotation processor for JSR 250 and F3 resource annotations.
 * 
 * @version $Revision$ $Date$
 */
public class ResourceAnnotationProcessor extends ImplementationProcessorExtension {

    /**
     * @see org.fabric3.pojo.processor.ImplementationProcessorExtension#visitField(java.lang.reflect.Field,
     *      org.fabric3.pojo.scdl.PojoComponentType,
     *      org.fabric3.spi.loader.LoaderContext)
     */
    @Override
    public void visitField(Field field, PojoComponentType type, LoaderContext context) throws ProcessingException {
        processResource(field, type);
    }

    /**
     * @see org.fabric3.pojo.processor.ImplementationProcessorExtension#visitMethod(java.lang.reflect.Method,
     *      org.fabric3.pojo.scdl.PojoComponentType,
     *      org.fabric3.spi.loader.LoaderContext)
     */
    @Override
    public void visitMethod(Method method, PojoComponentType type, LoaderContext context) throws ProcessingException {
        processResource(method, type);
    }
    
    /*
     * Generic method for processing resources.
     */
    private void processResource(AccessibleObject member, PojoComponentType type) throws ProcessingException {
        
        javax.annotation.Resource jsr250Annotation = member.getAnnotation(javax.annotation.Resource.class);
        org.fabric3.api.annotation.Resource f3Annotation = member.getAnnotation(org.fabric3.api.annotation.Resource.class);
        
        if(jsr250Annotation == null && f3Annotation == null) {
            return;
        }
        
        if(jsr250Annotation != null && f3Annotation != null) {
            throw new ProcessingException("Either JSR 250 or F3 annotation is allowed");
        }
        
        String name = null;
        String mappedName = null;
        Class<?> resourceType = null;
        
        if(jsr250Annotation != null) {
            
            name = jsr250Annotation.name();
            mappedName = jsr250Annotation.mappedName();
            resourceType = jsr250Annotation.annotationType();
            
        } else {
            
            name = f3Annotation.name();
            mappedName = f3Annotation.mappedName();
            resourceType = f3Annotation.annotationType();
            
        }
        
        Resource<?> resource = createResource(name, resourceType, (Member) member);
        resource.setMappedName(mappedName);

        if (type.getResources().get(name) != null) {
            throw new ProcessingException("Duplicate resource:" + name);
        }
        
        type.add(resource);
        
    }
    
    /*
     * Creates resource object.
     */
    private <T> Resource<T> createResource(String name, Class<T> type, Member member) {
        return new Resource<T>(name, type, member);
    }

}
