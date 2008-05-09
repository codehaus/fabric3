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
package org.fabric3.jpa.introspection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.introspection.contract.ContractProcessor;
import org.fabric3.introspection.contract.InvalidServiceContractException;
import org.fabric3.introspection.java.AbstractAnnotationProcessor;
import org.fabric3.jpa.scdl.PersistenceContextResource;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.scdl.Scope;
import org.fabric3.scdl.ServiceContract;

/**
 * Processes @PersistenceContext annotations.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class PersistenceContextProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<PersistenceContext, I> {
    private final ServiceContract<Type> factoryServiceContract;

    public PersistenceContextProcessor(@Reference ContractProcessor contractProcessor) throws InvalidServiceContractException {
        super(PersistenceContext.class);
        factoryServiceContract = contractProcessor.introspect(new TypeMapping(), EntityManager.class);
    }

    public void visitField(PersistenceContext annotation, Field field, I implementation, IntrospectionContext context) throws IntrospectionException {
        FieldInjectionSite site = new FieldInjectionSite(field);
        InjectingComponentType componentType = implementation.getComponentType();
        PersistenceContextResource definition = createDefinition(annotation, componentType);
        componentType.add(definition, site);
    }

    public void visitMethod(PersistenceContext annotation, Method method, I implementation, IntrospectionContext context)
            throws IntrospectionException {
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        InjectingComponentType componentType = implementation.getComponentType();
        PersistenceContextResource definition = createDefinition(annotation, componentType);
        componentType.add(definition, site);
    }

    private PersistenceContextResource createDefinition(PersistenceContext annotation, InjectingComponentType componentType) {
        String name = annotation.name();
        String unitName = annotation.unitName();
        PersistenceContextType type = annotation.type();
        boolean multiThreaded = Scope.COMPOSITE.getScope().equals(componentType.getScope());
        return new PersistenceContextResource(name, unitName, type, factoryServiceContract, multiThreaded);
    }
}