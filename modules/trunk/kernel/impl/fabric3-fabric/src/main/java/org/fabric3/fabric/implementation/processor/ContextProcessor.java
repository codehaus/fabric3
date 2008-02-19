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

import org.osoa.sca.ComponentContext;
import org.osoa.sca.RequestContext;
import org.osoa.sca.annotations.Context;

import org.fabric3.pojo.processor.ImplementationProcessorExtension;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.introspection.IntrospectionContext;

/**
 * Processes {@link @Context} annotations on a component implementation.
 *
 * @version $Rev$ $Date$
 */
public class ContextProcessor extends ImplementationProcessorExtension {
    public void visitMethod(
            Method method,
            PojoComponentType type,
            IntrospectionContext context)
            throws ProcessingException {
        if (method.getAnnotation(Context.class) == null) {
            return;
        }
        if (method.getParameterTypes().length != 1) {
            throw new IllegalContextException("Context setter must have one parameter", method.toString());
        }
        InjectionSite site = new MethodInjectionSite(method, 0);
        Class<?> paramType = method.getParameterTypes()[0];
        if (paramType.isAssignableFrom(ComponentContext.class)) {
            type.addInjectionSite(InjectableAttribute.COMPONENT_CONTEXT, site);
        } else if (paramType.isAssignableFrom(RequestContext.class)) {
            type.addInjectionSite(InjectableAttribute.REQUEST_CONTEXT, site);
        }
    }

    public void visitField(Field field,
                           PojoComponentType type,
                           IntrospectionContext context) throws ProcessingException {
        if (field.getAnnotation(Context.class) == null) {
            return;
        }
        InjectionSite site = new FieldInjectionSite(field);
        Class<?> paramType = field.getType();
        if (paramType.isAssignableFrom(ComponentContext.class)) {
            type.addInjectionSite(InjectableAttribute.COMPONENT_CONTEXT, site);
        } else if (paramType.isAssignableFrom(RequestContext.class)) {
            type.addInjectionSite(InjectableAttribute.REQUEST_CONTEXT, site);
        }
    }
}
