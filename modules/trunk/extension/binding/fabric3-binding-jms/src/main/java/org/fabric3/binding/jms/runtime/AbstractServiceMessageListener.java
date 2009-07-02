/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.binding.jms.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.IOException;
import javax.jms.Message;
import javax.jms.JMSException;

import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.ConversationContext;
import org.fabric3.spi.util.Base64;
import org.fabric3.spi.component.F3Conversation;

/**
 * Base class for SourceMessageListener implementations.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractServiceMessageListener implements ServiceMessageListener {
    protected WireHolder wireHolder;
    protected Map<String, InvocationChainHolder> invocationChainMap;
    protected InvocationChainHolder onMessageHolder;

    public AbstractServiceMessageListener(WireHolder wireHolder) {
        this.wireHolder = wireHolder;
        invocationChainMap = new HashMap<String, InvocationChainHolder>();
        for (InvocationChainHolder chainHolder : wireHolder.getInvocationChains()) {
            String name = chainHolder.getChain().getPhysicalOperation().getName();
            if ("onMessage".equals(name)) {
                onMessageHolder = chainHolder;
            }
            invocationChainMap.put(name, chainHolder);
        }
    }

    protected InvocationChainHolder getInvocationChainHolder(String opName) throws JmsBadMessageException {
        List<InvocationChainHolder> chainHolders = wireHolder.getInvocationChains();
        if (chainHolders.size() == 1) {
            return chainHolders.get(0);
        } else if (opName != null) {
            InvocationChainHolder chainHolder = invocationChainMap.get(opName);
            if (chainHolder == null) {
                throw new JmsBadMessageException("Unable to match operation on the service contract: " + opName);
            }
            return chainHolder;
        } else if (onMessageHolder != null) {
            return onMessageHolder;
        } else {
            throw new JmsBadMessageException("Unable to match operation on the service contract");
        }

    }

    /**
     * Creates a WorkContext for the request by deserializing the callframe stack
     *
     * @param request     the message received from the JMS transport
     * @param callbackUri if the destination service for the message is bidirectional, the callback URI is the URI of the callback service for the
     *                    client that is wired to it. Otherwise, it is null.
     * @return the work context
     * @throws JmsBadMessageException if an error is encountered deserializing the callframe
     */
    @SuppressWarnings({"unchecked"})
    protected WorkContext createWorkContext(Message request, String callbackUri) throws JmsBadMessageException {
        try {
            WorkContext workContext = new WorkContext();
            String encoded = request.getStringProperty("f3Context");
            if (encoded == null) {
                // no callframe found, use a blank one
                return workContext;
            }
            ByteArrayInputStream bas = new ByteArrayInputStream(Base64.decode(encoded));
            ObjectInputStream stream = new ObjectInputStream(bas);
            List<CallFrame> stack = (List<CallFrame>) stream.readObject();
            workContext.addCallFrames(stack);
            stream.close();
            CallFrame previous = workContext.peekCallFrame();
            // Copy correlation and conversation information from incoming frame to new frame
            // Note that the callback URI is set to the callback address of this service so its callback wire can be mapped in the case of a
            // bidirectional service
            Serializable id = previous.getCorrelationId(Serializable.class);
            ConversationContext context = previous.getConversationContext();
            F3Conversation conversation = previous.getConversation();
            CallFrame frame = new CallFrame(callbackUri, id, conversation, context);
            stack.add(frame);
            return workContext;
        } catch (JMSException ex) {
            throw new JmsBadMessageException("Error deserializing callframe", ex);
        } catch (IOException ex) {
            throw new JmsBadMessageException("Error deserializing callframe", ex);
        } catch (ClassNotFoundException ex) {
            throw new JmsBadMessageException("Error deserializing callframe", ex);
        }
    }

    protected void addCallFrame(org.fabric3.spi.invocation.Message message, String callbackUri) {
        WorkContext workContext = message.getWorkContext();
        CallFrame previous = workContext.peekCallFrame();
        // Copy correlation and conversation information from incoming frame to new frame
        // Note that the callback URI is set to the callback address of this service so its callback wire can be mapped in the case of a
        // bidirectional service
        Serializable id = previous.getCorrelationId(Serializable.class);
        ConversationContext context = previous.getConversationContext();
        F3Conversation conversation = previous.getConversation();
        CallFrame frame = new CallFrame(callbackUri, id, conversation, context);
        workContext.addCallFrame(frame);
    }


}
