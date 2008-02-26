/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.tests.function.callback.stateless;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Reference;

import org.fabric3.tests.function.callback.common.CallbackData;

/**
 * Tests for stateless calbacks.
 *
 * @version $Rev$ $Date$
 */
public class StatelessCallbackTest extends TestCase {
    @Reference
    protected ClientService client1;

//    @Reference
//    protected ClientService client2;

    /**
     * Verifies the case where two clients are wired to the same target service.
     *
     * @throws Exception
     */
    public void testSimpleCallback() throws Exception {
        CallbackData data = new CallbackData(1);
        client1.invoke(data);
//        latch.await(4000, TimeUnit.MILLISECONDS);
        data.getLatch().await();
        assertTrue(data.isCalledBack());
        // test that the other client was not issued a callback
        // assertFalse(client2.isCallback());
    }

    /**
     * Verifies a callback from a forward invocation through a ServiceReference
     *
     * @throws Exception
     */
    public void testServiceReferenceCallback() throws Exception {
//        CountDownLatch latch = new CountDownLatch(1);
//        client1.invokeServiceReferenceCallback(latch);
//        latch.await(4000, TimeUnit.MILLISECONDS);
//        assertTrue(client1.isCallback());
//        // test that the other client was not issued a callback
//        assertFalse(client2.isCallback());
    }

    /**
     * Verifies callbacks are routed for a sequence of two forward invocations:
     * <pre>
     * CallbackClient--->ForwardService--->EndService
     * </pre>
     *
     * @throws Exception
     */
    public void testMultipleHopCallback() throws Exception {
//        CountDownLatch latch = new CountDownLatch(1);
//        client1.invokeMultipleHops(latch);
//        latch.await(4000, TimeUnit.MILLISECONDS);
//        assertTrue(client1.isCallback());
//        assertFalse(client2.isCallback());
    }

    /**
     * Verifies a callback is routed through a CallableReference passed to another service.
     *
     * @throws Exception
     */
    public void testNoCallbackServiceReference() throws Exception {
//        CountDownLatch latch = new CountDownLatch(2);
//        client1.invokeNoCallbackServiceReference(latch);
//        latch.await(4000, TimeUnit.MILLISECONDS);
//        assertTrue(client1.isCallback());
//        assertFalse(client2.isCallback());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
