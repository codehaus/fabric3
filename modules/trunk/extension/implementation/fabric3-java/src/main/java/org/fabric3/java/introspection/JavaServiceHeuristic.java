/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.java.introspection;

import java.lang.reflect.Type;
import java.util.Set;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.contract.ContractProcessor;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.java.control.JavaImplementation;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.model.type.component.ServiceDefinition;

/**
 * @version $Rev$ $Date$
 */
public class JavaServiceHeuristic implements HeuristicProcessor<JavaImplementation> {

    private final IntrospectionHelper helper;
    private final ContractProcessor contractProcessor;

    public JavaServiceHeuristic(@Reference IntrospectionHelper helper,
                                @Reference ContractProcessor contractProcessor) {
        this.helper = helper;
        this.contractProcessor = contractProcessor;
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

    ServiceDefinition createServiceDefinition(Class<?> serviceInterface, TypeMapping typeMapping, IntrospectionContext context) {
        ServiceContract<Type> contract = contractProcessor.introspect(typeMapping, serviceInterface, context);
        return new ServiceDefinition(contract.getInterfaceName(), contract);
    }
}
