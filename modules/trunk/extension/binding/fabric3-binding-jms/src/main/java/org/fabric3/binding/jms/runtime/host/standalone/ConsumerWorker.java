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

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.runtime.JmsBadMessageException;
import org.fabric3.binding.jms.runtime.JmsMonitor;
import org.fabric3.binding.jms.runtime.JmsServiceException;
import org.fabric3.binding.jms.runtime.ServiceMessageListener;
import org.fabric3.binding.jms.runtime.helper.JmsHelper;
import org.fabric3.binding.jms.runtime.tx.JmsTxException;
import org.fabric3.binding.jms.runtime.tx.TransactionHandler;
import org.fabric3.host.work.DefaultPausableWork;

/**
 * A thread pull message from destination and invoke Message listener.
 *
 * @version $Revision$ $Date$
 */
public class ConsumerWorker extends DefaultPausableWork {

    private Session session;
    private TransactionHandler transactionHandler;
    private MessageConsumer consumer;
    private ServiceMessageListener listener;
    private long readTimeout;
    private TransactionType transactionType;
    private ClassLoader cl;
    private JmsMonitor monitor;
    private Connection responseConnection;
    private Destination responseDestination;

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
        session = template.getRequestConnection().createSession(true, Session.SESSION_TRANSACTED);
        consumer = session.createConsumer(template.getRequestDestination());
        responseConnection = template.getResponseConnection();
        responseDestination = template.getResponseDestination();
        readTimeout = template.getReadTimeout();
        cl = template.getClassloader();
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
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            Message message = consumer.receive(readTimeout);
            // set the TCCL to the target service classloader
            Thread.currentThread().setContextClassLoader(cl);
            if (message != null) {
                if (responseDestination != null) {
                    // invocation is request-response, resolve the response destination
                    responseSession = responseConnection.createSession(true, Session.SESSION_TRANSACTED);
                    if (transactionType == TransactionType.GLOBAL) {
                        transactionHandler.enlist(responseSession);
                    }
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
            JmsHelper.closeQuietly(responseSession);
            Thread.currentThread().setContextClassLoader(oldCl);
        }

    }

    public final void stop() {
        super.stop();
        JmsHelper.closeQuietly(session);
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
