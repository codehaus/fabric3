/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.java.annotation;

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
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;

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
