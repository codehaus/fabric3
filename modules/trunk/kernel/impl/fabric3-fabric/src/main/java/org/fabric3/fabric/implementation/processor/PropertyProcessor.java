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
import java.util.Map;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.pojo.processor.ImplementationProcessorExtension;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.MethodInjectionSite;

/**
 * Processes an {@link @Property} annotation.
 *
 * @version $Rev$ $Date$
 */
public class PropertyProcessor extends ImplementationProcessorExtension {
    private final IntrospectionHelper helper;

    public PropertyProcessor(@Reference IntrospectionHelper helper) {
        this.helper = helper;
    }

    protected String getName(Property annotation) {
        return annotation.name();
    }

    public void visitMethod(Method method, PojoComponentType type, IntrospectionContext context) throws ProcessingException {
        try {
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

            String name = helper.getSiteName(method, annotation.name());

            Map<String, org.fabric3.scdl.Property> properties = type.getProperties();
            if (properties.containsKey(name)) {
                throw new DuplicatePropertyException(name);
            }

            MethodInjectionSite site = new MethodInjectionSite(method, 0);
            org.fabric3.scdl.Property property = new org.fabric3.scdl.Property(name, null);
            property.setMany(helper.isManyValued(context.getTypeMapping(), helper.getGenericType(method)));
            property.setRequired(annotation.required());
            type.add(property, site);
        } catch (IntrospectionException e) {
            throw new ProcessingException(e);
        }
    }

    public void visitField(Field field, PojoComponentType type, IntrospectionContext context) throws ProcessingException {

        try {
            Property annotation = field.getAnnotation(Property.class);
            if (annotation == null) {
                return;
            }

            String name = helper.getSiteName(field, annotation.name());

            Map<String, org.fabric3.scdl.Property> properties = type.getProperties();
            if (properties.containsKey(name)) {
                throw new DuplicatePropertyException(name);
            }

            FieldInjectionSite site = new FieldInjectionSite(field);
            org.fabric3.scdl.Property property = new org.fabric3.scdl.Property(name, null);
            property.setMany(helper.isManyValued(context.getTypeMapping(), field.getGenericType()));
            property.setRequired(annotation.required());
            type.add(property, site);
        } catch (IntrospectionException e) {
            throw new ProcessingException(e);
        }
    }
}
