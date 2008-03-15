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
package org.fabric3.resource.introspection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.java.AbstractAnnotationProcessor;
import org.fabric3.introspection.contract.ContractProcessor;
import org.fabric3.introspection.contract.InvalidServiceContractException;
import org.fabric3.introspection.helper.IntrospectionHelper;
import org.fabric3.introspection.helper.TypeMapping;
import org.fabric3.resource.model.SystemSourcedResource;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.scdl.ResourceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.api.annotation.Resource;

/**
 * @version $Rev$ $Date$
 */
public class Fabric3ResourceProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<Resource, I> {
    private final IntrospectionHelper helper;
    private final ContractProcessor contractProcessor;

    public Fabric3ResourceProcessor(@Reference IntrospectionHelper helper,
                                    @Reference ContractProcessor contractProcessor) {
        super(Resource.class);
        this.helper = helper;
        this.contractProcessor = contractProcessor;
    }

    public void visitField(Resource annotation, Field field, I implementation, IntrospectionContext context) throws IntrospectionException {
        String name = helper.getSiteName(field, annotation.name());
        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        ResourceDefinition definition = createResource(name, type, annotation.optional(), annotation.mappedName(), context.getTypeMapping());
        implementation.getComponentType().add(definition, site);
    }

    public void visitMethod(Resource annotation, Method method, I implementation, IntrospectionContext context) throws IntrospectionException {
        String name = helper.getSiteName(method, annotation.name());
        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        ResourceDefinition definition = createResource(name, type, annotation.optional(), annotation.mappedName(), context.getTypeMapping());
        implementation.getComponentType().add(definition, site);
    }

    SystemSourcedResource createResource(String name, Type type, boolean optional, String mappedName, TypeMapping typeMapping)
            throws InvalidServiceContractException {
        ServiceContract<Type> serviceContract = contractProcessor.introspect(typeMapping, type);
        return new SystemSourcedResource(name, optional, mappedName, serviceContract);
    }
}