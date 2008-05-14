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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.XAConnection;
import javax.jms.XAQueueConnectionFactory;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.aq.Fabric3AQException;
import org.fabric3.binding.aq.helper.JmsHelper;
import org.fabric3.binding.aq.host.AQHost;
import org.fabric3.binding.aq.monitor.AQMonitor;
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

    private WorkScheduler workScheduler;
    private PollingConsumer pollingConsumer; 
    private XAConnection connection;
    private AQMonitor monitor;    
    private int receiverCount = 5;
    private long readTimeout = 1000L;
       

    /**
     * Registers the listeners to start on consuming messages     
     */
    public void registerListener( final XAQueueConnectionFactory connectionFactory, final Destination destination, final MessageListener listener, final TransactionHandler transactionHandler, final ClassLoader cl) {
        try {
            connection = connectionFactory.createXAConnection();
            for (int i = 0; i < receiverCount; i++) {
                final Session session = connection.createXASession();
                final MessageConsumer consumer = session.createConsumer(destination);
                pollingConsumer = new ConsumerWorker(session, consumer, listener, transactionHandler, readTimeout, cl);
                workScheduler.scheduleWork(pollingConsumer);
            }
            connection.start();
        } catch (JMSException ex) {
            throw new Fabric3AQException("Unable to activate service", ex);
        }
    }
    
    /**
     * Stops the receiver threads.
     * @throws JMSException
     */
    @Destroy
    public void stop() throws JMSException {
        monitor.stopOnAQHost(" Stoping AQ");       
        pollingConsumer.stopConsumption();
        JmsHelper.closeQuietly(connection);        
        monitor.stopOnAQHost(" Stopped ");
    }
    
    /**
     * Injects the work scheduler.
     * @param workScheduler Work scheduler to be used.
     */
    @Reference
    protected void setWorkScheduler(final WorkScheduler workScheduler) {
        this.workScheduler = workScheduler;
    }

    /**
     * Sets the Receiver Count
     * @param receiver count
     */
    @Property
    protected void setReceiverCount(final int recieverCount) {
        this.readTimeout = recieverCount;
    }

    /**
     * Sets the read timeout.
     * @param readTimeout Read timeout for blocking receive.
     */
    @Property
    protected void setReadTimeout(final long readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    /**
     * Sets the Monitor
     * @param monitor
     */
    @Monitor
    protected void setMonitor(AQMonitor monitor){
        this.monitor = monitor;
    }
}
