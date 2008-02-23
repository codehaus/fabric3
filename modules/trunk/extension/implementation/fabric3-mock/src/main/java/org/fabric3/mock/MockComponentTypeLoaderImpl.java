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

import org.fabric3.introspection.ContractProcessor;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.InvalidServiceContractException;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.scdl.Scope;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.loader.LoaderException;

/**
 * @version $Revision$ $Date$
 */
public class MockComponentTypeLoaderImpl implements MockComponentTypeLoader {
    private final ContractProcessor contractProcessor;
    private final IntrospectionHelper helper;
    private final ServiceDefinition controlService;

    public MockComponentTypeLoaderImpl(@Reference IntrospectionHelper helper,
                                       @Reference ContractProcessor contractProcessor) {
        this.helper = helper;
        this.contractProcessor = contractProcessor;
        try {
            ServiceContract<Type> controlServiceContract = introspect(IMocksControl.class);
            controlService = new ServiceDefinition("mockControl", controlServiceContract);
        } catch (InvalidServiceContractException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Loads the mock component type.
     *
     * @param mockedInterfaces     Interfaces that need to be mocked.
     * @param introspectionContext Loader context.
     * @return Mock component type.
     */
    public MockComponentType load(List<String> mockedInterfaces, IntrospectionContext introspectionContext) throws LoaderException {

        try {

            MockComponentType componentType = new MockComponentType();

            ClassLoader classLoader = introspectionContext.getTargetClassLoader();
            for (String mockedInterface : mockedInterfaces) {
                Class<?> interfaceClass = classLoader.loadClass(mockedInterface);

                ServiceContract<Type> serviceContract = introspect(interfaceClass);
                String name = interfaceClass.getName();
                int index = name.lastIndexOf('.');
                if (index != -1) {
                    name = name.substring(index + 1);
                }
                componentType.add(new ServiceDefinition(name, serviceContract));
            }
            componentType.add(controlService);
            componentType.setImplementationScope(Scope.STATELESS);

            return componentType;

        } catch (ClassNotFoundException ex) {
            throw new LoaderException(ex);
        } catch (InvalidServiceContractException e) {
            throw new LoaderException(e);
        }

    }

    private ServiceContract<Type> introspect(Class<?> interfaceClass) throws InvalidServiceContractException {
        TypeMapping typeMapping = helper.mapTypeParameters(interfaceClass);
        return contractProcessor.introspect(typeMapping, interfaceClass);
    }

}
