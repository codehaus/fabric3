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
import java.lang.reflect.Type;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.ContractProcessor;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.InvalidServiceContractException;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.pojo.processor.ImplementationProcessorExtension;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.scdl.Multiplicity;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;

/**
 * Processes an {@link @Reference} annotation
 *
 * @version $Rev$ $Date$
 */
public class ReferenceProcessor extends ImplementationProcessorExtension {

    private final IntrospectionHelper helper;
    private final ContractProcessor interfaceIntrospector;

    public ReferenceProcessor(@Reference IntrospectionHelper helper,
                              @Reference ContractProcessor interfaceIntrospector) {
        this.interfaceIntrospector = interfaceIntrospector;
        this.helper = helper;
    }

    public void visitMethod(Method method, PojoComponentType type, IntrospectionContext context) throws ProcessingException {
        try {
            Reference annotation = method.getAnnotation(Reference.class);
            if (annotation == null) {
                return; // Not a reference annotation.
            }
            if (method.getParameterTypes().length != 1) {
                throw new IllegalReferenceException("Setter must have one parameter", method.toString());
            }
            String name = helper.getSiteName(method, annotation.name());
            if (type.getReferences().get(name) != null) {
                throw new DuplicateReferenceException(name);
            }

            boolean required = annotation.required();

            ServiceContract contract;
            try {
                Type baseType = helper.getBaseType(method.getGenericParameterTypes()[0], context.getTypeMapping());
                contract = interfaceIntrospector.introspect(context.getTypeMapping(), baseType);
            } catch (InvalidServiceContractException e) {
                throw new ProcessingException(e);
            }
            InjectionSite injectionSite = new MethodInjectionSite(method, 0);
            Multiplicity multiplicity = multiplicity(required, helper.getGenericType(method), context.getTypeMapping());
            ReferenceDefinition reference = new ReferenceDefinition(name, contract, multiplicity);
            type.add(reference, injectionSite);
        } catch (IntrospectionException e) {
            throw new ProcessingException(e);
        }
    }

    public void visitField(Field field, PojoComponentType type, IntrospectionContext context) throws ProcessingException {
        Reference annotation = field.getAnnotation(Reference.class);
        if (annotation == null) {
            return;
        }
        String name = field.getName();
        if (annotation.name() != null) {
            name = annotation.name();
        }
        boolean required = annotation.required();
        if (name.length() == 0) {
            name = field.getName();
        }
        if (type.getReferences().get(name) != null) {
            throw new DuplicateReferenceException(name);
        }

        ServiceContract contract;
        try {
            Type baseType = helper.getBaseType(field.getGenericType(), context.getTypeMapping());
            contract = interfaceIntrospector.introspect(context.getTypeMapping(), baseType);
        } catch (InvalidServiceContractException e) {
            throw new ProcessingException(e);
        }

        InjectionSite injectionSite = new FieldInjectionSite(field);
        Multiplicity multiplicity = multiplicity(required, field.getGenericType(), context.getTypeMapping());
        ReferenceDefinition reference = new ReferenceDefinition(name, contract, multiplicity);
        type.add(reference, injectionSite);
    }

    Multiplicity multiplicity(boolean required, Type type, TypeMapping typeMapping) {
        if (helper.isManyValued(typeMapping, type)) {
            return required ? Multiplicity.ONE_N : Multiplicity.ZERO_N;
        } else {
            return required ? Multiplicity.ONE_ONE : Multiplicity.ZERO_ONE;
        }
    }
}
