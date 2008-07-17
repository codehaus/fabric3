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
package org.fabric3.fabric.async;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import org.easymock.IAnswer;

import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.services.work.WorkScheduler;
import org.fabric3.spi.wire.Interceptor;

/**
 * @version $Rev$ $Date$
 */
public class NonBlockingInterceptorTestCase extends TestCase {
    private Interceptor next;
    private NonBlockingInterceptor interceptor;
    private WorkScheduler workScheduler;
    private WorkContext workContext;

    public void testInvoke() throws Exception {
        final Message message = new MessageImpl();
        message.setWorkContext(workContext);
        workScheduler.scheduleWork(isA(AsyncRequest.class));
        expectLastCall().andStubAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                AsyncRequest request =
                        (AsyncRequest) getCurrentArguments()[0];
                request.run();
                assertSame(next, request.getNext());
                assertSame(message, request.getMessage());
                WorkContext newWorkContext = message.getWorkContext();
                assertNotSame(workContext, newWorkContext);
                return null;
            }
        });
        replay(workScheduler);
        assertSame(NonBlockingInterceptor.RESPONSE, interceptor.invoke(message));

    }

    public void testNextInterceptor() {
        assertSame(next, interceptor.getNext());
    }

    protected void setUp() throws Exception {
        super.setUp();
        workContext = new WorkContext();
        CallFrame frame = new CallFrame();
        workContext.addCallFrame(frame);

        workScheduler = EasyMock.createMock(WorkScheduler.class);
        next = EasyMock.createMock(Interceptor.class);
        interceptor = new NonBlockingInterceptor(workScheduler);
        interceptor.setNext(next);
    }
}
