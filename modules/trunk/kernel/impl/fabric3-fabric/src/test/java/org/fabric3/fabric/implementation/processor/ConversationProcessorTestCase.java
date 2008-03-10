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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import junit.framework.TestCase;
import org.osoa.sca.annotations.ConversationAttributes;
import org.osoa.sca.annotations.ConversationID;
import org.osoa.sca.annotations.Scope;

import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.InjectableAttribute;

/**
 * @version $Rev$ $Date$
 */
public class ConversationProcessorTestCase extends TestCase {
    private ConversationProcessor processor = new ConversationProcessor();

    public void testMaxIdleTime() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        processor.visitClass(FooMaxIdle.class, type, null);
        assertEquals(10000L, type.getMaxIdleTime());
        assertEquals(0, type.getMaxAge());
    }

    public void testMaxAge() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        processor.visitClass(FooMaxAge.class, type, null);
        assertEquals(10000L, type.getMaxAge());
        assertEquals(0, type.getMaxIdleTime());
    }

    public void testBadFooBoth() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        try {
            processor.visitClass(BadFooBoth.class, type, null);
            fail();
        } catch (InvalidConversationalImplementation e) {
            // expected
        }
    }

    public void testImplicitScope() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        processor.visitClass(ImplicitFooScope.class, type, null);
        assertEquals(org.fabric3.scdl.Scope.CONVERSATION, type.getImplementationScope());
    }

    public void testBadFooScope() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        try {
            processor.visitClass(BadFooScope.class, type, null);
            fail();
        } catch (InvalidConversationalImplementation e) {
            // expected
        }
    }

    public void testJustConversation() throws Exception {
        // TODO do we want these semantics
        PojoComponentType type = new PojoComponentType(null);
        processor.visitClass(FooJustConversation.class, type, null);
        assertEquals(org.fabric3.scdl.Scope.CONVERSATION, type.getImplementationScope());
        assertEquals(0, type.getMaxAge());
        assertEquals(0, type.getMaxIdleTime());
    }

    public void testSetConversationIdField() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        Field field = FooWithConversationIDField.class.getDeclaredField("conversationID");
        processor.visitField(field, type, null);
        assertTrue(type.getInjectionSites().containsKey(InjectableAttribute.CONVERSATION_ID));
    }

    public void testSetConversationIdMethod() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        Method method = FooWithConversationIDMethod.class.getDeclaredMethods()[0];
        processor.visitMethod(method, type, null);
        assertTrue(type.getInjectionSites().containsKey(InjectableAttribute.CONVERSATION_ID));
    }

    @Scope("CONVERSATION")
    @ConversationAttributes(maxIdleTime = "10 seconds")
    private class FooMaxIdle {
    }

    @Scope("CONVERSATION")
    @ConversationAttributes(maxAge = "10 seconds")
    private class FooMaxAge {
    }

    @Scope("CONVERSATION")
    @ConversationAttributes(maxAge = "10 seconds", maxIdleTime = "10 seconds")
    private class BadFooBoth {
    }

    @ConversationAttributes(maxAge = "10 seconds")
    private class ImplicitFooScope {
    }

    @Scope("STATELESS")
    @ConversationAttributes(maxAge = "10 seconds")
    private class BadFooScope {
    }

    @ConversationAttributes
    private class FooJustConversation {
    }

    private class FooWithConversationIDField {
        @ConversationID
        private String conversationID;
    }

    private class FooWithConversationIDMethod {
        @ConversationID
        void setConversationID(String conversationID) {
        }
    }
}
