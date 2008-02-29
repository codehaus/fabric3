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
package org.fabric3.tests.function.callback.conversation;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Reference;

import org.fabric3.tests.function.callback.common.CallbackData;

/**
 * Tests for stateless calbacks.
 *
 * @version $Rev: 2751 $ $Date: 2008-02-12 01:14:41 -0800 (Tue, 12 Feb 2008) $
 */
public class ConversationalCallbackTest extends TestCase {
    @Reference
    protected ConversationalClientService client;

    @Reference
    protected ConversationalClientService conversationalToCompositeClient;

    /**
     * Verfies a callback is routed back to the correct conversational client instance.
     *
     * @throws Throwable
     */
    public void testConversationalCallback() throws Throwable {
        CallbackData data = new CallbackData(1);
        client.invoke(data);
        data.getLatch().await();
        if (data.isError()) {
            throw data.getException();
        }
        assertTrue(data.isCalledBack());
        assertEquals(3, client.getCount());
    }

    /**
     * Verifies a conversational client is called back when it invokes a composite-scoped component which in turn invokes another composite-scoped
     * component via a non-blocking operation. The fabric must route back to the orignal conversational client instance as the invocation sequence is
     * processed on different threads.
     *
     * @throws Throwable
     */
    public void testConversationalToCompositeCallback() throws Throwable {
        CallbackData data = new CallbackData(1);
        conversationalToCompositeClient.invoke(data);
        data.getLatch().await();
        if (data.isError()) {
            throw data.getException();
        }
        assertTrue(data.isCalledBack());
        assertEquals(3, conversationalToCompositeClient.getCount());
    }

}