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
package org.fabric3.junit.introspection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.osoa.sca.annotations.Reference;

import org.fabric3.junit.scdl.JUnitImplementation;
import org.fabric3.junit.scdl.JUnitServiceContract;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.service.DataType;
import org.fabric3.model.type.service.Operation;
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
public class JUnitServiceHeuristic implements HeuristicProcessor<JUnitImplementation> {
    private static final String TEST_SERVICE_NAME = "testService";
    private static final DataType<List<DataType<Type>>> INPUT_TYPE;
    private static final DataType<Type> OUTPUT_TYPE;
    private static final List<DataType<Type>> FAULT_TYPE;

    static {
        List<DataType<Type>> paramDataTypes = Collections.emptyList();
        INPUT_TYPE = new DataType<List<DataType<Type>>>(Object[].class, paramDataTypes);
        OUTPUT_TYPE = new DataType<Type>(void.class, void.class);
        FAULT_TYPE = Collections.emptyList();
    }

    private final IntrospectionHelper helper;
    private final ContractProcessor contractProcessor;
    private PolicyAnnotationProcessor policyProcessor;

    public JUnitServiceHeuristic(@Reference IntrospectionHelper helper, @Reference ContractProcessor contractProcessor) {
        this.helper = helper;
        this.contractProcessor = contractProcessor;
    }

    @Reference
    public void setPolicyProcessor(PolicyAnnotationProcessor processor) {
        this.policyProcessor = processor;
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

    @SuppressWarnings({"unchecked"})
    private ServiceDefinition createServiceDefinition(Class<?> serviceInterface, TypeMapping typeMapping, IntrospectionContext context) {
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

    private JUnitServiceContract generateTestContract(Class<?> implClass) {
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
