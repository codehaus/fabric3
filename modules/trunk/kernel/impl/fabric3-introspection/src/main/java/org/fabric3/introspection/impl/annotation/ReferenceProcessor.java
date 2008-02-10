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

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.AbstractAnnotationProcessor;
import org.fabric3.introspection.ContractProcessor;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;
import org.fabric3.scdl.MemberSite;
import org.fabric3.scdl.Multiplicity;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class ReferenceProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<Reference, I> {
    private final ContractProcessor contractProcessor;
    private final IntrospectionHelper helper;

    public ReferenceProcessor(@Reference ContractProcessor contractProcessor,
                              @Reference IntrospectionHelper helper) {
        super(Reference.class);
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }

    public void visitField(Reference annotation, Field field, I implementation, IntrospectionContext context)
            throws IntrospectionException {

        String name = helper.getSiteName(field, annotation.name());
        createDefinition(implementation.getComponentType(), name, annotation.required(), field.getGenericType(), new MemberSite(field));
    }

    public void visitMethod(Reference annotation, Method method, I implementation, IntrospectionContext context)
            throws IntrospectionException {

        String name = helper.getSiteName(method, annotation.name());
        createDefinition(implementation.getComponentType(), name, annotation.required(), helper.getGenericType(method), new MemberSite(method));
    }

    void createDefinition(InjectingComponentType componentType, String name, boolean required, Type type, MemberSite site) throws IntrospectionException {
        ServiceContract<Type> contract = contractProcessor.introspect(type);
        Multiplicity multiplicity = multiplicity(required, type);
        ReferenceDefinition definition = new ReferenceDefinition(name, contract, multiplicity);
        componentType.add(definition, site);
    }

    /**
     * Returns the multiplicity of a type based on whether it describes a single value or a collection.
     *
     * @param required whether a value must be supplied (implies 1.. multiplicity)
     * @param type     the multiplicity of a type
     * @return the multiplicity of the type
     */
    Multiplicity multiplicity(boolean required, Type type) {
        if (helper.isManyValued(type)) {
            return required ? Multiplicity.ONE_N : Multiplicity.ZERO_N;
        } else {
            return required ? Multiplicity.ONE_ONE : Multiplicity.ZERO_ONE;
        }
    }
}
