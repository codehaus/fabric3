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
package org.fabric3.binding.jms.runtime.host.standalone;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.fabric3.binding.jms.common.Fabric3JmsException;
import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.runtime.JMSObjectFactory;
import org.fabric3.binding.jms.runtime.JMSRuntimeMonitor;
import org.fabric3.binding.jms.runtime.ResponseMessageListener;
import org.fabric3.binding.jms.runtime.tx.JmsTxException;
import org.fabric3.binding.jms.runtime.tx.TransactionHandler;
import org.fabric3.host.work.DefaultPausableWork;

/**
 * A thread pull message from destination and invoke Message listener.
 * 
 * @version $Revision$ $Date$
 */
public class ConsumerWorker extends DefaultPausableWork {

    private final Session session;
    private final TransactionHandler transactionHandler;
    private final MessageConsumer consumer;
    private final ResponseMessageListener listener;
    private final long readTimeout;
    private final TransactionType transactionType;
    private final ClassLoader cl;
    private final JMSObjectFactory responseJMSObjectFactory;
    private final JMSObjectFactory requestJMSObjectFactory;
    private JMSRuntimeMonitor monitor;

    /**
     * @param session Session used to receive messages.
     * @param transactionHandler Transaction handler.
     * @param consumer Message consumer.
     * @param listener Delegate message listener.
     * @param readTimeout Read timeout.
     */
    public ConsumerWorker(ConsumerWorkerTemplate template) {

        super(true);

        try {

            transactionHandler = template.getTransactionHandler();
            transactionType = template.getTransactionType();
            listener = template.getListener();
            responseJMSObjectFactory = template.getResponseJMSObjectFactory();
            requestJMSObjectFactory = template.getRequestJMSObjectFactory();
            session = requestJMSObjectFactory.createSession();
            consumer = session.createConsumer(requestJMSObjectFactory.getDestination());
            readTimeout = template.getReadTimeout();
            cl = template.getCl();
            monitor = template.getMonitor();

        } catch (JMSException e) {
            throw new Fabric3JmsException("Unale to create consumer", e);
        }

    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void execute() {
        Session responseSession = null;
        Destination responseDestination = null;

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {

            Thread.currentThread().setContextClassLoader(cl);
            if (transactionType == TransactionType.GLOBAL) {
                transactionHandler.enlist(session);
            }
            Message message = consumer.receive(readTimeout);
            try {
                if (message != null) {
                    if (responseJMSObjectFactory != null) {
                        responseSession = responseJMSObjectFactory.createSession();
                        if (transactionType == TransactionType.GLOBAL) {
                            transactionHandler.enlist(responseSession);
                        }
                        responseDestination = responseJMSObjectFactory.getDestination();
                    }
                    listener.onMessage(message, responseSession, responseDestination);
                    if (transactionType == TransactionType.GLOBAL) {
                        transactionHandler.commit();
                        transactionHandler.enlist(session);
                    } else {
                        session.commit();
                    }
                }
            } catch (JmsTxException e) {
                if (transactionType == TransactionType.GLOBAL) {
                    transactionHandler.rollback();
                } else {
                    try {
                        session.rollback();
                    } catch (JMSException ne) {
                        reportException(ne);
                    }
                    reportException(e);
                }
            }
        } catch (JMSException ex) {
            if (transactionType == TransactionType.GLOBAL) {
                transactionHandler.rollback();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }

    }

    /**
     * Report an exception.
     */
    private void reportException(Exception e) {
        if (monitor != null) {
            monitor.jmsListenerError(e);
        }
    }

}
