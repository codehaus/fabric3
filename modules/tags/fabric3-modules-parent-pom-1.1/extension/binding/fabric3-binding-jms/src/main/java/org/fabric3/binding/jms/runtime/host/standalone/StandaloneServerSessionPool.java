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

import java.util.Stack;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ServerSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;

import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.runtime.JmsMonitor;
import org.fabric3.binding.jms.runtime.helper.JmsHelper;
import org.fabric3.binding.jms.runtime.tx.JmsTxException;
import org.fabric3.binding.jms.runtime.tx.TransactionHandler;
import org.fabric3.host.work.DefaultPausableWork;
import org.fabric3.host.work.WorkScheduler;

/**
 * Server session pool used by the standalone JMS server.
 *
 * @version $Revision$ $Date$
 */
public class StandaloneServerSessionPool implements ServerSessionPool {

    // Available server sessions
    private final Stack<ServerSession> serverSessions = new Stack<ServerSession>();
    private Connection connection;
    private final TransactionHandler transactionHandler;
    private final TransactionType transactionType;
    private int poolSize = 3; //default value
    private JmsMonitor monitor;
    private final WorkScheduler workScheduler;

    public StandaloneServerSessionPool(Connection connection,
                                       TransactionHandler transactionHandler,
                                       MessageListener listener,
                                       TransactionType transactionType,
                                       WorkScheduler workScheduler,
                                       int receiverCount,
                                       JmsMonitor monitor) throws JMSException {
        this.connection = connection;
        this.transactionHandler = transactionHandler;
        this.transactionType = transactionType;
        this.workScheduler = workScheduler;
        this.poolSize = receiverCount;
        this.monitor = monitor;
        initSessions(listener);
    }

    private void initSessions(MessageListener listener) throws JMSException {
        for (int i = 0; i < poolSize; i++) {
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            session.setMessageListener(listener);
            ServerSession serverSession = new StandaloneServerSession(session, this);
            serverSessions.add(serverSession);
        }
    }

    /**
     * Closes the underlying sessions.
     *
     * @throws JMSException if there is an error closing the session
     */
    public void stop() throws JMSException {
        ServerSession serverSession;
        while ((serverSession = getServerSession()) != null) {
            JmsHelper.closeQuietly(serverSession.getSession());
        }
    }

    public ServerSession getServerSession() throws JMSException {
        synchronized (serverSessions) {
            while (serverSessions.isEmpty()) {
                try {
                    serverSessions.wait();
                } catch (InterruptedException e) {
                    throw new JMSException("Unable to get a server session");
                }
            }
            return serverSessions.pop();
        }
    }

    /**
     * Returns the session to the pool.
     *
     * @param serverSession Server session to be returned.
     */
    protected void returnSession(ServerSession serverSession) {
        synchronized (serverSessions) {
            serverSessions.push(serverSession);
            serverSessions.notify();
        }
    }

    /**
     * Start a JMS Session asynchronously.
     *
     * @param serverSession the session
     */
    public void startServerSession(final ServerSession serverSession) {
        workScheduler.scheduleWork(new DefaultPausableWork() {
            public void execute() {
                try {
                    Session session = serverSession.getSession();
                    if (transactionType == TransactionType.GLOBAL) {
                        transactionHandler.enlist(session);
                    }
                    session.run();
                    if (transactionType == TransactionType.LOCAL) {
                        session.commit();
                    }
                } catch (JMSException e) {
                    monitor.jmsListenerError(e);
                } catch (JmsTxException e) {
                    monitor.jmsListenerError(e);
                } finally {
                    returnSession(serverSession);
                }
            }
        });

    }

}
