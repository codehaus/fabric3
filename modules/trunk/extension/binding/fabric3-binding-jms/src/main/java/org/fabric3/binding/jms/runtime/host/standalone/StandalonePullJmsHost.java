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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.jms.common.Fabric3JmsException;
import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.runtime.JMSObjectFactory;
import org.fabric3.binding.jms.runtime.JMSRuntimeMonitor;
import org.fabric3.binding.jms.runtime.ResponseMessageListener;
import org.fabric3.binding.jms.runtime.helper.JmsHelper;
import org.fabric3.binding.jms.runtime.host.JmsHost;
import org.fabric3.binding.jms.runtime.tx.TransactionHandler;
import org.fabric3.spi.services.work.WorkScheduler;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

/**
 * Service handler for JMS.
 *
 * @version $Revsion$ $Date: 2007-05-22 00:19:04 +0100 (Tue, 22 May 2007) $
 */
public class StandalonePullJmsHost implements JmsHost, StandalonePullJmsHostMBean {

    private WorkScheduler workScheduler;
    private long readTimeout = 1000L;
    private JMSRuntimeMonitor monitor;
    private int receiverCount = 3;
    private Map<String, List<ConsumerWorker>> consumerWorkerMap = new HashMap<String, List<ConsumerWorker>>();
    private Map<String, Connection> connectionMap = new HashMap<String, Connection>();
    private Map<String, ConsumerWorkerTemplate> templateMap = new HashMap<String, ConsumerWorkerTemplate>();

    /**
     * Injects the monitor.
     * @param monitor Monitor used for logging.
     */
    public StandalonePullJmsHost(@Monitor JMSRuntimeMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * Configurable property for default receiver count.
     * @param receiverCount Default receiver count.
     */
    @Property
    public void setReceiverCount(int receiverCount) {
        this.receiverCount = receiverCount;
    }

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
     *
     * @throws JMSException
     */
    @Destroy
    public void stop() throws JMSException {
        
        for (List<ConsumerWorker> consumerWorkers : consumerWorkerMap.values()) {
            for (ConsumerWorker worker : consumerWorkers) {
                worker.inactivate();
            }
        }
        for (Connection connection : connectionMap.values()) {
            JmsHelper.closeQuietly(connection);
        }
        monitor.jmsRuntimeStop();
        
    }

    public void registerResponseListener(JMSObjectFactory requestJMSObjectFactory,
                                         JMSObjectFactory responseJMSObjectFactory,
                                         ResponseMessageListener messageListener,
                                         TransactionType transactionType,
                                         TransactionHandler transactionHandler,
                                         ClassLoader cl) {
        
        Destination requestDestination = responseJMSObjectFactory.getDestination();
        
        try {
            
            Connection connection = requestJMSObjectFactory.getConnection();
            List<ConsumerWorker> consumerWorkers = new ArrayList<ConsumerWorker>();
            
            ConsumerWorkerTemplate template = new ConsumerWorkerTemplate(transactionHandler,
                                                                         transactionType,
                                                                         messageListener,
                                                                         responseJMSObjectFactory,
                                                                         requestJMSObjectFactory,
                                                                         readTimeout,
                                                                         cl,
                                                                         monitor);
            templateMap.put(requestDestination.toString(), template);
            
            for (int i = 0; i < receiverCount; i++) {
                ConsumerWorker work = new ConsumerWorker(template);
                workScheduler.scheduleWork(work);
                consumerWorkers.add(work);
            }
            
            connection.start();
            connectionMap.put(requestDestination.toString(), connection);
            consumerWorkerMap.put(requestDestination.toString(), consumerWorkers);
            
        } catch (JMSException ex) {
            throw new Fabric3JmsException("Unable to activate service", ex);
        }
        
        monitor.registerListener(requestJMSObjectFactory.getDestination());
        
    }

    public int getReceiverCount(String destination) {
        
        for (Map.Entry<String, List<ConsumerWorker>> entry : consumerWorkerMap.entrySet()) {
            if (destination.equals(entry.getKey())) {
                return entry.getValue().size();
            }
        }
        throw new IllegalArgumentException("Unknown receiver:" + destination);
        
    }

    public void setReceiverCount(String destination, int receiverCount) {
        
        for (Map.Entry<String, List<ConsumerWorker>> entry : consumerWorkerMap.entrySet()) {
            if (destination.equals(entry.getKey().toString())) {
                if (receiverCount != entry.getValue().size()) { 
                    ConsumerWorkerTemplate template = templateMap.get(destination);
                    for (ConsumerWorker consumerWorker : entry.getValue()) {
                        consumerWorker.inactivate();
                    }
                    entry.getValue().clear();
                    for (int i = 0;i < receiverCount;i++) {
                        ConsumerWorker consumerWorker = new ConsumerWorker(template);
                        workScheduler.scheduleWork(consumerWorker);
                        entry.getValue().add(consumerWorker);
                    }
                }
                return;
            }
        }
        
        throw new IllegalArgumentException("Unknown receiver:" + destination);
        
    }

    public List<String> getReceivers() {
        
        List<String> receivers = new ArrayList<String>();
        for (String destination : connectionMap.keySet()) {
            receivers.add(destination);
        }
        return receivers;
        
    }

}
