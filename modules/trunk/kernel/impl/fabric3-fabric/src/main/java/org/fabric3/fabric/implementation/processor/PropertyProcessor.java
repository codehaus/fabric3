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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.osoa.sca.annotations.Property;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.pojo.processor.DuplicatePropertyException;
import org.fabric3.pojo.processor.IllegalPropertyException;
import org.fabric3.pojo.processor.ImplementationProcessorExtension;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.JavaMappedProperty;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.MethodInjectionSite;

/**
 * Processes an {@link @Property} annotation, updating the component type with corresponding {@link org.fabric3.pojo.scdl.JavaMappedProperty}
 *
 * @version $Rev$ $Date$
 */
public class PropertyProcessor extends ImplementationProcessorExtension {
    protected String getName(Property annotation) {
        return annotation.name();
    }

    public void visitMethod(
            Method method,
            PojoComponentType type,
            IntrospectionContext context) throws ProcessingException {
        Property annotation = method.getAnnotation(Property.class);
        if (annotation == null) {
            return;
        }

        if (!Void.TYPE.equals(method.getReturnType())) {
            throw new IllegalPropertyException("Method does not have void return type", method.toString());
        }
        Class[] paramTypes = method.getParameterTypes();
        if (paramTypes.length != 1) {
            throw new IllegalPropertyException("Method must have a single parameter", method.toString());
        }
        Class<?> javaType = paramTypes[0];

        String name = getName(annotation);
        if (name == null || name.length() == 0) {
            name = method.getName();
            if (name.startsWith("set")) {
                name = toPropertyName(method.getName());
            }
        }

        Map<String, JavaMappedProperty<?>> properties = type.getProperties();
        if (properties.containsKey(name)) {
            throw new DuplicatePropertyException(name);
        }

        Class<?> baseType = getBaseType(javaType, method.getGenericParameterTypes()[0]);
        JavaMappedProperty<?> property = createProperty(name, baseType, new MethodInjectionSite(method, 0));
        if (javaType.isArray() || Collection.class.isAssignableFrom(javaType)) {
            property.setMany(true);
        }

        property.setRequired(annotation.required());
        properties.put(name, property);
    }

    public void visitField(
            Field field,
            PojoComponentType type,
            IntrospectionContext context) throws ProcessingException {

        Property annotation = field.getAnnotation(Property.class);
        if (annotation == null) {
            return;
        }

        Class<?> javaType = field.getType();

        String name = getName(annotation);
        if (name == null || name.length() == 0) {
            name = field.getName();
        }

        Map<String, JavaMappedProperty<?>> properties = type.getProperties();
        if (properties.containsKey(name)) {
            throw new DuplicatePropertyException(name);
        }

        Class<?> baseType = getBaseType(javaType, field.getGenericType());
        JavaMappedProperty<?> property = createProperty(name, baseType, new FieldInjectionSite(field));
        if (javaType.isArray() || Collection.class.isAssignableFrom(javaType)) {
            property.setMany(true);
        }

        property.setRequired(annotation.required());
        properties.put(name, property);
    }

    protected <T> JavaMappedProperty<T> createProperty(String name, Class<T> javaType, InjectionSite injectionSite)
            throws ProcessingException {
        return new JavaMappedProperty<T>(name, null, javaType, injectionSite);
    }

    public static String toPropertyName(String name) {
        if (!name.startsWith("set")) {
            return name;
        }
        return Character.toLowerCase(name.charAt(3)) + name.substring(4);
    }
}
