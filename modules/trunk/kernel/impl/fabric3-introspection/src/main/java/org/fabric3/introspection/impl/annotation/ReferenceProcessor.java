/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.java.ConstructorInjectionSite;
import org.fabric3.model.type.java.FieldInjectionSite;
import org.fabric3.model.type.java.InjectingComponentType;
import org.fabric3.model.type.java.MethodInjectionSite;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.contract.ContractProcessor;
import org.fabric3.spi.introspection.java.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.PolicyAnnotationProcessor;

/**
 * @version $Rev$ $Date$
 */
public class ReferenceProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<Reference, I> {
    private final ContractProcessor contractProcessor;
    private final IntrospectionHelper helper;
    private PolicyAnnotationProcessor policyProcessor;

    public ReferenceProcessor(@Reference ContractProcessor contractProcessor, @Reference IntrospectionHelper helper) {
        super(Reference.class);
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }

    @Reference
    public void setPolicyProcessor(PolicyAnnotationProcessor processor) {
        this.policyProcessor = processor;
    }

    public void visitField(Reference annotation, Field field, I implementation, IntrospectionContext context) {
        validate(annotation, field, context);
        String name = helper.getSiteName(field, annotation.name());
        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        Annotation[] annotations = field.getAnnotations();
        ReferenceDefinition definition = createDefinition(name, annotation.required(), type, annotations, context);
        implementation.getComponentType().add(definition, site);
    }

    public void visitMethod(Reference annotation, Method method, I implementation, IntrospectionContext context) {
        validate(annotation, method, context);

        String name = helper.getSiteName(method, annotation.name());
        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        Annotation[] annotations = method.getAnnotations();
        ReferenceDefinition definition = createDefinition(name, annotation.required(), type, annotations, context);
        implementation.getComponentType().add(definition, site);
    }

    private void validate(Reference annotation, Field field, IntrospectionContext context) {
        if (!Modifier.isProtected(field.getModifiers()) && !Modifier.isPublic(field.getModifiers())) {
            Class<?> clazz = field.getDeclaringClass();
            if (annotation.required()) {
                InvalidAccessor error =
                        new InvalidAccessor("Invalid required reference. The field " + field.getName() + " on " + clazz.getName()
                                + " is annotated with @Reference and must be public or protected.", clazz);
                context.addError(error);
            } else {
                InvalidAccessor warning =
                        new InvalidAccessor("Ignoring the field " + field.getName() + " annotated with @Reference on " + clazz.getName()
                                + ". References must be public or protected.", clazz);
                context.addWarning(warning);
            }
        }
    }

    private void validate(Reference annotation, Method method, IntrospectionContext context) {
        if (!Modifier.isProtected(method.getModifiers()) && !Modifier.isPublic(method.getModifiers())) {
            Class<?> clazz = method.getDeclaringClass();
            if (annotation.required()) {
                InvalidAccessor error =
                        new InvalidAccessor("Invalid required reference. The method " + method + " on " + clazz.getName()
                                + " is annotated with @Reference and must be public or protected.", clazz);
                context.addError(error);
            } else {
                InvalidAccessor warning =
                        new InvalidAccessor("Ignoring " + method + " annotated with @Reference. References " + "must be public or protected.", clazz);
                context.addWarning(warning);
            }
        }
    }

    public void visitConstructorParameter(Reference annotation,
                                          Constructor<?> constructor,
                                          int index,
                                          I implementation,
                                          IntrospectionContext context) {

        String name = helper.getSiteName(constructor, index, annotation.name());
        Type type = helper.getGenericType(constructor, index);
        ConstructorInjectionSite site = new ConstructorInjectionSite(constructor, index);
        Annotation[] annotations = constructor.getParameterAnnotations()[index];
        ReferenceDefinition definition = createDefinition(name, annotation.required(), type, annotations, context);
        implementation.getComponentType().add(definition, site);
    }

    @SuppressWarnings({"unchecked"})
    ReferenceDefinition createDefinition(String name, boolean required, Type type, Annotation[] annotations, IntrospectionContext context) {
        TypeMapping typeMapping = context.getTypeMapping();
        Type baseType = helper.getBaseType(type, typeMapping);
        ServiceContract<Type> contract = contractProcessor.introspect(typeMapping, baseType, context);
        Multiplicity multiplicity = multiplicity(required, type, typeMapping);
        ReferenceDefinition definition = new ReferenceDefinition(name, contract, multiplicity);
        if (policyProcessor != null) {
            for (Annotation annotation : annotations) {
                policyProcessor.process(annotation, definition, context);
            }
        }
        return definition;
    }

    /**
     * Returns the multiplicity of a type based on whether it describes a single value or a collection.
     *
     * @param required    whether a value must be supplied (implies 1.. multiplicity)
     * @param type        the multiplicity of a type
     * @param typeMapping the current introspection type mapping
     * @return the multiplicity of the type
     */
    Multiplicity multiplicity(boolean required, Type type, TypeMapping typeMapping) {
        if (helper.isManyValued(typeMapping, type)) {
            return required ? Multiplicity.ONE_N : Multiplicity.ZERO_N;
        } else {
            return required ? Multiplicity.ONE_ONE : Multiplicity.ZERO_ONE;
        }
    }
}
