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
 * --- Original Apache License ---
 *
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
package org.fabric3.binding.jms.runtime.helper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.osoa.sca.Conversation;

import org.fabric3.binding.jms.common.Fabric3JmsException;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.ConversationContext;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.util.Base64;

/**
 * Helper class for JMS ops.
 */
public class JmsHelper {

    /**
     * Utility class constructor.
     */
    private JmsHelper() {
    }

    /**
     * Creates a WorkContext for the request by deserializing the callframe stack
     *
     * @param request     the message received from the JMS transport
     * @param callbackUri if the destination service for the message is bidirectional, the callback URI is the URI of the callback service for the
     *                    client that is wired to it. Otherwise, it is null.
     * @return the work context
     * @throws Fabric3JmsException if an error is encountered deserializing the callframe
     */
    @SuppressWarnings({"unchecked"})
    public static WorkContext createWorkContext(Message request, String callbackUri) throws Fabric3JmsException {
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
            Object id = previous.getCorrelationId(Object.class);
            ConversationContext context = previous.getConversationContext();
            Conversation conversation = previous.getConversation();
            CallFrame frame = new CallFrame(callbackUri, id, conversation, context);
            stack.add(frame);
            return workContext;
        } catch (JMSException ex) {
            throw new Fabric3JmsException("Error deserializing callframe", ex);
        } catch (IOException ex) {
            throw new Fabric3JmsException("Error deserializing callframe", ex);
        } catch (ClassNotFoundException ex) {
            throw new Fabric3JmsException("Error deserializing callframe", ex);
        }
    }

    /**
     * Closes connections quietly.
     *
     * @param connection Connection to be closed.
     */
    public static void closeQuietly(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException ignore) {
        }
    }

    /**
     * Closes sessions quietly.
     *
     * @param session Connection to be closed.
     */
    public static void closeQuietly(Session session) {
        try {
            if (session != null) {
                session.close();
            }
        } catch (JMSException ignore) {
        }
    }

    /**
     * Closes message producer quietly.
     *
     * @param producer Message producer to be closed.
     */
    public static void closeQuietly(MessageProducer producer) {
        try {
            if (producer != null) {
                producer.close();
            }
        } catch (JMSException ignore) {
        }
    }

    /**
     * Closes message consumer quietly.
     *
     * @param consumer Message consumer to be closed.
     */
    public static void closeQuietly(MessageConsumer consumer) {
        try {
            if (consumer != null) {
                consumer.close();
            }
        } catch (JMSException ignore) {
        }
    }

}
