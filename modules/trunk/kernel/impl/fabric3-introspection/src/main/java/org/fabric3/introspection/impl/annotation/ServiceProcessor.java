/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.impl.annotation;

import java.lang.annotation.Annotation;

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.java.InjectingComponentType;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.contract.ContractProcessor;
import org.fabric3.spi.introspection.java.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.PolicyAnnotationProcessor;
import org.fabric3.spi.introspection.policy.OperationPolicyIntrospector;

/**
 * Processes the @Service annotation on a component implementaiton class.
 *
 * @version $Rev$ $Date$
 */
public class ServiceProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<Service, I> {
    private final ContractProcessor contractProcessor;
    private OperationPolicyIntrospector policyIntrospector;
    private PolicyAnnotationProcessor policyProcessor;

    public ServiceProcessor(@Reference ContractProcessor contractProcessor, @Reference OperationPolicyIntrospector policyIntrospector) {
        super(Service.class);
        this.contractProcessor = contractProcessor;
        this.policyIntrospector = policyIntrospector;
    }

    @Reference
    public void setPolicyProcessor(PolicyAnnotationProcessor processor) {
        this.policyProcessor = processor;
    }

    public void visitType(Service annotation, Class<?> type, I implementation, IntrospectionContext context) {
        TypeMapping typeMapping = context.getTypeMapping();
        InjectingComponentType componentType = implementation.getComponentType();

        for (Class<?> service : annotation.interfaces()) {
            ServiceDefinition definition = createDefinition(service, typeMapping, type, context);
            componentType.add(definition);
        }

        Class<?> service = annotation.value();
        if (!Void.class.equals(service)) {
            ServiceDefinition definition = createDefinition(service, typeMapping, type, context);
            componentType.add(definition);
        }
    }

    @SuppressWarnings({"unchecked"})
    private ServiceDefinition createDefinition(Class<?> service, TypeMapping typeMapping, Class<?> implClass, IntrospectionContext context) {
        ServiceContract<?> serviceContract = contractProcessor.introspect(typeMapping, service, context);
        ServiceDefinition definition = new ServiceDefinition(serviceContract.getInterfaceName(), serviceContract);
        Annotation[] annotations = service.getAnnotations();
        if (policyProcessor != null) {
            for (Annotation annotation : annotations) {
                policyProcessor.process(annotation, definition, context);
            }

            policyIntrospector.introspectPolicyOnOperations(serviceContract, implClass, context);
        }
        return definition;
    }


}