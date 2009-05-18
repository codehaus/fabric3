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
package org.fabric3.binding.jms.runtime.host.standalone;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.runtime.JmsFactory;
import org.fabric3.binding.jms.runtime.JMSRuntimeMonitor;
import org.fabric3.binding.jms.runtime.JmsBadMessageException;
import org.fabric3.binding.jms.runtime.JmsServiceException;
import org.fabric3.binding.jms.runtime.ServiceMessageListener;
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
    private final ServiceMessageListener listener;
    private final long readTimeout;
    private final TransactionType transactionType;
    private final ClassLoader cl;
    private final JmsFactory responseJmsFactory;
    private JMSRuntimeMonitor monitor;

    /**
     * Constructor.
     *
     * @param template the ConsumerWorkerTemplate  to use with this worker.
     * @throws JMSException if an error occurs initializing JMS objects required by the worker
     */
    public ConsumerWorker(ConsumerWorkerTemplate template) throws JMSException {
        super(true);
        transactionHandler = template.getTransactionHandler();
        transactionType = template.getTransactionType();
        listener = template.getListener();
        responseJmsFactory = template.getResponseJMSObjectFactory();
        JmsFactory requestJmsFactory = template.getRequestJMSObjectFactory();
        session = requestJmsFactory.createTransactedSession();
        consumer = session.createConsumer(requestJmsFactory.getDestination());
        readTimeout = template.getReadTimeout();
        cl = template.getCl();
        monitor = template.getMonitor();
    }

    public void execute() {
        try {
            if (transactionType == TransactionType.GLOBAL) {
                // enlist the session in global transaction
                transactionHandler.enlist(session);
            }
        } catch (JmsTxException e) {
            monitor.jmsListenerError(e);
            return;
        }

        Session responseSession = null;
        Destination responseDestination = null;
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            Message message = consumer.receive(readTimeout);
            // set the TCCL to the target service classloader
            Thread.currentThread().setContextClassLoader(cl);
            if (message != null) {
                if (responseJmsFactory != null) {
                    // invocation is request-response, resolve the response destination
                    responseSession = responseJmsFactory.createTransactedSession();
                    if (transactionType == TransactionType.GLOBAL) {
                        transactionHandler.enlist(responseSession);
                    }
                    responseDestination = responseJmsFactory.getDestination();
                }
                // dispatch the message
                listener.onMessage(message, responseSession, responseDestination);
                commit();
            }
        } catch (JmsServiceException e) {
            // exception was thrown by the service, log the root cause but do not roll back
            monitor.jmsListenerError(e.getCause());
            commit();
        } catch (JmsBadMessageException e) {
            // Bad message. Do not rollback since the message should not be redelivered. Just log the exception for now.
            monitor.jmsListenerError(e);
            commit();
        } catch (JmsTxException e) {
            // error performing a transaction operation.
            monitor.jmsListenerError(e);
            rollback();
        } catch (JMSException ex) {
            // error raised by the JMS provider
            monitor.jmsListenerError(ex);
            rollback();
        } catch (RuntimeException e) {
            // an unexpected error thrown during message dispatch
            monitor.jmsListenerError(e);
            rollback();
            throw e;
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }

    }

    private void rollback() {
        if (transactionType == TransactionType.GLOBAL) {
            try {
                transactionHandler.rollback();
            } catch (JmsTxException e) {
                monitor.jmsListenerError(e);
            }
        } else {
            // local transaction
            try {
                session.rollback();
            } catch (JMSException e) {
                monitor.jmsListenerError(e);
            }
        }
    }

    private void commit() {
        if (transactionType == TransactionType.GLOBAL) {
            try {
                transactionHandler.commit();
            } catch (JmsTxException e) {
                try {
                    transactionHandler.rollback();
                } catch (JmsTxException e1) {
                    monitor.jmsListenerError(e);
                }
            }
        } else {
            // local transaction
            try {
                session.commit();
            } catch (JMSException e) {
                try {
                    session.rollback();
                } catch (JMSException e1) {
                    monitor.jmsListenerError(e);
                }
            }
        }
    }


}
