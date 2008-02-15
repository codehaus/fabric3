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

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.AbstractAnnotationProcessor;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.scdl.ConstructorInjectionSite;

/**
 * @version $Rev$ $Date$
 */
public class PropertyProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<Property, I> {
    private final IntrospectionHelper helper;

    public PropertyProcessor(@Reference IntrospectionHelper helper) {
        super(Property.class);
        this.helper = helper;
    }

    public void visitField(Property annotation, Field field, I implementation, IntrospectionContext context) throws IntrospectionException {
        String name = helper.getSiteName(field, annotation.name());
        createDefinition(implementation.getComponentType(), name, annotation.required(), field.getType(), new FieldInjectionSite(field));
    }

    public void visitMethod(Property annotation, Method method, I implementation, IntrospectionContext context) throws IntrospectionException {
        String name = helper.getSiteName(method, annotation.name());
        createDefinition(implementation.getComponentType(), name, annotation.required(), helper.getType(method), new MethodInjectionSite(method, 0));
    }

    public void visitConstructorParameter(Property annotation, Constructor<?> constructor, int index, I implementation, IntrospectionContext context)
            throws IntrospectionException {
        String name = helper.getSiteName(constructor, index, annotation.name());
        Class<?> type = helper.getType(constructor, index);
        createDefinition(implementation.getComponentType(), name, annotation.required(), type, new ConstructorInjectionSite(constructor, index));
    }

    <T> void createDefinition(InjectingComponentType componentType, String name, boolean required, Class<T> type, InjectionSite injectionSite) {
        org.fabric3.scdl.Property<T> property = new org.fabric3.scdl.Property<T>();
        property.setName(name);
        property.setJavaType(type);
        property.setRequired(required);
        property.setMany(helper.isManyValued(type));
        componentType.add(property, injectionSite);
    }
}
