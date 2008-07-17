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
package org.fabric3.binding.test;

import java.net.URI;

import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.invocation.Message;

/**
 * @version $Rev$ $Date$
 */
public class TestBindingInterceptor implements Interceptor {
    private BindingChannel channel;
    private URI destination;
    private String operation;

    public TestBindingInterceptor(BindingChannel channel, URI destination, String operation) {
        this.channel = channel;
        this.destination = destination;
        this.operation = operation;
    }

    public Message invoke(Message msg) {
        return channel.send(destination, operation, msg);
    }

    public void setNext(Interceptor next) {
        throw new UnsupportedOperationException("Interceptor must be the last in the chain");
    }

    public Interceptor getNext() {
        return null;
    }
}
