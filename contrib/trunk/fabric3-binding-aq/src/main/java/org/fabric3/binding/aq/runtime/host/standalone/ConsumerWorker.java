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
package org.fabric3.binding.aq.runtime.host.standalone;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.XAConnection;

import org.fabric3.binding.aq.runtime.helper.JmsHelper;
import org.fabric3.binding.aq.runtime.host.PollingConsumer;
import org.fabric3.binding.aq.runtime.monitor.AQMonitor;
import org.fabric3.binding.aq.runtime.tx.TransactionHandler;

/**
 * @version $Revision$ $Date$
 */
public class ConsumerWorker implements PollingConsumer {

    private final Session session;
    private final TransactionHandler transactionHandler;
    private final MessageConsumer consumer;
    private final MessageListener listener;
    private final long readTimeout; 
    private final ClassLoader cl;
    private final AtomicBoolean consume;
    private final AQMonitor monitor;

    /**
     * @param session Session used to receive messages.
     * @param transactionHandler Transaction handler.
     * @param consumer Message consumer.
     * @param listener Delegate message listener.
     * @param readTimeout Read timeout.
     * @throws JMSException 
     */
    public ConsumerWorker(XAConnection connection, Destination destination, final MessageListener listener, final TransactionHandler transactionHandler, final long readTimeout, final ClassLoader cl, final AQMonitor monitor) throws JMSException {
        this.session = connection.createXASession();        
        this.transactionHandler = transactionHandler; 
        this.consumer = session.createConsumer(destination);
        this.listener = listener;
        this.readTimeout = readTimeout;        
        this.cl = cl;
        this.monitor = monitor;
        consume = new AtomicBoolean(true);
    }

    /**
     * Run's the unit of work
     */
    public void run() {
        final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        assignClassLoader(cl);        
        try {                                    
            while (consume.get()) {
                transactionHandler.enlist(session);
                consumeMessage();
                transactionHandler.commit();
            }
        } catch (JMSException ex) {
            monitor.onException(ex);
            transactionHandler.rollback();            
        } finally {
            assignClassLoader(oldClassLoader);
        }
    }
    
    /**
     * Stop Consuming Data
     * @throws JMSException 
     */
    public void stopConsumption()  {
        consume.set(false);        
        JmsHelper.closeQuietly(consumer);        
        JmsHelper.closeQuietly(session);               
    }

    /*
     * Consumes the Message
     */
    private void consumeMessage() throws JMSException {
        final Message message = consumer.receive(readTimeout);
        if (message != null) {            
            listener.onMessage(message);
        }
    }

    /*
     * Restores the Classloader
     */
    private void assignClassLoader(final ClassLoader classLoader) {
        Thread.currentThread().setContextClassLoader(classLoader);
    }    
}
