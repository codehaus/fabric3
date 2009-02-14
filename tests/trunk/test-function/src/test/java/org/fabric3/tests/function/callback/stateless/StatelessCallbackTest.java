/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.tests.function.callback.stateless;

import junit.framework.TestCase;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.tests.function.callback.common.CallbackData;

/**
 * Tests for stateless callbacks.
 *
 * @version $Rev$ $Date$
 */
public class StatelessCallbackTest extends TestCase {
    @Reference
    protected ClientService client1;

    @Reference
    protected ClientService client2;

    /**
     * Verifies the case where two clients are wired to the same target bidirectional service and the callback is made to the correct client.
     *
     * @throws Exception
     */
    public void testSimpleCallback() throws Exception {
        CallbackData data = new CallbackData(1);
        client1.invoke(data);
        data.getLatch().await();
        assertTrue(data.isCalledBack());
    }

    /**
     * Verifies a callback from a forward invocation through a ServiceReference
     *
     * @throws Exception
     */
    public void testServiceReferenceCallback() throws Exception {
//        CountDownLatch latch = new CountDownLatch(1);
//        client1.invokeServiceReferenceCallback(latch);
//        latch.await();
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
//        latch.await();
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
//        latch.await();
//        assertTrue(client1.isCallback());
//        assertFalse(client2.isCallback());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
