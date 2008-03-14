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
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.java.AbstractAnnotationProcessor;
import org.fabric3.introspection.java.ContractProcessor;
import org.fabric3.introspection.java.InvalidServiceContractException;
import org.fabric3.introspection.java.TypeMapping;
import org.fabric3.jpa.PersistenceUnitResource;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.scdl.ServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class PersistenceUnitProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<PersistenceUnit, I> {

    private final ServiceContract<Type> factoryServiceContract;

    public PersistenceUnitProcessor(@Reference ContractProcessor contractProcessor) {
        super(PersistenceUnit.class);
        try {
            factoryServiceContract = contractProcessor.introspect(new TypeMapping(), EntityManagerFactory.class);
        } catch (InvalidServiceContractException e) {
            throw new AssertionError();
        }
    }

    public void visitField(PersistenceUnit annotation, Field field, I implementation, IntrospectionContext context) throws IntrospectionException {
        FieldInjectionSite site = new FieldInjectionSite(field);
        PersistenceUnitResource definition = createDefinition(annotation);
        implementation.getComponentType().add(definition, site);
    }

    public void visitMethod(PersistenceUnit annotation, Method method, I implementation, IntrospectionContext context) throws IntrospectionException {
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        PersistenceUnitResource definition = createDefinition(annotation);
        implementation.getComponentType().add(definition, site);
    }

    PersistenceUnitResource createDefinition(PersistenceUnit annotation) {
        String name = annotation.name();
        String unitName = annotation.unitName();
        return new PersistenceUnitResource(name, unitName, factoryServiceContract);
    }
}
