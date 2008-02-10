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
package org.fabric3.fabric.idl.java;

import java.lang.reflect.Type;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Conversational;
import org.osoa.sca.annotations.EndsConversation;

import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.introspection.impl.InvalidConversationalOperationException;
import org.fabric3.introspection.impl.DefaultContractProcessor;
import org.fabric3.introspection.ContractProcessor;

/**
 * @version $Rev$ $Date$
 */
public class ConversationalIntrospectionTestCase extends TestCase {
    private ContractProcessor registry = new DefaultContractProcessor();

    public void testServiceContractConversationalInformationIntrospection() throws Exception {
        ServiceContract<Type> contract = registry.introspect(Foo.class);
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

    public void testBadServiceContract() throws Exception {
        try {
            registry.introspect(BadFoo.class);
            fail();
        } catch (InvalidConversationalOperationException e) {
            //expected
        }
    }

    public void testNonConversationalInformationIntrospection() throws Exception {
        ServiceContract<Type> contract = registry.introspect(NonConversationalFoo.class);
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

    @Conversational
    private interface Foo {
        void operation();

        @EndsConversation
        void endOperation();
    }

    private interface BadFoo {
        void operation();

        @EndsConversation
        void endOperation();
    }

    private interface NonConversationalFoo {
        void operation();
    }

}
