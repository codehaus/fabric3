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
import java.util.Map;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;

import org.fabric3.introspection.ClassWalker;
import org.fabric3.introspection.ContractProcessor;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;

/**
 * @version $Rev$ $Date$
 */
public class SystemComponentTypeLoader2TestCase extends TestCase {
    private static final Set<Class<?>> NOCLASSES = Collections.emptySet();
    private SystemComponentTypeLoaderImpl2 loader;
    private ClassWalker<SystemImplementation> classWalker;
    private ContractProcessor contractProcessor;
    private IntrospectionHelper helper;
    private IntrospectionContext context;
    private SystemImplementation impl;
    private PojoComponentType componentType;
    private ServiceContract<Type> serviceInterfaceContract;
    private ServiceContract<Type> noInterfaceContract;

    public void testServiceHeuristicsNoInterface() throws IntrospectionException {
        EasyMock.expect(helper.getImplementedInterfaces(NoInterface.class)).andReturn(NOCLASSES);
        EasyMock.expect(contractProcessor.introspect(NoInterface.class)).andReturn(noInterfaceContract);
        EasyMock.replay(helper, contractProcessor);

        loader.serviceHeuristics(impl, NoInterface.class, context);
        Map<String,ServiceDefinition> services = componentType.getServices();
        assertEquals(1, services.size());
        assertEquals(noInterfaceContract, services.get("NoInterface").getServiceContract());
        EasyMock.verify(helper, contractProcessor);
    }

    public void testServiceHeuristicsWithInterface() throws IntrospectionException {
        Set<Class<?>> interfaces = new HashSet<Class<?>>();
        interfaces.add(ServiceInterface.class);
        
        EasyMock.expect(helper.getImplementedInterfaces(OneInterface.class)).andReturn(interfaces);
        EasyMock.expect(contractProcessor.introspect(ServiceInterface.class)).andReturn(serviceInterfaceContract);
        EasyMock.replay(helper, contractProcessor);

        loader.serviceHeuristics(impl, OneInterface.class, context);
        Map<String,ServiceDefinition> services = componentType.getServices();
        assertEquals(1, services.size());
        assertEquals(serviceInterfaceContract, services.get("ServiceInterface").getServiceContract());
        EasyMock.verify(helper, contractProcessor);
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

        context = EasyMock.createMock(IntrospectionContext.class);
        classWalker = EasyMock.createMock(ClassWalker.class);
        contractProcessor = EasyMock.createMock(ContractProcessor.class);
        helper = EasyMock.createMock(IntrospectionHelper.class);
        this.loader = new SystemComponentTypeLoaderImpl2(classWalker, contractProcessor, helper);
    }

    private ServiceContract<Type> createServiceContract(Class<?> type) {
        @SuppressWarnings("unchecked")
        ServiceContract<Type> contract = EasyMock.createMock(ServiceContract.class);
        EasyMock.expect(contract.getInterfaceName()).andStubReturn(type.getSimpleName());
        EasyMock.replay(contract);
        return contract;
    }
}
