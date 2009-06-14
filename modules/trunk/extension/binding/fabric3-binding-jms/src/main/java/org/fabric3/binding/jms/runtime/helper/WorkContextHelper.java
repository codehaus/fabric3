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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.jms.runtime.helper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;
import javax.jms.JMSException;
import javax.jms.Message;

import org.fabric3.binding.jms.runtime.JmsBadMessageException;
import org.fabric3.spi.component.F3Conversation;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.ConversationContext;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.util.Base64;

/**
 * Helper class for reconstituting WorkContexts.
 */
public class WorkContextHelper {

    /**
     * Utility class constructor.
     */
    private WorkContextHelper() {
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
    public static WorkContext createWorkContext(Message request, String callbackUri) throws JmsBadMessageException {
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

    public static void addCallFrame(org.fabric3.spi.invocation.Message message, String callbackUri) {
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