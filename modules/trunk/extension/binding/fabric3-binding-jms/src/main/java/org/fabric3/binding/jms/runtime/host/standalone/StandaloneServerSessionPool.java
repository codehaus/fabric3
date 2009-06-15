/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
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
 * @version $Rev$ $Date$
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
