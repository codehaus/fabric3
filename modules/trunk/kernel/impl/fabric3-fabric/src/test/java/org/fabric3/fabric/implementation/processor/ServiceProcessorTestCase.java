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
package org.fabric3.fabric.implementation.processor;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.Remotable;
import org.osoa.sca.annotations.Service;

import org.fabric3.introspection.ContractProcessor;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.pojo.scdl.JavaMappedService;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class ServiceProcessorTestCase extends TestCase {
    private ServiceProcessor processor;
    private PojoComponentType type;

    public void testMultipleInterfaces() throws Exception {
        processor.visitClass(FooMultiple.class, type, null);
        assertEquals(2, type.getServices().size());
        JavaMappedService service = type.getServices().get(Baz.class.getSimpleName());
        ServiceContract contract = service.getServiceContract();
        assertEquals(Baz.class.getName(), contract.getQualifiedInterfaceName());
        assertNotNull(type.getServices().get(Bar.class.getSimpleName()));
    }

    public void testSingleInterfaces() throws Exception {
        processor.visitClass(FooSingle.class, type, null);
        assertEquals(1, type.getServices().size());
        assertNotNull(type.getServices().get(Baz.class.getSimpleName()));
    }

    public void testMultipleNoService() throws Exception {
        processor.visitClass(FooMultipleNoService.class, type, null);
        assertEquals(0, type.getServices().size());
    }

    /**
     * Verifies a service with a callback annotation is recognized
     */
    public void testMultipleWithCallbackAnnotation() throws Exception {
        processor.visitClass(FooMultipleWithCalback.class, type, null);
        assertEquals(1, type.getServices().size());
    }

    public void testRemotableNoService() throws Exception {
        processor.visitClass(FooRemotableNoService.class, type, null);
        assertEquals(1, type.getServices().size());
        JavaMappedService service = type.getServices().get(BazRemotable.class.getSimpleName());
        ServiceContract contract = service.getServiceContract();
        assertEquals(BazRemotable.class.getName(), contract.getQualifiedInterfaceName());
    }

    public void testNonInterface() throws Exception {
        try {
            processor.visitClass(BadImpl.class, type, null);
            fail();
        } catch (InvalidServiceType e) {
            //expected
        }
    }

    public void testNoInterfaces() throws Exception {
        try {
            processor.visitClass(BadDefinition.class, type, null);
            fail();
        } catch (IllegalServiceDefinitionException e) {
            //expected
        }
    }

    public void testSpecifiedOnSuperClass() throws Exception {
        processor.visitClass(ServiceOnSuper.class, type, null);
        assertEquals(1, type.getServices().size());
    }

    protected void setUp() throws Exception {
        super.setUp();
        ContractProcessor contractProcessor = new DefaultContractProcessor();
        processor = new ServiceProcessor(new ImplementationProcessorServiceImpl(contractProcessor, new DefaultIntrospectionHelper()));
        type = new PojoComponentType(null);
    }

    @Callback(Bar.class)
    private interface Baz {
    }

    private interface Bar {
    }

    private interface Bar2 {
    }

    @Remotable
    private interface BazRemotable {
    }

    @Service(interfaces = {Baz.class, Bar.class})
    private class FooMultiple implements Baz, Bar {

    }

    @Service(Baz.class)
    private class FooSingle implements Baz, Bar {

    }

    private class FooMultipleNoService implements Bar, Bar2 {

    }

    private class FooMultipleWithCalback implements Baz, Bar {

    }

    private class FooRemotableNoService implements BazRemotable, Bar {

    }

    @Service(FooSingle.class)
    private class BadImpl extends FooSingle {

    }


    @Service()
    private class BadDefinition extends FooSingle {

    }

    private class ServiceOnSuper extends FooSingle {

    }

}
