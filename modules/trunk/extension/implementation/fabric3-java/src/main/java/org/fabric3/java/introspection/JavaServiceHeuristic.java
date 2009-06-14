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
   */
package org.fabric3.java.introspection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import org.osoa.sca.annotations.Reference;

import org.fabric3.java.control.JavaImplementation;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.contract.ContractProcessor;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.spi.introspection.java.PolicyAnnotationProcessor;

/**
 * @version $Rev$ $Date$
 */
public class JavaServiceHeuristic implements HeuristicProcessor<JavaImplementation> {
    private final IntrospectionHelper helper;
    private final ContractProcessor contractProcessor;
    private PolicyAnnotationProcessor policyProcessor;

    public JavaServiceHeuristic(@Reference IntrospectionHelper helper,
                                @Reference ContractProcessor contractProcessor) {
        this.helper = helper;
        this.contractProcessor = contractProcessor;
    }

    @Reference
    public void setPolicyProcessor(PolicyAnnotationProcessor processor) {
        this.policyProcessor = processor;
    }

    public void applyHeuristics(JavaImplementation implementation, Class<?> implClass, IntrospectionContext context) {
        PojoComponentType componentType = implementation.getComponentType();
        TypeMapping typeMapping = context.getTypeMapping();

        // if any services have been defined, then there's nothing to do
        if (!componentType.getServices().isEmpty()) {
            return;
        }

        // if the class implements a single interface, use it, otherwise the contract is the class itself
        Set<Class<?>> interfaces = helper.getImplementedInterfaces(implClass);
        if (interfaces.size() == 1) {
            Class<?> service = interfaces.iterator().next();
            ServiceDefinition serviceDefinition = createServiceDefinition(service, typeMapping, context);
            componentType.add(serviceDefinition);
        } else {
            ServiceDefinition serviceDefinition = createServiceDefinition(implClass, typeMapping, context);
            componentType.add(serviceDefinition);
        }
    }

    @SuppressWarnings({"unchecked"})
    ServiceDefinition createServiceDefinition(Class<?> serviceInterface, TypeMapping typeMapping, IntrospectionContext context) {
        ServiceContract<Type> contract = contractProcessor.introspect(typeMapping, serviceInterface, context);

        ServiceDefinition definition = new ServiceDefinition(contract.getInterfaceName(), contract);
        Annotation[] annotations = serviceInterface.getAnnotations();
        if (policyProcessor != null) {
            for (Annotation annotation : annotations) {
                policyProcessor.process(annotation, definition, context);
            }
        }
        return definition;
    }
}
