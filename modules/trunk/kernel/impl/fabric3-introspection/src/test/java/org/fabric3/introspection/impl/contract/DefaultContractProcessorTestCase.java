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
package org.fabric3.introspection.impl.contract;

import java.lang.reflect.Type;
import java.util.List;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.EndsConversation;
import org.osoa.sca.annotations.Conversational;

import org.fabric3.introspection.InvalidServiceContractException;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class DefaultContractProcessorTestCase extends TestCase {
    private DefaultContractProcessor impl;

    public void testSimpleInterface() throws InvalidServiceContractException {
        ServiceContract<Type> contract = impl.introspect(Simple.class);
        assertEquals("Simple", contract.getInterfaceName());
        assertEquals(Simple.class.getName(), contract.getQualifiedInterfaceName());
        List<Operation<Type>> operations = contract.getOperations();
        assertEquals(1, operations.size());
        Operation<Type> baseInt = operations.get(0);
        assertNotNull(baseInt);

        DataType<Type> returnType = baseInt.getOutputType();
        assertEquals(Integer.TYPE, returnType.getPhysical());
        assertEquals(Integer.TYPE, returnType.getLogical());

        List<DataType<Type>> parameterTypes = baseInt.getInputType().getLogical();
        assertEquals(1, parameterTypes.size());
        DataType<Type> arg0 = parameterTypes.get(0);
        assertEquals(Integer.TYPE, arg0.getPhysical());
        assertEquals(Integer.TYPE, arg0.getLogical());

        List<DataType<Type>> faultTypes = baseInt.getFaultTypes();
        assertEquals(1, faultTypes.size());
        DataType<Type> fault0 = faultTypes.get(0);
        assertEquals(IllegalArgumentException.class, fault0.getPhysical());
        assertEquals(IllegalArgumentException.class, fault0.getLogical());
    }

    public void testCallbackInterface() throws InvalidServiceContractException {
        ServiceContract<?> contract = impl.introspect(ForwardInterface.class);
        ServiceContract<?> callback = contract.getCallbackContract();
        assertEquals("CallbackInterface", callback.getInterfaceName());
        assertEquals(CallbackInterface.class.getName(), callback.getQualifiedInterfaceName());
        List<? extends Operation<?>> operations = callback.getOperations();
        assertEquals(1, operations.size());
        Operation<?> back = operations.get(0);
        assertEquals("back", back.getName());
    }

    public void testConversationalInformationIntrospection() throws Exception {
        ServiceContract<Type> contract = impl.introspect(Foo.class);
        assertTrue(contract.isConversational());
        boolean testedContinue = false;
        boolean testedEnd = false;
        for (Operation<Type> operation : contract.getOperations()) {
            if (operation.getName().equals("operation")) {
                assertEquals(Operation.CONVERSATION_CONTINUE, operation.getConversationSequence());
                testedContinue = true;
            } else if (operation.getName().equals("endOperation")) {
                assertEquals(Operation.CONVERSATION_END, operation.getConversationSequence());
                testedEnd = true;
            }
        }
        assertTrue(testedContinue);
        assertTrue(testedEnd);
    }

    public void testNonConversationalInformationIntrospection() throws Exception {
        ServiceContract<Type> contract = impl.introspect(NonConversationalFoo.class);
        assertFalse(contract.isConversational());
        boolean tested = false;
        for (Operation<Type> operation : contract.getOperations()) {
            if (operation.getName().equals("operation")) {
                int seq = operation.getConversationSequence();
                assertEquals(Operation.NO_CONVERSATION, seq);
                tested = true;
            }
        }
        assertTrue(tested);
    }

    public void testInvalidConversationalAttribute() throws Exception {
        try {
            impl.introspect(BadConversation.class);
            fail();
        } catch (InvalidConversationalOperationException e) {
            //expected
        }
    }

/*
    public void testUnregister() throws Exception {
        JavaInterfaceProcessor processor = createMock(JavaInterfaceProcessor.class);
        processor.visitInterface(eq(Base.class), isA(JavaServiceContract.class));
        processor.visitOperation(eq(Base.class.getMethod("baseInt", Integer.TYPE)), isA(Operation.class));
        expectLastCall().once();
        replay(processor);
        impl.registerProcessor(processor);
        impl.introspect(Base.class);
        impl.unregisterProcessor(processor);
        impl.introspect(Base.class);
        verify(processor);
    }
*/

    protected void setUp() throws Exception {
        super.setUp();
        impl = new DefaultContractProcessor();

    }

    private static interface Base {
        int baseInt(int param) throws IllegalArgumentException;
    }

    private static interface Simple extends Base {
    }

    @Callback(CallbackInterface.class)
    private static interface ForwardInterface {
        int forward() throws IllegalArgumentException;
    }

    private static interface CallbackInterface {
        int back() throws IllegalArgumentException;
    }

    private interface NonConversationalFoo {
        void operation();
    }

    @Conversational
    private interface Foo {
        void operation();

        @EndsConversation
        void endOperation();
    }

    private static interface BadConversation {
        void operation();

        @EndsConversation
        void endOperation();
    }

}
