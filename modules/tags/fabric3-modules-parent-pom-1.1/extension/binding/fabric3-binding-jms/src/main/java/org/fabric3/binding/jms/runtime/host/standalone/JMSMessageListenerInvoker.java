/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
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

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ServerSessionPool;
import javax.jms.Session;

import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.runtime.JmsBadMessageException;
import org.fabric3.binding.jms.runtime.JmsMonitor;
import org.fabric3.binding.jms.runtime.JmsServiceException;
import org.fabric3.binding.jms.runtime.ServiceMessageListener;
import org.fabric3.binding.jms.runtime.helper.JmsHelper;
import org.fabric3.binding.jms.runtime.tx.JmsTxException;
import org.fabric3.binding.jms.runtime.tx.TransactionHandler;
import org.fabric3.host.work.WorkScheduler;

/**
 * A container class used to support MessageListener with ServerSessionPool.
 */
public class JMSMessageListenerInvoker implements MessageListener {
    private Connection requestConnection;
    private Destination requestDestination;
    private Connection responseConnection;
    private Destination responseDestination;
    private ServiceMessageListener messageListener = null;
    private TransactionType transactionType;
    private TransactionHandler transactionHandler;
    private WorkScheduler workScheduler;
    private JmsMonitor monitor;

    public JMSMessageListenerInvoker(Connection requestConnection,
                                     Destination requestDestination,
                                     Connection responseConnection,
                                     Destination responseDestination,
                                     ServiceMessageListener messageListener,
                                     TransactionType transactionType,
                                     TransactionHandler transactionHandler,
                                     WorkScheduler workScheduler,
                                     JmsMonitor monitor) {
        this.requestConnection = requestConnection;
        this.requestDestination = requestDestination;
        this.responseConnection = responseConnection;
        this.responseDestination = responseDestination;
        this.messageListener = messageListener;
        this.transactionType = transactionType;
        this.transactionHandler = transactionHandler;
        this.workScheduler = workScheduler;
        this.monitor = monitor;
    }

    public void start(int receiverCount) throws JMSException {
        ServerSessionPool serverSessionPool =
                new StandaloneServerSessionPool(requestConnection, transactionHandler, this, transactionType, workScheduler, receiverCount, monitor);
        requestConnection.createConnectionConsumer(requestDestination, null, serverSessionPool, 1);
        requestConnection.start();
    }

    public void stop() {
    }

    public void onMessage(Message message) {
        Session responseSession = null;
        try {
            responseSession = responseConnection.createSession(true, Session.SESSION_TRANSACTED);
            if (transactionType == TransactionType.GLOBAL) {
                transactionHandler.enlist(responseSession);
            }
            messageListener.onMessage(message, responseSession, responseDestination);
            commit(responseSession);
        } catch (JMSException e) {
            rollback();
            monitor.jmsListenerError(e);
        } catch (RuntimeException e) {
            rollback();
            monitor.jmsListenerError(e);
            throw e;
        } catch (JmsServiceException e) {
            // FIXME
            monitor.jmsListenerError(e);
            try {
                commit(responseSession);
            } catch (JmsTxException e1) {
                monitor.jmsListenerError(e1);
            } catch (JMSException e1) {
                monitor.jmsListenerError(e1);
            }
        } catch (JmsTxException e) {
            rollback();
            monitor.jmsListenerError(e);
        } catch (JmsBadMessageException e) {
            // Bad message. Do not rollback since the message should not be redelivered. Just log the exception for now.
            monitor.jmsListenerError(e);
            try {
                commit(responseSession);
            } catch (JmsTxException e2) {
                rollback();
            } catch (JMSException e1) {
                rollback();
            }
        } finally {
            JmsHelper.closeQuietly(responseSession);
        }
    }

    private void commit(Session responseSession) throws JmsTxException, JMSException {
        if (transactionType == TransactionType.GLOBAL) {
            transactionHandler.commit();
        } else if (transactionType == TransactionType.LOCAL) {
            responseSession.commit();
        }
    }

    private void rollback() {
        try {
            if (transactionType == TransactionType.GLOBAL) {
                transactionHandler.rollback();
            }
        } catch (Exception ne) {
            //ignore
        }
    }


}
