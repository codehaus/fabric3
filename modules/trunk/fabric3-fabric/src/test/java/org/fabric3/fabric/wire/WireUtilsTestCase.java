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
package org.fabric3.fabric.wire;

import java.lang.reflect.Method;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.fabric3.spi.model.physical.PhysicalOperationDefinition;

/**
 * @version $Rev$ $Date$
 */
public class WireUtilsTestCase extends TestCase {
    public void testCreateInterfaceToWireMapping() throws Exception {
//        Wire wire = new WireImpl();
//        Operation<Type> op = new Operation<Type>("hello", null, null, null);
//        InvocationChain chain = new InvocationChainImpl(op);
//        wire.addInvocationChain(op, chain);
//        Map<Method, ChainHolder> chains = WireUtils.createInterfaceToWireMapping(Foo.class, wire);
//        assertEquals(1, chains.size());
//        assertNotNull(chains.get(m));
    }

    public void testCreateInterfaceToWireMappingNoOperation() throws Exception {
//        Wire wire = new WireImpl();
//        Operation<Type> op = new Operation<Type>("goodbye", null, null, null);
//        InvocationChain chain = new InvocationChainImpl(op);
//        wire.addInvocationChain(op, chain);
//        try {
//            WireUtils.createInterfaceToWireMapping(Foo.class, wire);
//            fail();
//        } catch (NoMethodForOperationException e) {
//            // expected
//        }
    }

    private Method method;
    private Method methodString;
    private PhysicalOperationDefinition physicalOperationDefinition;
    private PhysicalOperationDefinition physicalOperationDefinitionString;

    public void testFindMethod() throws Exception {
        Assert.assertEquals(method, WireUtils.findMethod(Foo.class, physicalOperationDefinition));
        assertEquals(methodString, WireUtils.findMethod(Foo.class, physicalOperationDefinitionString));
    }

    protected void setUp() throws Exception {
        super.setUp();
        method = Foo.class.getMethod("operation");
        methodString = Foo.class.getMethod("operation", String.class);
        physicalOperationDefinition = new PhysicalOperationDefinition();
        physicalOperationDefinition.setName("operation");
        physicalOperationDefinitionString = new PhysicalOperationDefinition();
        physicalOperationDefinitionString.setName("operation");
        physicalOperationDefinitionString.addParameter(String.class.getName());
    }

    private interface Foo {
        void operation();

        void operation(String param);

        void operationOverload(String param1, String param2);

        void operationOverload(String param1, int param2);

    }
}
