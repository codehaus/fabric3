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

package org.fabric3.binding.aq.transport;

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

import org.fabric3.binding.aq.Fabric3AQException;
import org.fabric3.binding.aq.TransactionType;
import org.fabric3.binding.aq.helper.JmsHelper;
import org.fabric3.binding.aq.model.CorrelationScheme;
import org.fabric3.binding.aq.tx.TransactionHandler;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.MessageImpl;

/**
 * Message listeher for service requests.
 * 
 * @version $Revison$ $Date$
 */
public class Fabric3MessageListener implements MessageListener {

    /**
     * Destination for sending response.
     */
    private Destination respDestination;

    /**
     * Connection factory for sending response.
     */
    private ConnectionFactory respConnectionFactory;

    /**
     * Operations available on the contract.
     */
    private Map<String, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops;

    /**
     * Correlation scheme.
     */
    private CorrelationScheme correlationScheme;

    /**
     * Transaction handler.
     */
    private TransactionHandler transactionHandler;

    /**
     * Transaction type.
     */
    private TransactionType transactionType;

    /**
     * @param destination
     *            Destination for sending responses.
     * @param connectionFactory
     *            Connection factory for sending responses.
     * @param ops
     *            Map of operation definitions.
     * @param correlationScheme
     *            Correlation scheme.
     * @param wire
     *            Wire associated with this listener.
     * @param transactionHandler
     *            Transaction handler.
     */
    public Fabric3MessageListener(Destination respDestination, ConnectionFactory respConnectionFactory,
            Map<String, Entry<PhysicalOperationDefinition, InvocationChain>> ops, CorrelationScheme correlationScheme,
            TransactionHandler transactionHandler, TransactionType transactionType) {
        this.respDestination = respDestination;
        this.respConnectionFactory = respConnectionFactory;
        this.ops = ops;
        this.correlationScheme = correlationScheme;
        this.transactionHandler = transactionHandler;
        this.transactionType = transactionType;
    }

    /**
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(final Message request) {
        final org.fabric3.spi.wire.Message outMessage;
        try {
            outMessage = handleInboundMessage(request);
            
            if (respDestination != null && respConnectionFactory != null) {
                /** TODO REFACTOR USED NULL CHECK WILL BE EVALUATED IN A STRATEGY */
                sendResponse(request, outMessage);
            }
        } catch (JMSException je) {
            throw new Fabric3AQException("Error on Reading Message", je);
        }
    }

    private void sendResponse(Message request, org.fabric3.spi.wire.Message outMessage) {
        Connection connection = null;
        try {
            connection = respConnectionFactory.createConnection();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            if (transactionType == TransactionType.GLOBAL) {
                transactionHandler.enlist(session);
            }

            MessageProducer producer = session.createProducer(respDestination);
            Message response = session.createObjectMessage((Serializable) outMessage.getBody());

            switch (correlationScheme) {
            case RequestCorrelIDToCorrelID: {
                break;
            }
            case RequestMsgIDToCorrelID: {
                response.setStringProperty("MsgIDToCorrlID", request.getJMSMessageID());
                break;
            }
            }

            producer.send(response);

            if (transactionType == TransactionType.LOCAL) {
                session.commit();
            }
        } catch (JMSException je) {
            throw new Fabric3AQException("Cannot forward Response ", je);
        } finally {
            JmsHelper.closeQuietly(connection);
        }

    }

    /**
     * Handle Inbound Message
     * 
     * @param request
     * @return Message
     * @throws JMSException
     */
    private org.fabric3.spi.wire.Message handleInboundMessage(final Message request) throws JMSException {
        final String opName = request.getStringProperty("scaOperationName");
        final Interceptor interceptor = getInterceptor(opName);

        final ObjectMessage objectMessage = (ObjectMessage) request;
        final Object[] payload = (Object[]) objectMessage.getObject();

        return invokeOnService(interceptor, payload);
    }

    /**
     * Invoke On Service
     * 
     * @param interceptor
     * @param payload
     * @return
     */
    private org.fabric3.spi.wire.Message invokeOnService(final Interceptor interceptor, final Object[] payload) {
        final org.fabric3.spi.wire.Message inMessage = new MessageImpl(payload, false, new WorkContext());
        return interceptor.invoke(inMessage);
    }

    /*
     * Finds the matching interceptor.
     */
    private Interceptor getInterceptor(String opName) {

        if (ops.size() == 1) {
            return ops.values().iterator().next().getValue().getHeadInterceptor();
        } else if (opName != null && ops.containsKey(opName)) {
            return ops.get(opName).getValue().getHeadInterceptor();
        } else if (ops.containsKey("onMessage")) {
            return ops.get("onMessage").getValue().getHeadInterceptor();
        } else {
            throw new Fabric3AQException("Unable to match operation on the service contract");
        }

    }

}
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

package org.fabric3.binding.aq.transport;

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

import org.fabric3.binding.aq.Fabric3AQException;
import org.fabric3.binding.aq.TransactionType;
import org.fabric3.binding.aq.helper.JmsHelper;
import org.fabric3.binding.aq.model.CorrelationScheme;
import org.fabric3.binding.aq.tx.TransactionHandler;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.MessageImpl;

/**
 * Message listeher for service requests.
 * 
 * @version $Revison$ $Date$
 */
public class Fabric3MessageListener implements MessageListener {

    /**
     * Destination for sending response.
     */
    private Destination respDestination;

    /**
     * Connection factory for sending response.
     */
    private ConnectionFactory respConnectionFactory;

    /**
     * Operations available on the contract.
     */
    private Map<String, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops;

    /**
     * Correlation scheme.
     */
    private CorrelationScheme correlationScheme;

    /**
     * Transaction handler.
     */
    private TransactionHandler transactionHandler;

    /**
     * Transaction type.
     */
    private TransactionType transactionType;

    /**
     * @param destination
     *            Destination for sending responses.
     * @param connectionFactory
     *            Connection factory for sending responses.
     * @param ops
     *            Map of operation definitions.
     * @param correlationScheme
     *            Correlation scheme.
     * @param wire
     *            Wire associated with this listener.
     * @param transactionHandler
     *            Transaction handler.
     */
    public Fabric3MessageListener(Destination respDestination, ConnectionFactory respConnectionFactory,
            Map<String, Entry<PhysicalOperationDefinition, InvocationChain>> ops, CorrelationScheme correlationScheme,
            TransactionHandler transactionHandler, TransactionType transactionType) {
        this.respDestination = respDestination;
        this.respConnectionFactory = respConnectionFactory;
        this.ops = ops;
        this.correlationScheme = correlationScheme;
        this.transactionHandler = transactionHandler;
        this.transactionType = transactionType;
    }

    /**
     * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
     */
    public void onMessage(final Message request) {
        final org.fabric3.spi.wire.Message outMessage;
        try {
            outMessage = handleInboundMessage(request);
            
            if (respDestination != null && respConnectionFactory != null) {
                /** TODO REFACTOR USED NULL CHECK WILL BE EVALUATED IN A STRATEGY */
                sendResponse(request, outMessage);
            }
        } catch (JMSException je) {
            throw new Fabric3AQException("Error on Reading Message", je);
        }
    }

    private void sendResponse(Message request, org.fabric3.spi.wire.Message outMessage) {
        Connection connection = null;
        try {
            connection = respConnectionFactory.createConnection();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            if (transactionType == TransactionType.GLOBAL) {
                transactionHandler.enlist(session);
            }

            MessageProducer producer = session.createProducer(respDestination);
            Message response = session.createObjectMessage((Serializable) outMessage.getBody());

            switch (correlationScheme) {
            case RequestCorrelIDToCorrelID: {
                break;
            }
            case RequestMsgIDToCorrelID: {
                response.setStringProperty("MsgIDToCorrlID", request.getJMSMessageID());
                break;
            }
            }

            producer.send(response);

            if (transactionType == TransactionType.LOCAL) {
                session.commit();
            }
        } catch (JMSException je) {
            throw new Fabric3AQException("Cannot forward Response ", je);
        } finally {
            JmsHelper.closeQuietly(connection);
        }

    }

    /**
     * Handle Inbound Message
     * 
     * @param request
     * @return Message
     * @throws JMSException
     */
    private org.fabric3.spi.wire.Message handleInboundMessage(final Message request) throws JMSException {
        final String opName = request.getStringProperty("scaOperationName");
        final Interceptor interceptor = getInterceptor(opName);

        final ObjectMessage objectMessage = (ObjectMessage) request;
        final Object[] payload = (Object[]) objectMessage.getObject();

        return invokeOnService(interceptor, payload);
    }

    /**
     * Invoke On Service
     * 
     * @param interceptor
     * @param payload
     * @return
     */
    private org.fabric3.spi.wire.Message invokeOnService(final Interceptor interceptor, final Object[] payload) {
        final org.fabric3.spi.wire.Message inMessage = new MessageImpl(payload, false, new WorkContext());
        return interceptor.invoke(inMessage);
    }

    /*
     * Finds the matching interceptor.
     */
    private Interceptor getInterceptor(String opName) {

        if (ops.size() == 1) {
            return ops.values().iterator().next().getValue().getHeadInterceptor();
        } else if (opName != null && ops.containsKey(opName)) {
            return ops.get(opName).getValue().getHeadInterceptor();
        } else if (ops.containsKey("onMessage")) {
            return ops.get("onMessage").getValue().getHeadInterceptor();
        } else {
            throw new Fabric3AQException("Unable to match operation on the service contract");
        }

    }

}
