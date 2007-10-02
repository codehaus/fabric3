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
package org.fabric3.jpa.processor;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.fabric3.jpa.PersistenceUnitResource;
import org.fabric3.pojo.processor.ImplementationProcessorExtension;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.spi.loader.LoaderContext;

/**
 * Implementation processor for persistence unit annotations.
 * 
 * @version $Revision$ $Date$
 */
public class PersistenceUnitAnnotationProcessor extends ImplementationProcessorExtension {

    /**
     * @see org.fabric3.pojo.processor.ImplementationProcessorExtension#visitMethod(java.lang.reflect.Method, 
     *                                                                              org.fabric3.pojo.scdl.PojoComponentType, 
     *                                                                              org.fabric3.spi.loader.LoaderContext)
     */
    public void visitMethod(Method method, PojoComponentType type, LoaderContext context) throws ProcessingException {
        
        PersistenceUnit annotation = method.getAnnotation(PersistenceUnit.class);
        if(annotation == null) {
            return;
        }
        
        checkMethod(method);
        
        processAnnotation(type, method, annotation);
        
    }

    /**
     * @see org.fabric3.pojo.processor.ImplementationProcessorExtension#visitField(java.lang.reflect.Field, 
     *                                                                             org.fabric3.pojo.scdl.PojoComponentType, 
     *                                                                             org.fabric3.spi.loader.LoaderContext)
     */
    public void visitField(Field field, PojoComponentType type, LoaderContext context) throws ProcessingException {
        
        PersistenceUnit annotation = field.getAnnotation(PersistenceUnit.class);
        if(annotation == null) {
            return;
        }
        
        Class<?> resourceType = field.getType();
        if(!EntityManagerFactory.class.isAssignableFrom(resourceType)) {
            throw new ProcessingException("Field is not an entity manager factory", field.toString());
        }
        
        processAnnotation(type, field, annotation);
        
    }

    /*
     * Processes the annotation.
     */
    private void processAnnotation(PojoComponentType type, AccessibleObject accessibleObject, PersistenceUnit annotation) {

        String name = annotation.name();
        String unitName = annotation.unitName();

        type.add(new PersistenceUnitResource(name, unitName, (Member) accessibleObject));
        
    }

    /*
     * Checks the method.
     */
    private void checkMethod(Method method) throws ProcessingException {
        
        boolean propertyFound = false;
        
        try {
            
            BeanInfo beanInfo = Introspector.getBeanInfo(method.getDeclaringClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            
            for(PropertyDescriptor pd : propertyDescriptors) {
                Method writeMethod = pd.getWriteMethod();
                if(writeMethod != null && method.equals(writeMethod)) {
                    Class<?> propertyType = pd.getPropertyType();
                    if(!EntityManagerFactory.class.isAssignableFrom(propertyType)) {
                        throw new ProcessingException("Invalid property type " + propertyType);
                    }
                    propertyFound = true;
                }
                
            }
        } catch (IntrospectionException e) {
            throw new ProcessingException(e);
        }
        
        if(!propertyFound) {
            throw new ProcessingException("Method is not a property setter " + method.toString());
        }
        
    }

}
