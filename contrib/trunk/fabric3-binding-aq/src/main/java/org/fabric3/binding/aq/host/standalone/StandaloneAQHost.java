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
package org.fabric3.binding.aq.host.standalone;

import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.fabric3.binding.aq.Fabric3AQException;
import org.fabric3.binding.aq.TransactionType;
import org.fabric3.binding.aq.helper.JmsHelper;
import org.fabric3.binding.aq.host.AQHost;
import org.fabric3.binding.aq.tx.TransactionHandler;
import org.fabric3.spi.services.work.WorkScheduler;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

/**
 * Service handler for JMS.
 * 
 * @version $Revsion$ $Date$
 */
public class StandaloneAQHost implements AQHost {
    
    /**
     * Work scheduler.
     */
    private WorkScheduler workScheduler;

    /**
     * Receiver connection.
     */
    private Connection connection;
    
    /**
     * Read timeout.
     */
    private long readTimeout = 1000L;
    
    /**
     * Sets the read timeout.
     * @param readTimeout Read timeout for blocking receive.
     */
    @Property
    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    /**
     * Injects the work scheduler.
     * @param workScheduler Work scheduler to be used.
     */
    @Reference
    public void setWorkScheduler(WorkScheduler workScheduler) {
        this.workScheduler = workScheduler;
    }

    /**
     * Stops the receiver threads.
     * @throws JMSException 
     */
    @Destroy
    public void stop() throws JMSException {
        JmsHelper.closeQuietly(connection);
    }

    /**
     * @see org.fabric3.binding.aq.host.AQHost#registerListener(javax.jms.Destination, 
     *                                                          javax.jms.ConnectionFactory, 
     *                                                          java.util.List, 
     *                                                          org.fabric3.binding.aq.TransactionType, 
     *                                                          org.fabric3.binding.aq.tx.TransactionHandler)
     */
    public void registerListener(final Destination destination, 
                                 final ConnectionFactory connectionFactory, 
                                 final List<MessageListener> listeners, 
                                 final TransactionType transactionType,
                                 final TransactionHandler transactionHandler,
                                 final ClassLoader cl) {
        
        try {

            connection = connectionFactory.createConnection();
            for (final MessageListener listener : listeners) {
                final Session session = connection.createSession(transactionType == TransactionType.LOCAL, Session.SESSION_TRANSACTED);
                final MessageConsumer consumer = session.createConsumer(destination);
                Runnable work = new ConsumerWorker(session, transactionHandler, transactionType, consumer ,listener, readTimeout, cl);
                workScheduler.scheduleWork(work);
            }
            
            connection.start();
            
        } catch (JMSException ex) {
            throw new Fabric3AQException("Unable to activate service", ex);
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
package org.fabric3.binding.aq.host.standalone;

import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.fabric3.binding.aq.Fabric3AQException;
import org.fabric3.binding.aq.TransactionType;
import org.fabric3.binding.aq.helper.JmsHelper;
import org.fabric3.binding.aq.host.AQHost;
import org.fabric3.binding.aq.tx.TransactionHandler;
import org.fabric3.spi.services.work.WorkScheduler;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

/**
 * Service handler for JMS.
 * 
 * @version $Revsion$ $Date$
 */
public class StandaloneAQHost implements AQHost {
    
    /**
     * Work scheduler.
     */
    private WorkScheduler workScheduler;

    /**
     * Receiver connection.
     */
    private Connection connection;
    
    /**
     * Read timeout.
     */
    private long readTimeout = 1000L;
    
    /**
     * Sets the read timeout.
     * @param readTimeout Read timeout for blocking receive.
     */
    @Property
    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    /**
     * Injects the work scheduler.
     * @param workScheduler Work scheduler to be used.
     */
    @Reference
    public void setWorkScheduler(WorkScheduler workScheduler) {
        this.workScheduler = workScheduler;
    }

    /**
     * Stops the receiver threads.
     * @throws JMSException 
     */
    @Destroy
    public void stop() throws JMSException {
        JmsHelper.closeQuietly(connection);
    }

    /**
     * @see org.fabric3.binding.aq.host.AQHost#registerListener(javax.jms.Destination, 
     *                                                          javax.jms.ConnectionFactory, 
     *                                                          java.util.List, 
     *                                                          org.fabric3.binding.aq.TransactionType, 
     *                                                          org.fabric3.binding.aq.tx.TransactionHandler)
     */
    public void registerListener(final Destination destination, 
                                 final ConnectionFactory connectionFactory, 
                                 final List<MessageListener> listeners, 
                                 final TransactionType transactionType,
                                 final TransactionHandler transactionHandler,
                                 final ClassLoader cl) {
        
        try {

            connection = connectionFactory.createConnection();
            for (final MessageListener listener : listeners) {
                final Session session = connection.createSession(transactionType == TransactionType.LOCAL, Session.SESSION_TRANSACTED);
                final MessageConsumer consumer = session.createConsumer(destination);
                Runnable work = new ConsumerWorker(session, transactionHandler, transactionType, consumer ,listener, readTimeout, cl);
                workScheduler.scheduleWork(work);
            }
            
            connection.start();
            
        } catch (JMSException ex) {
            throw new Fabric3AQException("Unable to activate service", ex);
        }       
    }
     
}
