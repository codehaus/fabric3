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
import java.util.Collection;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.Conversational;
import org.osoa.sca.annotations.EndsConversation;

import org.fabric3.introspection.java.ContractProcessor;
import org.fabric3.introspection.java.IntrospectionHelper;
import org.fabric3.introspection.java.InvalidServiceContractException;
import org.fabric3.introspection.java.TypeMapping;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class DefaultContractProcessorTestCase extends TestCase {
    private ContractProcessor impl;
    private TypeMapping boundMapping;
    private TypeMapping emptyMapping;

    public void testSimpleInterface() throws InvalidServiceContractException {
        ServiceContract<Type> contract = impl.introspect(emptyMapping, Simple.class);
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

    public void testBoundGenericInterface() throws InvalidServiceContractException {
        ServiceContract<Type> contract = impl.introspect(boundMapping, Generic.class);
        assertEquals("Generic", contract.getInterfaceName());

        List<Operation<Type>> operations = contract.getOperations();
        assertEquals(2, operations.size());
        Operation<Type> operation = operations.get(0);
        assertEquals("echo", operation.getName());

        DataType<Type> returnType = operation.getOutputType();
        assertEquals(Base.class, returnType.getPhysical());

    }

    public void testMethodGeneric() throws InvalidServiceContractException {
        ServiceContract<Type> contract = impl.introspect(boundMapping, Generic.class);
        List<Operation<Type>> operations = contract.getOperations();
        Operation<Type> operation = operations.get(1);
        assertEquals("echo2", operation.getName());

        DataType<Type> returnType = operation.getOutputType();
//        assertEquals(Collection.class, returnType.getPhysical());
    }

    public void testCallbackInterface() throws InvalidServiceContractException {
        ServiceContract<?> contract = impl.introspect(emptyMapping, ForwardInterface.class);
        ServiceContract<?> callback = contract.getCallbackContract();
        assertEquals("CallbackInterface", callback.getInterfaceName());
        assertEquals(CallbackInterface.class.getName(), callback.getQualifiedInterfaceName());
        List<? extends Operation<?>> operations = callback.getOperations();
        assertEquals(1, operations.size());
        Operation<?> back = operations.get(0);
        assertEquals("back", back.getName());
    }

    public void testConversationalInformationIntrospection() throws Exception {
        ServiceContract<Type> contract = impl.introspect(emptyMapping, Foo.class);
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
        ServiceContract<Type> contract = impl.introspect(emptyMapping, NonConversationalFoo.class);
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
            impl.introspect(emptyMapping, BadConversation.class);
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
        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        impl = new DefaultContractProcessor(helper);
        emptyMapping = new TypeMapping();
        boundMapping = helper.mapTypeParameters(BoundImpl.class);

    }

    private static interface Base {
        int baseInt(int param) throws IllegalArgumentException;
    }

    private static interface Simple extends Base {
    }

    private static interface Generic<T extends Base> {
        T echo(T t);

        <Q extends Collection<?>> Q echo2(Q q);
    }

    private static class GenericImpl<T extends Base> implements Generic<T> {
        public T echo(T t) {
            return t;
        }

        public <Q extends Collection<?>> Q echo2(Q q) {
            return q;
        }
    }

    private static class BoundImpl extends GenericImpl<Simple> {
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
