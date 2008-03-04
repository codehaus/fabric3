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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osoa.sca.ServiceRuntimeException;
import org.osoa.sca.ServiceUnavailableException;
import org.osoa.sca.Conversation;
import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class BindingChannelImpl implements BindingChannel {
    private Map<URI, Holder> wires = new ConcurrentHashMap<URI, Holder>();

    public void registerDestinationWire(URI uri, Wire wire, URI callbackUri) {
        wires.put(uri, new Holder(wire, callbackUri));
    }

    public Message send(URI destination, String operation, Message msg) {
        Holder holder = wires.get(destination);
        if (holder == null) {
            throw new ServiceUnavailableException("No destination registered for [" + destination + "]");
        }
        Wire wire = holder.getWire();
        InvocationChain chain = null;
        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            if (entry.getKey().getName().equals(operation)) {
                chain = entry.getValue();
            }
        }
        if (chain == null) {
            throw new ServiceRuntimeException("Operation on " + destination + " not found [" + operation + "]");
        }
        WorkContext workContext = msg.getWorkContext();
        try {
            CallFrame previous = workContext.peekCallFrame();
            // copy correlation information from incoming frame
            Object id = previous.getCorrelationId(Object.class);
            boolean start = previous.isStartConversation();
            Conversation conversation = previous.getConversation();
            String callbackUri = holder.getCallbackUri();
            CallFrame frame = new CallFrame(callbackUri, id, conversation, start);
            workContext.addCallFrame(frame);
            return chain.getHeadInterceptor().invoke(msg);
        } finally {
            workContext.popCallFrame();
        }
    }

    private class Holder {
        private Wire wire;
        private String callbackUri;

        public Wire getWire() {
            return wire;
        }

        public String getCallbackUri() {
            return callbackUri;
        }

        private Holder(Wire wire, URI callbackUri) {
            this.wire = wire;
            if (callbackUri != null) {
                this.callbackUri = callbackUri.toString();
            }
        }
    }
}
