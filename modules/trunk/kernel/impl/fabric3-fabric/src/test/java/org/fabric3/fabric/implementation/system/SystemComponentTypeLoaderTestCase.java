/*
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
package org.fabric3.fabric.implementation.system;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.idl.java.JavaInterfaceProcessorRegistryImpl;
import org.fabric3.fabric.implementation.IntrospectionRegistryImpl;
import org.fabric3.fabric.implementation.processor.ConstructorProcessor;
import org.fabric3.fabric.implementation.processor.DestroyProcessor;
import org.fabric3.fabric.implementation.processor.HeuristicPojoProcessor;
import org.fabric3.fabric.implementation.processor.ImplementationProcessorServiceImpl;
import org.fabric3.fabric.implementation.processor.InitProcessor;
import org.fabric3.fabric.implementation.processor.PropertyProcessor;
import org.fabric3.fabric.implementation.processor.ReferenceProcessor;
import org.fabric3.fabric.implementation.processor.ServiceProcessor;
import org.fabric3.loader.common.IntrospectionContextImpl;
import org.fabric3.pojo.processor.ImplementationProcessorService;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.Property;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.idl.java.JavaServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class SystemComponentTypeLoaderTestCase extends TestCase {
    private SystemComponentTypeLoaderImpl loader;

    public void testIntrospectUnannotatedClass() throws Exception {
        SystemImplementation impl = new SystemImplementation(BasicInterfaceImpl.class.getName());
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        IntrospectionContextImpl context = new IntrospectionContextImpl(cl, null, null);
        PojoComponentType componentType = loader.loadByIntrospection(impl, context);
        ServiceDefinition service = componentType.getServices().get(BasicInterface.class.getSimpleName());
        JavaServiceContract contract = JavaServiceContract.class.cast(service.getServiceContract());
        assertEquals(BasicInterface.class.getName(), contract.getInterfaceClass());
        Property<?> property = componentType.getProperties().get("publicProperty");
        assertEquals(String.class, property.getJavaType());
        ReferenceDefinition referenceDefinition = componentType.getReferences().get("protectedReference");
        JavaServiceContract refContract = JavaServiceContract.class.cast(referenceDefinition.getServiceContract());
        assertEquals(BasicInterface.class.getName(), refContract.getInterfaceClass());
    }

    protected void setUp() throws Exception {
        super.setUp();
        JavaInterfaceProcessorRegistryImpl interfaceProcessorRegistry = new JavaInterfaceProcessorRegistryImpl();
        ImplementationProcessorService service =
                new ImplementationProcessorServiceImpl(interfaceProcessorRegistry);
        IntrospectionRegistryImpl registry = new IntrospectionRegistryImpl();
        registry.setMonitor(EasyMock.createMock(IntrospectionRegistryImpl.Monitor.class));
        registry.registerProcessor(new ConstructorProcessor(service));
        registry.registerProcessor(new DestroyProcessor());
        registry.registerProcessor(new InitProcessor());
        registry.registerProcessor(new PropertyProcessor(service));
        registry.registerProcessor(new ReferenceProcessor(interfaceProcessorRegistry));
        registry.registerProcessor(new ServiceProcessor(service));
        registry.registerProcessor(new HeuristicPojoProcessor(service, null));
        loader = new SystemComponentTypeLoaderImpl(registry);
    }

    public interface BasicInterface {
        String returnsProperty();

        BasicInterface returnsReference();

        int returnsInt();
    }

    public static class BasicInterfaceImpl implements BasicInterface {

        @org.osoa.sca.annotations.Property
        public String publicProperty;

        @Reference(required = false)
        public BasicInterface publicReference;

        @org.osoa.sca.annotations.Property
        protected String protectedProperty;

        @Reference(required = false)
        protected BasicInterface protectedReference;

        private String privateProperty;

        private BasicInterface privateReference;

        @org.osoa.sca.annotations.Property
        public void setPrivateProperty(String privateProperty) {
            this.privateProperty = privateProperty;
        }

        @Reference(required = false)
        public void setPrivateReference(BasicInterface privateReference) {
            this.privateReference = privateReference;
        }

        public String returnsProperty() {
            return privateProperty;
        }

        public BasicInterface returnsReference() {
            return privateReference;
        }

        public int returnsInt() {
            return 0;
        }


    }
}
