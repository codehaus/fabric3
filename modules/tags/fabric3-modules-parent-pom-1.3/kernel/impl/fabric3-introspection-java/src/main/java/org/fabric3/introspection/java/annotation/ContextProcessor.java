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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.osoa.sca.ComponentContext;
import org.osoa.sca.RequestContext;
import org.osoa.sca.annotations.Context;
import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.Injectable;
import org.fabric3.spi.model.type.java.InjectingComponentType;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;

/**
 * @version $Rev$ $Date$
 */
public class ContextProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<Context, I> {
    private final IntrospectionHelper helper;

    public ContextProcessor(@Reference IntrospectionHelper helper) {
        super(Context.class);
        this.helper = helper;
    }


    public void visitField(Context annotation, Field field, Class<?> implClass, I implementation, IntrospectionContext context) {

        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        Injectable injectable = null;
        if (type instanceof Class) {
            injectable = getContext((Class) type);

        }
        if (injectable != null) {
            implementation.getComponentType().addInjectionSite(site, injectable);
        }
    }

    public void visitMethod(Context annotation, Method method, Class<?> implClass, I implementation, IntrospectionContext context) {

        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        Injectable attribute = null;
        if (type instanceof Class) {
            attribute = getContext((Class) type);

        }
        if (attribute != null) {
            implementation.getComponentType().addInjectionSite(site, attribute);
        }
    }

    Injectable getContext(Class<?> type) {
        if (RequestContext.class.isAssignableFrom(type)) {
            return Injectable.REQUEST_CONTEXT;
        } else if (ComponentContext.class.isAssignableFrom(type)) {
            return Injectable.COMPONENT_CONTEXT;
        } else {
            return null;
        }
    }
}
