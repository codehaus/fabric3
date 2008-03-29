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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.osoa.sca.ComponentContext;
import org.osoa.sca.RequestContext;
import org.osoa.sca.annotations.Context;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.java.AbstractAnnotationProcessor;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectingComponentType;
import org.fabric3.scdl.MethodInjectionSite;

/**
 * @version $Rev$ $Date$
 */
public class ContextProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<Context, I> {
    private final IntrospectionHelper helper;

    public ContextProcessor(@Reference IntrospectionHelper helper) {
        super(Context.class);
        this.helper = helper;
    }


    public void visitField(Context annotation, Field field, I implementation, IntrospectionContext context) throws IntrospectionException {

        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        InjectableAttribute attribute = getContext(type);
        if (attribute != null) {
            implementation.getComponentType().addInjectionSite(attribute, site);
        }
    }

    public void visitMethod(Context annotation, Method method, I implementation, IntrospectionContext context) throws IntrospectionException {

        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        InjectableAttribute attribute = getContext(type);
        if (attribute != null) {
            implementation.getComponentType().addInjectionSite(attribute, site);
        }
    }

    InjectableAttribute getContext(Type type) {
        if (RequestContext.class.equals(type)) {
            return InjectableAttribute.REQUEST_CONTEXT;
        } else if (ComponentContext.class.equals(type)) {
            return InjectableAttribute.COMPONENT_CONTEXT;
        } else {
            return null;
        }
    }
}