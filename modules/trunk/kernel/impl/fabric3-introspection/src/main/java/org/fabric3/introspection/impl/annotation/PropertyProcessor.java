/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.introspection.impl.annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.java.ConstructorInjectionSite;
import org.fabric3.model.type.java.FieldInjectionSite;
import org.fabric3.model.type.java.InjectingComponentType;
import org.fabric3.model.type.java.MethodInjectionSite;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.AbstractAnnotationProcessor;

/**
 * @version $Rev$ $Date$
 */
public class PropertyProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<org.osoa.sca.annotations.Property, I> {
    private final IntrospectionHelper helper;

    public PropertyProcessor(@Reference IntrospectionHelper helper) {
        super(org.osoa.sca.annotations.Property.class);
        this.helper = helper;
    }

    public void visitField(org.osoa.sca.annotations.Property annotation, Field field, I implementation, IntrospectionContext context) {
        validate(annotation, field, context);
        String name = helper.getSiteName(field, annotation.name());
        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        Property property = createDefinition(name, annotation.required(), type, context.getTypeMapping());
        implementation.getComponentType().add(property, site);
    }

    public void visitMethod(org.osoa.sca.annotations.Property annotation, Method method, I implementation, IntrospectionContext context) {
        boolean result = validate(annotation, method, context);
        if (!result) {
            return;
        }
        String name = helper.getSiteName(method, annotation.name());
        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        Property property = createDefinition(name, annotation.required(), type, context.getTypeMapping());
        implementation.getComponentType().add(property, site);
    }

    private void validate(org.osoa.sca.annotations.Property annotation, Field field, IntrospectionContext context) {
        if (!Modifier.isProtected(field.getModifiers()) && !Modifier.isPublic(field.getModifiers())) {
            Class<?> clazz = field.getDeclaringClass();
            if (annotation.required()) {
                InvalidAccessor error =
                        new InvalidAccessor("Invalid required property. The field " + field.getName() + " on " + clazz.getName()
                                + " is annotated with @Property but properties must be public or protected.", clazz);
                context.addError(error);
            } else {
                InvalidAccessor warning =
                        new InvalidAccessor("Ignoring the field " + field.getName() + " annotated with @Property on " + clazz.getName()
                                + ". Properties must be public or protected.", clazz);
                context.addWarning(warning);
            }
        }
    }

    public void visitConstructorParameter(org.osoa.sca.annotations.Property annotation,
                                          Constructor<?> constructor,
                                          int index,
                                          I implementation,
                                          IntrospectionContext context) {
        String name = helper.getSiteName(constructor, index, annotation.name());
        Type type = helper.getGenericType(constructor, index);
        ConstructorInjectionSite site = new ConstructorInjectionSite(constructor, index);
        Property property = createDefinition(name, annotation.required(), type, context.getTypeMapping());
        implementation.getComponentType().add(property, site);
    }

    private boolean validate(org.osoa.sca.annotations.Property annotation, Method method, IntrospectionContext context) {
        if (method.getParameterTypes().length != 1) {
            InvalidMethod error = new InvalidMethod("Setter methods for properties must have a single parameter: " + method);
            context.addError(error);
            return false;
        }
        if (!Modifier.isProtected(method.getModifiers()) && !Modifier.isPublic(method.getModifiers())) {
            Class<?> clazz = method.getDeclaringClass();
            if (annotation.required()) {
                InvalidAccessor error =
                        new InvalidAccessor("Invalid required property. The method " + method
                                + " is annotated with @Property and must be public or protected.", clazz);
                context.addError(error);
                return false;
            } else {
                InvalidAccessor warning =
                        new InvalidAccessor("Ignoring " + method + " annotated with @Property. Property " + "must be public or protected.", clazz);
                context.addWarning(warning);
                return false;
            }
        }
        return true;
    }

    private Property createDefinition(String name, boolean required, Type type, TypeMapping typeMapping) {
        Property property = new Property();
        property.setName(name);
        property.setRequired(required);
        property.setMany(helper.isManyValued(typeMapping, type));
        return property;
    }
}
