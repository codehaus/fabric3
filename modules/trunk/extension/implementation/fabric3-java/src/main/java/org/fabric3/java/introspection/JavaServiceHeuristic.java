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
package org.fabric3.java.introspection;

import java.util.Set;
import java.lang.reflect.Type;

import org.osoa.sca.annotations.Reference;

import org.fabric3.java.scdl.JavaImplementation;
import org.fabric3.introspection.java.HeuristicProcessor;
import org.fabric3.introspection.helper.IntrospectionHelper;
import org.fabric3.introspection.contract.ContractProcessor;
import org.fabric3.introspection.helper.TypeMapping;
import org.fabric3.introspection.contract.InvalidServiceContractException;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.ServiceContract;

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

    public void applyHeuristics(JavaImplementation implementation, Class<?> implClass, IntrospectionContext context) throws IntrospectionException {
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
            componentType.add(createServiceDefinition(service, typeMapping));
        } else {
            componentType.add(createServiceDefinition(implClass, typeMapping));
        }
    }

    ServiceDefinition createServiceDefinition(Class<?> serviceInterface, TypeMapping typeMapping) throws InvalidServiceContractException {
        ServiceContract<Type> contract = contractProcessor.introspect(typeMapping, serviceInterface);
        return new ServiceDefinition(contract.getInterfaceName(), contract);
    }
}
