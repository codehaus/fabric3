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
package org.fabric3.binding.jms.runtime;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.fabric3.binding.jms.common.Fabric3JmsException;
import org.fabric3.binding.jms.runtime.helper.JmsHelper;

/**
 * Message listener that blocks for responses from a service. This listener is attached to the reference side of a wire.
 *
 * @version $Revison$ $Date$
 */
public class JmsResponseMessageListener {

    /**
     * Destination for receiving response.
     */
    private Destination destination;

    /**
     * Connection factory for receiving response.
     */
    private ConnectionFactory connectionFactory;

    /**
     * @param destination       Destination for sending responses.
     * @param connectionFactory Connection factory for sending responses.
     */
    public JmsResponseMessageListener(Destination destination, ConnectionFactory connectionFactory) {
        this.destination = destination;
        this.connectionFactory = connectionFactory;
    }

    /**
     * Performs a blocking receive.
     *
     * @param correlationId Correlation Id.
     * @return Received message.
     */
    public Message receive(String correlationId) {
        Connection connection = null;
        try {
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            String selector = "JMSCorrelationID = '" + correlationId + "'";
            MessageConsumer consumer = session.createConsumer(destination, selector);
            connection.start();
            Message message = consumer.receive();
            session.commit();
            return message;
        } catch (JMSException ex) {
            throw new Fabric3JmsException("Unable to receive response", ex);
        } finally {
            JmsHelper.closeQuietly(connection);
        }

    }

}
