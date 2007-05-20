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

package org.fabric3.binding.jms.wire;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.fabric3.binding.jms.Fabric3JmsException;
import org.fabric3.binding.jms.model.CorrelationScheme;
import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.MessageImpl;
import org.fabric3.spi.wire.Wire;

/**
 * Message listeher for service requests.
 * 
 * @version $Revison$ $Date$
 */
public class Fabric3MessageListener implements MessageListener {

    /**
     * Destination for sending response.
     */
    private Destination destination;

    /**
     * Connection factory for sending response.
     */
    private ConnectionFactory connectionFactory;

    /**
     * Operations available on the contract.
     */
    private Map<String, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops;
    
    /**
     * Correlation scheme.
     */
    private CorrelationScheme correlationScheme;
    
    /**
     * Wire.
     */
    private Wire wire;
    
    /**
     * @param destination Destination for sending responses.
     * @param connectionFactory Connection factory for sending responses.
     * @param ops Map of operation definitions.
     */
    public Fabric3MessageListener(Destination destination,
                                  ConnectionFactory connectionFactory,
                                  Map<String, Entry<PhysicalOperationDefinition, InvocationChain>> ops,
                                  CorrelationScheme correlationScheme,
                                  Wire wire) {
        this.destination = destination;
        this.connectionFactory = connectionFactory;
        this.ops = ops;
        this.correlationScheme = correlationScheme;
        this.wire = wire;
    }

    /**
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(Message request) {
        
        Connection connection = null;
        
        try {
            
            String opName = request.getStringProperty("scaOperationName");
            Interceptor interceptor = ops.get(opName).getValue().getHeadInterceptor();
            
            Object payload = ((ObjectMessage) request).getObject();
            
            org.fabric3.spi.wire.Message inMessage = new MessageImpl(payload, false, new SimpleWorkContext(), wire);
            org.fabric3.spi.wire.Message outMessage = interceptor.invoke(inMessage);
            
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            
            MessageProducer producer = session.createProducer(destination);
            Message response = session.createObjectMessage((Serializable) outMessage.getBody());
            
            switch(correlationScheme) {
                case RequestCorrelIDToCorrelID: {
                    response.setStringProperty("JMSCorrelationID", request.getJMSCorrelationID());
                    break;
                }
                case RequestMsgIDToCorrelID: {
                    response.setStringProperty("JMSCorrelationID", request.getJMSMessageID());
                    break;
                }
            }
            
            producer.send(response);
            session.commit();
            
        } catch(JMSException ex) {
            throw new Fabric3JmsException("Unable to send response", ex);
        } finally {
            try {
                if(connection != null) {
                    connection.stop();
                }
            } catch(JMSException ex) {
                throw new Fabric3JmsException("Unable to close connection", ex);
            }
        }
    }

}
