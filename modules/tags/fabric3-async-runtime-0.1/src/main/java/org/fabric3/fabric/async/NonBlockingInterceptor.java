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

import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.services.work.WorkScheduler;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.Wire;

/**
 * Adds non-blocking behavior to an invocation chain
 *
 * @version $$Rev$$ $$Date$$
 */
public class NonBlockingInterceptor implements Interceptor {

    protected static final Message RESPONSE = new ImmutableMessage();

    private final WorkScheduler workScheduler;
    private Interceptor next;

    public NonBlockingInterceptor(WorkScheduler workScheduler) {
        this.workScheduler = workScheduler;
    }

    public Message invoke(final Message msg) {
        WorkContext workContext = msg.getWorkContext();
        WorkContext newWorkContext = new SimpleWorkContext();
        newWorkContext.setScopeIdentifier(Scope.CONVERSATION, workContext.getScopeIdentifier(Scope.CONVERSATION));
        msg.setWorkContext(newWorkContext);
        AsyncRequest request = new AsyncRequest(next, msg);
        workScheduler.scheduleWork(request);
        return RESPONSE;
    }

    public Interceptor getNext() {
        return next;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public boolean isOptimizable() {
        return false;
    }

    protected static class AsyncRequest implements Runnable {
        private final Interceptor next;
        private final Message message;

        public AsyncRequest(Interceptor next, Message message) {
            this.next = next;
            this.message = message;
        }

        public void run() {
            next.invoke(message);
        }

        public Interceptor getNext() {
            return next;
        }

        public Message getMessage() {
            return message;
        }
    }

    /**
     * A dummy message passed back on an invocation
     */
    private static class ImmutableMessage implements Message {

        public Object getBody() {
            return null;
        }

        public void setBody(Object body) {
            if (body != null) {
                throw new UnsupportedOperationException();
            }
        }

        public WorkContext getWorkContext() {
            throw new UnsupportedOperationException();
        }

        public void setWorkContext(WorkContext workContext) {
            throw new UnsupportedOperationException();
        }

        public Wire getWire() {
            throw new UnsupportedOperationException();
        }

        public void setWire(Wire wire) {
            throw new UnsupportedOperationException();
        }

        public boolean isFault() {
            return false;
        }

        public void setBodyWithFault(Object fault) {
            throw new UnsupportedOperationException();
        }

    }

}
