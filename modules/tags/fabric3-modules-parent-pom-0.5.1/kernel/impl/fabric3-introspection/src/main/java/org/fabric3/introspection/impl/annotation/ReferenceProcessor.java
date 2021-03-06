/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
import java.lang.reflect.Type;
import java.lang.reflect.Modifier;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.introspection.contract.ContractProcessor;
import org.fabric3.introspection.java.AbstractAnnotationProcessor;
import org.fabric3.scdl.ConstructorInjectionSite;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.scdl.Multiplicity;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class ReferenceProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<Reference, I> {
    private final ContractProcessor contractProcessor;
    private final IntrospectionHelper helper;

    public ReferenceProcessor(@Reference ContractProcessor contractProcessor, @Reference IntrospectionHelper helper) {
        super(Reference.class);
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }

    public void visitField(Reference annotation, Field field, I implementation, IntrospectionContext context) {
        validate(annotation, field, context);
        String name = helper.getSiteName(field, annotation.name());
        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        ReferenceDefinition definition = createDefinition(name, annotation.required(), type, context.getTypeMapping(), context);
        implementation.getComponentType().add(definition, site);
    }

    public void visitMethod(Reference annotation, Method method, I implementation, IntrospectionContext context) {
        validate(annotation, method, context);

        String name = helper.getSiteName(method, annotation.name());
        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        ReferenceDefinition definition = createDefinition(name, annotation.required(), type, context.getTypeMapping(), context);
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
        ReferenceDefinition definition = createDefinition(name, annotation.required(), type, context.getTypeMapping(), context);
        implementation.getComponentType().add(definition, site);
    }

    ReferenceDefinition createDefinition(String name, boolean required, Type type, TypeMapping typeMapping, IntrospectionContext context) {
        Type baseType = helper.getBaseType(type, typeMapping);
        ServiceContract<Type> contract = contractProcessor.introspect(typeMapping, baseType, context);
        Multiplicity multiplicity = multiplicity(required, type, typeMapping);
        return new ReferenceDefinition(name, contract, multiplicity);
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
