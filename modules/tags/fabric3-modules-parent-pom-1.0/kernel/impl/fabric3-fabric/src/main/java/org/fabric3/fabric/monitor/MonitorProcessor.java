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
import org.fabric3.spi.introspection.contract.ContractProcessor;
import org.fabric3.spi.introspection.java.AbstractAnnotationProcessor;

/**
 * @version $Rev$ $Date$
 */
public class MonitorProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<Monitor, I> {

    private final IntrospectionHelper helper;
    private final ContractProcessor contractProcessor;

    public MonitorProcessor(@Reference IntrospectionHelper helper, @Reference ContractProcessor contractProcessor) {
        super(Monitor.class);
        this.helper = helper;
        this.contractProcessor = contractProcessor;
    }

    public void visitField(Monitor annotation, Field field, I implementation, IntrospectionContext context) {
        String name = helper.getSiteName(field, null);
        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        MonitorResource resource = createDefinition(name, type, context.getTypeMapping(), context);
        implementation.getComponentType().add(resource, site);
    }

    public void visitMethod(Monitor annotation, Method method, I implementation, IntrospectionContext context) {
        String name = helper.getSiteName(method, null);
        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        MonitorResource resource = createDefinition(name, type, context.getTypeMapping(), context);
        implementation.getComponentType().add(resource, site);
    }

    public void visitConstructorParameter(Monitor annotation, Constructor<?> constructor, int index, I implementation, IntrospectionContext context) {
        String name = helper.getSiteName(constructor, index, null);
        Type type = helper.getGenericType(constructor, index);
        ConstructorInjectionSite site = new ConstructorInjectionSite(constructor, index);
        MonitorResource resource = createDefinition(name, type, context.getTypeMapping(), context);
        implementation.getComponentType().add(resource, site);
    }


    MonitorResource createDefinition(String name, Type type, TypeMapping typeMapping, IntrospectionContext context) {
        ServiceContract<?> contract = contractProcessor.introspect(typeMapping, type, context);
        return new MonitorResource(name, false, contract);
    }
}
