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

import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.wire.InvocationChain;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class InvocationChainImplTestCase extends TestCase {

    public void testInsertAtPos() throws Exception {
        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
        InvocationChain chain = new InvocationChainImpl(operation);
        Interceptor inter3 = new MockInterceptor();
        Interceptor inter2 = new MockInterceptor();
        Interceptor inter1 = new MockInterceptor();
        chain.addInterceptor(inter3);
        chain.addInterceptor(0, inter1);
        chain.addInterceptor(1, inter2);
        Interceptor head = chain.getHeadInterceptor();
        assertEquals(inter1, head);
        assertEquals(inter2, head.getNext());
        assertEquals(inter3, head.getNext().getNext());
    }

    public void testInsertAtEnd() throws Exception {
        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
        InvocationChain chain = new InvocationChainImpl(operation);
        Interceptor inter2 = new MockInterceptor();
        Interceptor inter1 = new MockInterceptor();
        chain.addInterceptor(0, inter1);
        chain.addInterceptor(1, inter2);
        Interceptor head = chain.getHeadInterceptor();
        assertEquals(inter1, head);
        assertEquals(inter2, head.getNext());
        assertEquals(inter2, chain.getTailInterceptor());

    }

    private class MockInterceptor implements Interceptor {

        private Interceptor next;

        public Message invoke(Message msg) {
            return null;
        }

        public void setNext(Interceptor next) {
            this.next = next;
        }

        public Interceptor getNext() {
            return next;
        }

        public boolean isOptimizable() {
            return false;
        }
    }


}
