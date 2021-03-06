/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.system.introspection;

import java.lang.reflect.Type;
import java.util.Set;

import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Management;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.contract.ContractProcessor;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.spi.model.type.JMXBinding;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.system.scdl.SystemImplementation;

/**
 * Heuristic that identifies the services provided by an implementation class.
 *
 * @version $Rev$ $Date$
 */
public class SystemServiceHeuristic implements HeuristicProcessor<SystemImplementation> {
    private final ContractProcessor contractProcessor;
    private final IntrospectionHelper helper;

    public SystemServiceHeuristic(@Reference ContractProcessor contractProcessor,
                                  @Reference IntrospectionHelper helper) {
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }

    public void applyHeuristics(SystemImplementation implementation, Class<?> implClass, IntrospectionContext context) {
        PojoComponentType componentType = implementation.getComponentType();
        TypeMapping typeMapping = context.getTypeMapping();

        // if the service contracts have not already been defined then introspect them
        if (componentType.getServices().isEmpty()) {
            // get the most specific interfaces implemented by the class
            Set<Class<?>> interfaces = helper.getImplementedInterfaces(implClass);

            // if the class does not implement any interfaces, then the class itself is the service contract
            // we don't have to worry about proxies because all wires to system components are optimized
            if (interfaces.isEmpty()) {
                ServiceDefinition serviceDefinition = createServiceDefinition(implClass, typeMapping, context);
                componentType.add(serviceDefinition);
            } else {
                // otherwise, expose all of the implemented interfaces
                for (Class<?> serviceInterface : interfaces) {
                    ServiceDefinition serviceDefinition = createServiceDefinition(serviceInterface, typeMapping, context);
                    componentType.add(serviceDefinition);
                }
            }
        }

        // Add the JMX Management binding to all services tagged as management
        for (ServiceDefinition service : componentType.getServices().values()) {
            if (service.isManagement()) {
                JMXBinding binding = new JMXBinding();
                service.addBinding(binding);
            }
        }
    }

    ServiceDefinition createServiceDefinition(Class<?> serviceInterface, TypeMapping typeMapping, IntrospectionContext context) {
        ServiceContract<Type> contract = contractProcessor.introspect(typeMapping, serviceInterface, context);
        ServiceDefinition service = new ServiceDefinition(contract.getInterfaceName(), contract);
        service.setManagement(serviceInterface.isAnnotationPresent(Management.class));
        return service;
    }
}
