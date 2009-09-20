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
package org.fabric3.fabric.monitor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.java.ConstructorInjectionSite;
import org.fabric3.model.type.java.FieldInjectionSite;
import org.fabric3.model.type.java.InjectingComponentType;
import org.fabric3.model.type.java.MethodInjectionSite;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;

/**
 * @version $Rev$ $Date$
 */
public class MonitorProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<Monitor, I> {

    private final IntrospectionHelper helper;
    private final JavaContractProcessor contractProcessor;

    public MonitorProcessor(@Reference IntrospectionHelper helper, @Reference JavaContractProcessor contractProcessor) {
        super(Monitor.class);
        this.helper = helper;
        this.contractProcessor = contractProcessor;
    }

    public void visitField(Monitor annotation, Field field, Class<?> implClass, I implementation, IntrospectionContext context) {
        String name = helper.getSiteName(field, null);
        Type genericType = field.getGenericType();
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Class<?> type = helper.getBaseType(genericType, typeMapping);
        FieldInjectionSite site = new FieldInjectionSite(field);
        MonitorResource resource = createDefinition(name, type, context);
        implementation.getComponentType().add(resource, site);
    }

    public void visitMethod(Monitor annotation, Method method, Class<?> implClass, I implementation, IntrospectionContext context) {
        String name = helper.getSiteName(method, null);
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Type genericType = helper.getGenericType(method);
        Class<?> type = helper.getBaseType(genericType, typeMapping);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        MonitorResource resource = createDefinition(name, type, context);
        implementation.getComponentType().add(resource, site);
    }

    public void visitConstructorParameter(Monitor annotation,
                                          Constructor<?> constructor,
                                          int index,
                                          Class<?> implClass,
                                          I implementation,
                                          IntrospectionContext context) {
        String name = helper.getSiteName(constructor, index, null);
        Type genericType = helper.getGenericType(constructor, index);
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Class<?> type = helper.getBaseType(genericType, typeMapping);
        ConstructorInjectionSite site = new ConstructorInjectionSite(constructor, index);
        MonitorResource resource = createDefinition(name, type, context);
        implementation.getComponentType().add(resource, site);
    }


    MonitorResource createDefinition(String name, Class<?> type, IntrospectionContext context) {
        ServiceContract contract = contractProcessor.introspect(type, context);
        return new MonitorResource(name, false, contract);
    }
}
