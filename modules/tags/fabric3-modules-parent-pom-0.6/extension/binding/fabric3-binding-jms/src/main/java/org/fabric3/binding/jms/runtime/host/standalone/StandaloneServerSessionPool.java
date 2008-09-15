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

import java.util.Stack;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ServerSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;

import org.fabric3.binding.jms.common.Fabric3JmsException;
import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.runtime.JMSObjectFactory;
import org.fabric3.binding.jms.runtime.helper.JmsHelper;
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
    private Stack<ServerSession> serverSessions = new Stack<ServerSession>();
    private final JMSObjectFactory jmsObjectFactory;
    private final TransactionHandler transactionHandler;
    private final TransactionType transactionType;
    private int poolSize = 3; //default value
    private final WorkScheduler workScheduler;

    /**
     * Initializes the server sessions.
     * @param workScheduler
     * @param serverSessions Server sessions.
     */
    public StandaloneServerSessionPool(JMSObjectFactory jmsObjectFactory,
            TransactionHandler transactionHandler,
            MessageListener listener,
            TransactionType transactionType,
            WorkScheduler workScheduler,
            int receiverCount) {
        this.jmsObjectFactory = jmsObjectFactory;
        this.transactionHandler = transactionHandler;
        this.transactionType = transactionType;
        this.workScheduler = workScheduler;
        this.poolSize =receiverCount;
        initSessions(listener);
    }

    private void initSessions(MessageListener listener) throws Fabric3JmsException {
        for (int i = 0; i < poolSize; i++) {
            try {
                Session session = jmsObjectFactory.createSession();
                session.setMessageListener(listener);
                ServerSession serverSession = new StandaloneServerSession(session, this);
                serverSessions.add(serverSession);
            } catch (JMSException e) {
                throw new Fabric3JmsException("Error when initialize ServerSessionPool",e);
            }
        }
    }

    /**
     * Closes the underlying sessions.
     */
    public void stop() throws JMSException {
        ServerSession serverSession = null;
        while ((serverSession = getServerSession()) != null) {
            JmsHelper.closeQuietly(serverSession.getSession());
        }
    }

    /**
     * @see javax.jms.ServerSessionPool#getServerSession()
     */
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
     * @param serverSessions
     */
    public void StartServerSession(final ServerSession serverSession){
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
                } catch (Exception jmse) {
                    throw new Fabric3JmsException("Error when start ServerSession",jmse);
                } finally {
                    returnSession(serverSession);
                }
            }
        });

    }

}
