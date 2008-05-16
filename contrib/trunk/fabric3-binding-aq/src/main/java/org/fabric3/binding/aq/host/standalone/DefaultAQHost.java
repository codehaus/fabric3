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

import java.util.LinkedList;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.XAConnection;
import javax.jms.XAQueueConnectionFactory;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.aq.Fabric3AQException;
import org.fabric3.binding.aq.helper.JmsHelper;
import org.fabric3.binding.aq.host.AQHost;
import org.fabric3.binding.aq.host.PollingConsumer;
import org.fabric3.binding.aq.monitor.AQMonitor;
import org.fabric3.binding.aq.tx.TransactionHandler;
import org.fabric3.spi.services.work.WorkScheduler;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

/**
 * Handler for AQ
 * 
 * @version $Revsion$ $Date$
 */
public class DefaultAQHost implements AQHost, DefaultAQHostMBean {

    private final List<PollingConsumer> consumers;
    private final WorkData workData;
    private WorkScheduler workScheduler;
    private XAConnection connection;
    private AQMonitor monitor;
    private int receiverCount = 1;
    private long readTimeout = 5000L;

    /**
     * Constructor
     */
    public DefaultAQHost() {
        consumers = new LinkedList<PollingConsumer>();
        workData = new WorkData();
    }

    /**
     * Registers the listeners to start on consuming messages
     */
    public void registerListener(final XAQueueConnectionFactory connectionFactory, final Destination destination, final MessageListener listener, final TransactionHandler transactionHandler, final ClassLoader classLoader) {
        workData.setConnectionFactory(connectionFactory);
        workData.setDestination(destination);
        workData.setListener(listener);
        workData.setClassLoader(classLoader);
        workData.setTxHandler(transactionHandler);
        try {
            prepareWorkSchedule();
        } catch (JMSException ex) {
            throw new Fabric3AQException("Unable to Start serviceing Requests", ex);
        }
    }

    /**
     * Stops the receiver threads.
     * 
     * @throws JMSException
     */
    @Destroy
    public void stop() throws JMSException {
        stopConsumers();
        JmsHelper.closeQuietly(connection);
        monitor.stopOnAQHost(" Stopped ");
    }

    /**
     * Sets The Number Of receivers
     * @return count
     */
    public void setReceivers(final String serviceUri, final int receivers) {        
        receiverCount = receivers;
        try {
            stop();
            consumers.clear();
            prepareWorkSchedule();
        } catch (JMSException je) {
           monitor.onException(je);
           throw new Fabric3AQException("Unable to Start serviceing Requests from Managment Console", je);
        }
    }

    /**
     * Gets the RecieverCount
     * @return count
     */
    public int getReceiverCount() {
        return receiverCount;
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
    protected void setMonitor(AQMonitor monitor) {
        this.monitor = monitor;
    }

    /*
     * Prepares the work schedule
     */
    private void prepareWorkSchedule() throws JMSException {
        connection = workData.getConnectionFactory().createXAConnection();
        for (int i = 0; i < receiverCount; i++) {
            startConsumption();
        }
        connection.start();
    }

    /*
     * Start the Consumers to process Messages
     */
    private void startConsumption() throws JMSException {        
        final PollingConsumer pollingConsumer = new ConsumerWorker(connection, workData.getDestination(), workData.getListener(), 
                                                                   workData.getTxHandler(), readTimeout, workData.getClassLoader(), monitor);
        workScheduler.scheduleWork(pollingConsumer);
        consumers.add(pollingConsumer);
    }

    /*
     * Stops Consumption Of messages
     */
    private void stopConsumers() {
        int i = 0;
        for (PollingConsumer consumer : consumers) {
            monitor.stopConsumer(++i + " Stopping ");
            consumer.stopConsumption();
        }
    }
}
