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
package org.fabric3.fabric.implementation.system;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

import org.fabric3.introspection.ContractProcessor;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;

/**
 * @version $Rev$ $Date$
 */
public class SystemServiceHeuristicTestCase extends TestCase {
    private static final Set<Class<?>> NOCLASSES = Collections.emptySet();
    private SystemServiceHeuristic heuristic;

    private ContractProcessor contractProcessor;
    private IntrospectionHelper helper;
    private IntrospectionContext context;
    private SystemImplementation impl;
    private PojoComponentType componentType;
    private ServiceContract<Type> serviceInterfaceContract;
    private ServiceContract<Type> noInterfaceContract;
    private IMocksControl control;

    public void testNoInterface() throws IntrospectionException {
        EasyMock.expect(helper.getImplementedInterfaces(NoInterface.class)).andReturn(NOCLASSES);
        EasyMock.expect(contractProcessor.introspect(NoInterface.class)).andReturn(noInterfaceContract);
        control.replay();

        heuristic.applyHeuristics(impl, NoInterface.class, context);
        Map<String,ServiceDefinition> services = componentType.getServices();
        assertEquals(1, services.size());
        assertEquals(noInterfaceContract, services.get("NoInterface").getServiceContract());
        control.verify();
    }

    public void testWithInterface() throws IntrospectionException {
        Set<Class<?>> interfaces = new HashSet<Class<?>>();
        interfaces.add(ServiceInterface.class);

        EasyMock.expect(helper.getImplementedInterfaces(OneInterface.class)).andReturn(interfaces);
        EasyMock.expect(contractProcessor.introspect(ServiceInterface.class)).andReturn(serviceInterfaceContract);
        control.replay();

        heuristic.applyHeuristics(impl, OneInterface.class, context);
        Map<String,ServiceDefinition> services = componentType.getServices();
        assertEquals(1, services.size());
        assertEquals(serviceInterfaceContract, services.get("ServiceInterface").getServiceContract());
        control.verify();
    }

    public void testServiceWithExistingServices() throws IntrospectionException {
        ServiceDefinition definition = new ServiceDefinition("Contract");
        impl.getComponentType().add(definition);
        control.replay();

        heuristic.applyHeuristics(impl, NoInterface.class, context);
        Map<String, ServiceDefinition> services = impl.getComponentType().getServices();
        assertEquals(1, services.size());
        assertSame(definition, services.get("Contract"));
        control.verify();
    }

    public static interface ServiceInterface {
    }

    public static class NoInterface {
    }

    public static class OneInterface implements ServiceInterface {
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        impl = new SystemImplementation();
        componentType = new PojoComponentType();
        impl.setComponentType(componentType);

        noInterfaceContract = createServiceContract(NoInterface.class);
        serviceInterfaceContract = createServiceContract(ServiceInterface.class);

        control = EasyMock.createControl();
        context = control.createMock(IntrospectionContext.class);
        contractProcessor = control.createMock(ContractProcessor.class);
        helper = control.createMock(IntrospectionHelper.class);
        heuristic = new SystemServiceHeuristic(contractProcessor, helper);
    }

    private ServiceContract<Type> createServiceContract(Class<?> type) {
        @SuppressWarnings("unchecked")
        ServiceContract<Type> contract = EasyMock.createMock(ServiceContract.class);
        EasyMock.expect(contract.getInterfaceName()).andStubReturn(type.getSimpleName());
        EasyMock.replay(contract);
        return contract;
    }
}