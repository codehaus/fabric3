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

import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.contract.ContractProcessor;
import org.fabric3.introspection.java.AbstractAnnotationProcessor;
import org.fabric3.introspection.contract.InvalidServiceContractException;
import org.fabric3.introspection.helper.IntrospectionHelper;
import org.fabric3.introspection.helper.TypeMapping;
import org.fabric3.scdl.CallbackDefinition;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.scdl.ServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class CallbackProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<Callback, I> {
    private final IntrospectionHelper helper;
    private final ContractProcessor contractProcessor;

    public CallbackProcessor(@Reference ContractProcessor contractProcessor,
                             @Reference IntrospectionHelper helper) {
        super(Callback.class);
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }


    public void visitField(Callback annotation, Field field, I implementation, IntrospectionContext context) throws IntrospectionException {

        String name = helper.getSiteName(field, null);
        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        CallbackDefinition definition = createDefinition(name, type, context.getTypeMapping());
        implementation.getComponentType().add(definition, site);
    }

    public void visitMethod(Callback annotation, Method method, I implementation, IntrospectionContext context) throws IntrospectionException {

        String name = helper.getSiteName(method, null);
        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        CallbackDefinition definition = createDefinition(name, type, context.getTypeMapping());
        implementation.getComponentType().add(definition, site);
    }

    CallbackDefinition createDefinition(String name, Type type, TypeMapping typeMapping) throws InvalidServiceContractException {
        ServiceContract<Type> contract = contractProcessor.introspect(typeMapping, helper.getBaseType(type, typeMapping));
        return new CallbackDefinition(name, contract);
    }
}
