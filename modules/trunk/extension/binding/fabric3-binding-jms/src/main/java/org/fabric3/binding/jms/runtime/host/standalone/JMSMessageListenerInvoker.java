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

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ServerSessionPool;
import javax.jms.Session;

import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.runtime.Fabric3JmsException;
import org.fabric3.binding.jms.runtime.JMSObjectFactory;
import org.fabric3.binding.jms.runtime.JMSRuntimeMonitor;
import org.fabric3.binding.jms.runtime.JmsOperationException;
import org.fabric3.binding.jms.runtime.SourceMessageListener;
import org.fabric3.binding.jms.runtime.tx.JmsTxException;
import org.fabric3.binding.jms.runtime.tx.TransactionHandler;
import org.fabric3.host.work.WorkScheduler;

/**
 * A container class used to support MessageListener with ServerSessionPool.
 */
public class JMSMessageListenerInvoker implements MessageListener {
    private JMSObjectFactory requestJMSObjectFactory = null;
    private JMSObjectFactory responseJMSObjectFactory;
    private SourceMessageListener messageListener = null;
    private TransactionType transactionType;
    private TransactionHandler transactionHandler;
    private WorkScheduler workScheduler;
    private JMSRuntimeMonitor monitor;

    public JMSMessageListenerInvoker(JMSObjectFactory requestJMSObjectFactory,
                                     JMSObjectFactory responseJMSObjectFactory,
                                     SourceMessageListener messageListener,
                                     TransactionType transactionType,
                                     TransactionHandler transactionHandler,
                                     WorkScheduler workScheduler,
                                     JMSRuntimeMonitor monitor) {
        this.requestJMSObjectFactory = requestJMSObjectFactory;
        this.responseJMSObjectFactory = responseJMSObjectFactory;
        this.messageListener = messageListener;
        this.transactionType = transactionType;
        this.transactionHandler = transactionHandler;
        this.workScheduler = workScheduler;
        this.monitor = monitor;
    }

    public void start(int receiverCount) {
        ServerSessionPool serverSessionPool = createServerSessionPool(receiverCount);
        try {
            Connection connection = requestJMSObjectFactory.getConnection();
            connection.createConnectionConsumer(requestJMSObjectFactory
                    .getDestination(), null, serverSessionPool, 1);
            connection.start();
        } catch (JMSException e) {
            throw new Fabric3JmsException("Error when register Listener", e);

        }
    }

    private StandaloneServerSessionPool createServerSessionPool(int receiverCount) {
        return new StandaloneServerSessionPool(requestJMSObjectFactory,
                                               transactionHandler, this, transactionType, workScheduler, receiverCount);
    }

    public void stop() {
        requestJMSObjectFactory.close();
        responseJMSObjectFactory.close();
    }

    public void onMessage(Message message) {
        try {
            Session responseSession = responseJMSObjectFactory.createSession();
            if (transactionType == TransactionType.GLOBAL) {
                transactionHandler.enlist(responseSession);
            }
            Destination responseDestination = responseJMSObjectFactory
                    .getDestination();
            messageListener.onMessage(message, responseSession, responseDestination);
            if (transactionType == TransactionType.GLOBAL) {
                transactionHandler.commit();
            } else if (transactionType == TransactionType.LOCAL) {
                responseSession.commit();
            }
            responseJMSObjectFactory.recycle();
        } catch (JMSException e) {
            throw new Fabric3JmsException("Error when invoking Listener", e);
        } catch (RuntimeException e) {
            try {
                if (transactionType == TransactionType.GLOBAL) {
                    transactionHandler.rollback();
                }
            } catch (Exception ne) {
                //ignore
            }
            monitor.jmsListenerError(e);
            throw e;
        } catch (JmsOperationException e) {
            throw new Fabric3JmsException("Error when invoking Listener", e.getCause());
        } catch (JmsTxException e) {
            try {
                if (transactionType == TransactionType.GLOBAL) {
                    transactionHandler.rollback();
                }
            } catch (Exception ne) {
                //ignore
            }
            monitor.jmsListenerError(e);
        }
    }


}
