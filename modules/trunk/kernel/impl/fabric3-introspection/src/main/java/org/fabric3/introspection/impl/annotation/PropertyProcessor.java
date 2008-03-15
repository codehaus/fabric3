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

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.java.AbstractAnnotationProcessor;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.scdl.ConstructorInjectionSite;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.scdl.Property;

/**
 * @version $Rev$ $Date$
 */
public class PropertyProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<org.osoa.sca.annotations.Property, I> {
    private final IntrospectionHelper helper;

    public PropertyProcessor(@Reference IntrospectionHelper helper) {
        super(org.osoa.sca.annotations.Property.class);
        this.helper = helper;
    }

    public void visitField(org.osoa.sca.annotations.Property annotation, Field field, I implementation, IntrospectionContext context) throws IntrospectionException {
        String name = helper.getSiteName(field, annotation.name());
        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        Property property = createDefinition(name, annotation.required(), type, context.getTypeMapping());
        implementation.getComponentType().add(property, site);
    }

    public void visitMethod(org.osoa.sca.annotations.Property annotation, Method method, I implementation, IntrospectionContext context) throws IntrospectionException {
        String name = helper.getSiteName(method, annotation.name());
        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        Property property = createDefinition(name, annotation.required(), type, context.getTypeMapping());
        implementation.getComponentType().add(property, site);
    }

    public void visitConstructorParameter(org.osoa.sca.annotations.Property annotation, Constructor<?> constructor, int index, I implementation, IntrospectionContext context)
            throws IntrospectionException {
        String name = helper.getSiteName(constructor, index, annotation.name());
        Type type = helper.getGenericType(constructor, index);
        ConstructorInjectionSite site = new ConstructorInjectionSite(constructor, index);
        Property property = createDefinition(name, annotation.required(), type, context.getTypeMapping());
        implementation.getComponentType().add(property, site);
    }

    Property createDefinition(String name, boolean required, Type type, TypeMapping typeMapping) {
        Property property = new Property();
        property.setName(name);
        property.setRequired(required);
        property.setMany(helper.isManyValued(typeMapping, type));
        return property;
    }
}
