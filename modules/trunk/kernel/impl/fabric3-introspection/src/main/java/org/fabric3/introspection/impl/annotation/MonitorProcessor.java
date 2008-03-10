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

import org.fabric3.api.annotation.Monitor;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.java.AbstractAnnotationProcessor;
import org.fabric3.introspection.java.ContractProcessor;
import org.fabric3.introspection.java.IntrospectionHelper;
import org.fabric3.introspection.java.InvalidServiceContractException;
import org.fabric3.introspection.java.TypeMapping;
import org.fabric3.scdl.ConstructorInjectionSite;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.scdl.MonitorResource;
import org.fabric3.scdl.ServiceContract;

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

    public void visitField(Monitor annotation, Field field, I implementation, IntrospectionContext context) throws IntrospectionException {
        String name = helper.getSiteName(field, null);
        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        MonitorResource resource = createDefinition(name, type, context.getTypeMapping());
        implementation.getComponentType().add(resource, site);
    }

    public void visitMethod(Monitor annotation, Method method, I implementation, IntrospectionContext context) throws IntrospectionException {
        String name = helper.getSiteName(method, null);
        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        MonitorResource resource = createDefinition(name, type, context.getTypeMapping());
        implementation.getComponentType().add(resource, site);
    }

    public void visitConstructorParameter(Monitor annotation, Constructor<?> constructor, int index, I implementation, IntrospectionContext context)
            throws IntrospectionException {
        String name = helper.getSiteName(constructor, index, null);
        Type type = helper.getGenericType(constructor, index);
        ConstructorInjectionSite site = new ConstructorInjectionSite(constructor, index);
        MonitorResource resource = createDefinition(name, type, context.getTypeMapping());
        implementation.getComponentType().add(resource, site);
    }


    MonitorResource createDefinition(String name, Type type, TypeMapping typeMapping) throws InvalidServiceContractException {
        ServiceContract<?> contract = contractProcessor.introspect(typeMapping, type);
        return new MonitorResource(name, false, contract);
    }
}
