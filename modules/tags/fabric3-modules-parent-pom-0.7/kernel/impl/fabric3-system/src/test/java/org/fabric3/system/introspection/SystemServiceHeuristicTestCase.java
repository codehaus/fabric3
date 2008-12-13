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
 * --- Original Apache License ---
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionException;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.contract.ContractProcessor;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.system.scdl.SystemImplementation;

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
    private TypeMapping typeMapping;

    public void testNoInterface() throws IntrospectionException {
        EasyMock.expect(helper.getImplementedInterfaces(NoInterface.class)).andReturn(NOCLASSES);
        IntrospectionContext context = new DefaultIntrospectionContext(null, null, null, null, typeMapping);
        EasyMock.expect(contractProcessor.introspect(typeMapping, NoInterface.class, context)).andReturn(noInterfaceContract);
        control.replay();

        heuristic.applyHeuristics(impl, NoInterface.class, context);
        Map<String, ServiceDefinition> services = componentType.getServices();
        assertEquals(1, services.size());
        assertEquals(noInterfaceContract, services.get("NoInterface").getServiceContract());
        control.verify();
    }

    public void testWithInterface() throws IntrospectionException {
        Set<Class<?>> interfaces = new HashSet<Class<?>>();
        interfaces.add(ServiceInterface.class);

        IntrospectionContext context = new DefaultIntrospectionContext(null, null, null, null, typeMapping);
        EasyMock.expect(helper.getImplementedInterfaces(OneInterface.class)).andReturn(interfaces);
        EasyMock.expect(contractProcessor.introspect(typeMapping, ServiceInterface.class, context)).andReturn(
                serviceInterfaceContract);
        control.replay();

        heuristic.applyHeuristics(impl, OneInterface.class, context);
        Map<String, ServiceDefinition> services = componentType.getServices();
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
        typeMapping = new TypeMapping();
        context = control.createMock(IntrospectionContext.class);
        EasyMock.expect(context.getTypeMapping()).andStubReturn(typeMapping);
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