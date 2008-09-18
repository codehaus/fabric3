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
package org.fabric3.junit.introspection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.introspection.contract.ContractProcessor;
import org.fabric3.introspection.java.HeuristicProcessor;
import org.fabric3.junit.scdl.JUnitImplementation;
import org.fabric3.junit.scdl.JUnitServiceContract;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;

/**
 * @version $Rev$ $Date$
 */
public class JUnitServiceHeuristic implements HeuristicProcessor<JUnitImplementation> {
    private static final String TEST_SERVICE_NAME = "testService";

    private final IntrospectionHelper helper;
    private final ContractProcessor contractProcessor;

    public JUnitServiceHeuristic(@Reference IntrospectionHelper helper, @Reference ContractProcessor contractProcessor) {
        this.helper = helper;
        this.contractProcessor = contractProcessor;
    }

    public void applyHeuristics(JUnitImplementation implementation, Class<?> implClass, IntrospectionContext context) {

        JUnitServiceContract testContract = generateTestContract(implClass);
        ServiceDefinition testService = new ServiceDefinition(TEST_SERVICE_NAME, testContract);
        PojoComponentType componentType = implementation.getComponentType();
        TypeMapping typeMapping = context.getTypeMapping();
        componentType.add(testService);
        // if the class implements a single interface, use it, otherwise the contract is the class itself
        Set<Class<?>> interfaces = helper.getImplementedInterfaces(implClass);
        if (interfaces.size() > 1) {
            for (Class interfaze : interfaces) {
                if (interfaze.getCanonicalName().endsWith("Test")) {
                    continue;
                }
                ServiceDefinition serviceDefinition = createServiceDefinition(interfaze, typeMapping, context);
                componentType.add(serviceDefinition);
            }
        }
    }

    ServiceDefinition createServiceDefinition(Class<?> serviceInterface, TypeMapping typeMapping, IntrospectionContext context) {
        ServiceContract<Type> contract = contractProcessor.introspect(typeMapping, serviceInterface, context);
        return new ServiceDefinition(contract.getInterfaceName(), contract);
    }

    private static final DataType<List<DataType<Type>>> INPUT_TYPE;
    private static final DataType<Type> OUTPUT_TYPE;
    private static final List<DataType<Type>> FAULT_TYPE;

    static {
        List<DataType<Type>> paramDataTypes = Collections.emptyList();
        INPUT_TYPE = new DataType<List<DataType<Type>>>(Object[].class, paramDataTypes);
        OUTPUT_TYPE = new DataType<Type>(void.class, void.class);
        FAULT_TYPE = Collections.emptyList();
    }

    JUnitServiceContract generateTestContract(Class<?> implClass) {
        List<Operation<Type>> operations = new ArrayList<Operation<Type>>();
        for (Method method : implClass.getMethods()) {
            // see if this is a test method
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (method.getReturnType() != void.class) {
                continue;
            }
            if (method.getParameterTypes().length != 0) {
                continue;
            }
            String name = method.getName();
            if (name.length() < 5 || !name.startsWith("test")) {
                continue;
            }
            Operation<Type> operation = new Operation<Type>(name, INPUT_TYPE, OUTPUT_TYPE, FAULT_TYPE);
            operations.add(operation);
        }
        return new JUnitServiceContract(operations);
    }
}
