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
package org.fabric3.introspection.impl.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.osoa.sca.ComponentContext;
import org.osoa.sca.RequestContext;
import org.osoa.sca.annotations.Context;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.introspection.java.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.model.type.java.FieldInjectionSite;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.java.InjectingComponentType;
import org.fabric3.model.type.java.MethodInjectionSite;

/**
 * @version $Rev$ $Date$
 */
public class ContextProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<Context, I> {
    private final IntrospectionHelper helper;

    public ContextProcessor(@Reference IntrospectionHelper helper) {
        super(Context.class);
        this.helper = helper;
    }


    public void visitField(Context annotation, Field field, I implementation, IntrospectionContext context) {

        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        InjectableAttribute attribute = null;
        if (type instanceof Class) {
            attribute = getContext((Class) type);

        }
        if (attribute != null) {
            implementation.getComponentType().addInjectionSite(attribute, site);
        }
    }

    public void visitMethod(Context annotation, Method method, I implementation, IntrospectionContext context) {

        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        InjectableAttribute attribute = null;
        if (type instanceof Class) {
            attribute = getContext((Class) type);

        }
        if (attribute != null) {
            implementation.getComponentType().addInjectionSite(attribute, site);
        }
    }

    InjectableAttribute getContext(Class<?> type) {
        if (RequestContext.class.isAssignableFrom(type)) {
            return InjectableAttribute.REQUEST_CONTEXT;
        } else if (ComponentContext.class.isAssignableFrom(type)) {
            return InjectableAttribute.COMPONENT_CONTEXT;
        } else {
            return null;
        }
    }
}
