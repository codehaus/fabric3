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

import java.util.List;

import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;

/**
 * Encapsulates an invocation to be processed asynchronously.
 *
 * @version $Revision$ $Date$
 */
public class AsyncRequest implements Runnable {
    private final Interceptor next;
    private final Message message;
    private List<CallFrame> stack;

    public AsyncRequest(Interceptor next, Message message, List<CallFrame> stack) {
        this.next = next;
        this.message = message;
        this.stack = stack;
    }

    public void run() {
        WorkContext newWorkContext = new WorkContext();
        if (stack != null) {
            newWorkContext.addCallFrames(stack);
        }
        message.setWorkContext(newWorkContext);
        next.invoke(message);
    }

    public Interceptor getNext() {
        return next;
    }

    public Message getMessage() {
        return message;
    }
}
