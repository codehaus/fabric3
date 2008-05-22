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
package org.fabric3.mock;

import java.lang.reflect.Type;
import java.util.List;

import org.easymock.IMocksControl;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.introspection.contract.ContractProcessor;
import org.fabric3.scdl.DefaultValidationContext;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.ValidationContext;
import org.fabric3.scdl.validation.MissingResource;

/**
 * @version $Revision$ $Date$
 */
public class MockComponentTypeLoaderImpl implements MockComponentTypeLoader {
    private final ContractProcessor contractProcessor;
    private final IntrospectionHelper helper;
    private final ServiceDefinition controlService;

    public MockComponentTypeLoaderImpl(@Reference IntrospectionHelper helper, @Reference ContractProcessor contractProcessor) {
        this.helper = helper;
        this.contractProcessor = contractProcessor;
        ValidationContext context = new DefaultValidationContext();
        ServiceContract<Type> controlServiceContract = introspect(IMocksControl.class, context);
        assert !context.hasErrors(); // should not happen
        controlService = new ServiceDefinition("mockControl", controlServiceContract);
    }

    /**
     * Loads the mock component type.
     *
     * @param mockedInterfaces     Interfaces that need to be mocked.
     * @param introspectionContext Loader context.
     * @return Mock component type.
     */
    public MockComponentType load(List<String> mockedInterfaces, IntrospectionContext introspectionContext) {

        MockComponentType componentType = new MockComponentType();

        ClassLoader classLoader = introspectionContext.getTargetClassLoader();
        for (String mockedInterface : mockedInterfaces) {
            Class<?> interfaceClass = null;
            try {
                interfaceClass = classLoader.loadClass(mockedInterface);
            } catch (ClassNotFoundException e) {
                MissingResource failure = new MissingResource("Mock interface not found: " + mockedInterface, mockedInterface);
                introspectionContext.addError(failure);
                continue;
            }

            ServiceContract<Type> serviceContract = introspect(interfaceClass, introspectionContext);
            String name = interfaceClass.getName();
            int index = name.lastIndexOf('.');
            if (index != -1) {
                name = name.substring(index + 1);
            }
            componentType.add(new ServiceDefinition(name, serviceContract));
        }
        componentType.add(controlService);
        componentType.setScope("STATELESS");

        return componentType;


    }

    private ServiceContract<Type> introspect(Class<?> interfaceClass, ValidationContext context) {
        TypeMapping typeMapping = helper.mapTypeParameters(interfaceClass);
        return contractProcessor.introspect(typeMapping, interfaceClass, context);
    }

}
