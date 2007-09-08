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

import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import org.easymock.IAnswer;
import org.osoa.sca.Conversation;

import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.services.work.WorkScheduler;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;

/**
 * @version $Rev$ $Date$
 */
public class NonBlockingInterceptorTestCase extends TestCase {
    private Interceptor next;
    private NonBlockingInterceptor interceptor;
    private WorkScheduler workScheduler;
    private Conversation convID;
    private WorkContext workContext;

    public void testInvoke() throws Exception {
        workContext.setScopeIdentifier(Scope.CONVERSATION, convID);
        final Message message = new MessageImpl();
        message.setWorkContext(workContext);
        workScheduler.scheduleWork(isA(NonBlockingInterceptor.AsyncRequest.class));
        expectLastCall().andStubAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                NonBlockingInterceptor.AsyncRequest request =
                        (NonBlockingInterceptor.AsyncRequest) getCurrentArguments()[0];
                assertSame(next, request.getNext());
                assertSame(message, request.getMessage());
                WorkContext newWorkContext = message.getWorkContext();
                assertNotSame(workContext, newWorkContext);
                assertSame(convID, newWorkContext.getScopeIdentifier(Scope.CONVERSATION));
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
        convID = EasyMock.createMock(Conversation.class);
        workContext = new SimpleWorkContext();
        workScheduler = EasyMock.createMock(WorkScheduler.class);
        next = EasyMock.createMock(Interceptor.class);
        interceptor = new NonBlockingInterceptor(workScheduler);
        interceptor.setNext(next);
    }
}
