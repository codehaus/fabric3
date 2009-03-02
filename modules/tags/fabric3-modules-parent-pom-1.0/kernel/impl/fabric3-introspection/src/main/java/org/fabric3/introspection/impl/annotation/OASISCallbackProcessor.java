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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import org.oasisopen.sca.annotation.Callback;
import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.component.CallbackDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.java.FieldInjectionSite;
import org.fabric3.model.type.java.InjectingComponentType;
import org.fabric3.model.type.java.MethodInjectionSite;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.contract.ContractProcessor;
import org.fabric3.spi.introspection.java.AbstractAnnotationProcessor;

/**
 * @version $Rev$ $Date$
 */
public class OASISCallbackProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<Callback, I> {
    private final IntrospectionHelper helper;
    private final ContractProcessor contractProcessor;

    public OASISCallbackProcessor(@Reference ContractProcessor contractProcessor, @Reference IntrospectionHelper helper) {
        super(Callback.class);
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }


    public void visitField(Callback annotation, Field field, I implementation, IntrospectionContext context) {
        validate(field, context);

        String name = helper.getSiteName(field, null);
        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        CallbackDefinition definition = createDefinition(name, type, context.getTypeMapping(), context);
        implementation.getComponentType().add(definition, site);
    }

    public void visitMethod(Callback annotation, Method method, I implementation, IntrospectionContext context) {
        validate(method, context);

        String name = helper.getSiteName(method, null);
        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        CallbackDefinition definition = createDefinition(name, type, context.getTypeMapping(), context);
        implementation.getComponentType().add(definition, site);
    }

    private void validate(Field field, IntrospectionContext context) {
        if (!Modifier.isProtected(field.getModifiers()) && !Modifier.isPublic(field.getModifiers())) {
            Class<?> clazz = field.getDeclaringClass();
            InvalidAccessor warning =
                    new InvalidAccessor("Illegal callback. The field " + field.getName() + " on " + clazz.getName()
                            + " is annotated with @Callback and must be public or protected.", clazz);
            context.addError(warning);
        }
    }

    private void validate(Method method, IntrospectionContext context) {
        if (!Modifier.isProtected(method.getModifiers()) && !Modifier.isPublic(method.getModifiers())) {
            Class<?> clazz = method.getDeclaringClass();
            InvalidAccessor warning = new InvalidAccessor("Illegal callback. The method " + method
                    + " is annotated with @Callback and must be public or protected.", clazz);
            context.addError(warning);
        }
    }

    private CallbackDefinition createDefinition(String name, Type type, TypeMapping typeMapping, IntrospectionContext context) {
        Type baseType = helper.getBaseType(type, typeMapping);
        ServiceContract<Type> contract = contractProcessor.introspect(typeMapping, baseType, context);
        return new CallbackDefinition(name, contract);
    }
}
