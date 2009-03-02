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
package org.fabric3.binding.test;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.ServiceUnavailableException;
import org.osoa.sca.Conversation;
import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.ConversationContext;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.InvocationChain;
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
            ConversationContext context = previous.getConversationContext();
            Conversation conversation = previous.getConversation();
            String callbackUri = holder.getCallbackUri();
            CallFrame frame = new CallFrame(callbackUri, id, conversation, context);
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
