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
package org.fabric3.system.introspection;

import java.lang.reflect.Type;
import java.util.Set;

import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Management;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.introspection.contract.ContractProcessor;
import org.fabric3.introspection.contract.InvalidServiceContractException;
import org.fabric3.introspection.java.HeuristicProcessor;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.system.scdl.SystemImplementation;
import org.fabric3.jmx.scdl.JMXBinding;

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

    public void applyHeuristics(SystemImplementation implementation, Class<?> implClass, IntrospectionContext context) throws IntrospectionException {
        PojoComponentType componentType = implementation.getComponentType();
        TypeMapping typeMapping = context.getTypeMapping();

        // if the service contracts have not already been defined then introspect them
        if (componentType.getServices().isEmpty()) {
            // get the most specific interfaces implemented by the class
            Set<Class<?>> interfaces = helper.getImplementedInterfaces(implClass);

            // if the class does not implement any interfaces, then the class itself is the service contract
            // we don't have to worry about proxies because all wires to system components are optimized
            if (interfaces.isEmpty()) {
                componentType.add(createServiceDefinition(implClass, typeMapping));
            } else {
                // otherwise, expose all of the implemented interfaces
                for (Class<?> serviceInterface : interfaces) {
                    componentType.add(createServiceDefinition(serviceInterface, typeMapping));
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

    ServiceDefinition createServiceDefinition(Class<?> serviceInterface, TypeMapping typeMapping) throws InvalidServiceContractException {
        ServiceContract<Type> contract = contractProcessor.introspect(typeMapping, serviceInterface);
        ServiceDefinition service = new ServiceDefinition(contract.getInterfaceName(), contract);
        service.setManagement(serviceInterface.isAnnotationPresent(Management.class));
        return service;
    }
}
