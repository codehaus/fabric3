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
import org.osoa.sca.annotations.Conversational;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Remotable;

import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.pojo.processor.ImplementationProcessorService;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;

/**
 * @version $Rev$ $Date$
 */
public class ImplementationProcessorServiceTestCase extends TestCase {

    private ImplementationProcessorService implService =
            new ImplementationProcessorServiceImpl(new DefaultContractProcessor(), new DefaultIntrospectionHelper());

    public void testCreateConversationalService() throws Exception {
        ServiceDefinition service = implService.createService(Foo.class);
        ServiceContract contract = service.getServiceContract();
        assertTrue(Foo.class.getName().equals(contract.getQualifiedInterfaceName()));
        assertTrue(contract.isConversational());
    }

    public void testCreateDefaultService() throws Exception {
        ServiceDefinition service = implService.createService(Baz.class);
        ServiceContract contract = service.getServiceContract();
        assertTrue(Baz.class.getName().equals(contract.getQualifiedInterfaceName()));
        assertFalse(service.getServiceContract().isConversational());
    }

    @Conversational
    @Callback(Bar.class)
    @Remotable
    public interface Foo {

    }

    public interface Bar {

    }

    public interface Baz {

    }

    public static class PropertyClass {
        private int foo;

        public PropertyClass(@Property(name = "foo")int foo) {
            this.foo = foo;
        }

        public int getFoo() {
            return foo;
        }
    }
}
