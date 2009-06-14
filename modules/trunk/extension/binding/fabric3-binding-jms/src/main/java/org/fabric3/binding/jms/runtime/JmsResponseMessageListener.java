/*
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
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

import org.oasisopen.sca.ServiceRuntimeException;

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
            // bubble exception to the client
            throw new ServiceRuntimeException("Unable to receive response for message with correlation id: " + correlationId, ex);
        } finally {
            JmsHelper.closeQuietly(connection);
        }

    }

}
