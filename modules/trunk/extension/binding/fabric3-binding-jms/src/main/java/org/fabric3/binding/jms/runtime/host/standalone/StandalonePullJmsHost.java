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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jms.Connection;
import javax.jms.JMSException;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.runtime.JMSRuntimeMonitor;
import org.fabric3.binding.jms.runtime.JmsFactory;
import org.fabric3.binding.jms.runtime.JmsHost;
import org.fabric3.binding.jms.runtime.JmsHostException;
import org.fabric3.binding.jms.runtime.ServiceMessageListener;
import org.fabric3.binding.jms.runtime.tx.TransactionHandler;
import org.fabric3.host.work.WorkScheduler;

/**
 * A JmsHost implementation that initializes one or more message consumers per service that receive and dispatch messages in separate threads. The
 * message consumers are registered with the kernel WorkScheduler.
 *
 * @version $Revsion$ $Date: 2007-05-22 00:19:04 +0100 (Tue, 22 May 2007) $
 */
public class StandalonePullJmsHost implements JmsHost, StandalonePullJmsHostMBean {
    private WorkScheduler workScheduler;
    private long readTimeout = 30000L;
    private JMSRuntimeMonitor monitor;
    private int receiverCount = 3;
    private Map<URI, List<ConsumerWorker>> consumerWorkerMap = new HashMap<URI, List<ConsumerWorker>>();
    private Map<URI, Connection> connectionMap = new HashMap<URI, Connection>();
    private Map<URI, ConsumerWorkerTemplate> templateMap = new HashMap<URI, ConsumerWorkerTemplate>();

    /**
     * Constructor.
     *
     * @param workScheduler the work scheduler
     * @param monitor       Monitor used for logging.
     */
    public StandalonePullJmsHost(@Reference WorkScheduler workScheduler, @Monitor JMSRuntimeMonitor monitor) {
        this.workScheduler = workScheduler;
        this.monitor = monitor;
    }

    /**
     * Configurable property for default receiver count.
     *
     * @param receiverCount Default receiver count.
     */
    @Property
    public void setReceiverCount(int receiverCount) {
        this.receiverCount = receiverCount;
    }

    /**
     * Sets the read timeout.
     *
     * @param readTimeout Read timeout for blocking receive.
     */
    @Property
    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * Initializes the host.
     */
    @Init
    public void init() {
        monitor.start();
    }

    /**
     * Stops the receiver threads.
     *
     * @throws JMSException if an error stopping the ConsumerWorkers is raised
     */
    @Destroy
    public void stop() throws JMSException {

        for (List<ConsumerWorker> consumerWorkers : consumerWorkerMap.values()) {
            for (ConsumerWorker worker : consumerWorkers) {
                worker.stop();
            }
        }
        monitor.stop();

    }

    public boolean isRegistered(URI serviceUri) {
        return consumerWorkerMap.containsKey(serviceUri);
    }

    public void unregisterListener(URI serviceUri) {
        List<ConsumerWorker> workers = consumerWorkerMap.remove(serviceUri);
        for (ConsumerWorker consumerWorker : workers) {
            consumerWorker.stop();
        }
        templateMap.remove(serviceUri);
        monitor.unRegisterListener(serviceUri);
    }

    public void registerResponseListener(JmsFactory requestFactory,
                                         JmsFactory responseFactory,
                                         ServiceMessageListener messageListener,
                                         TransactionType transactionType,
                                         TransactionHandler transactionHandler,
                                         ClassLoader cl,
                                         URI serviceUri) throws JmsHostException {

        try {

            Connection connection = requestFactory.getConnection();
            List<ConsumerWorker> consumerWorkers = new ArrayList<ConsumerWorker>();

            ConsumerWorkerTemplate template = new ConsumerWorkerTemplate(transactionHandler,
                                                                         transactionType,
                                                                         messageListener,
                                                                         responseFactory,
                                                                         requestFactory,
                                                                         readTimeout,
                                                                         cl,
                                                                         monitor);
            templateMap.put(serviceUri, template);

            for (int i = 0; i < receiverCount; i++) {
                ConsumerWorker work = new ConsumerWorker(template);
                workScheduler.scheduleWork(work);
                consumerWorkers.add(work);
            }

            connection.start();
            connectionMap.put(serviceUri, connection);
            consumerWorkerMap.put(serviceUri, consumerWorkers);

        } catch (JMSException ex) {
            throw new JmsHostException("Unable to register service listener for: " + serviceUri, ex);
        }

        monitor.registerListener(serviceUri);

    }

    // ---------------------------------- Start of management operations -------------------

    public int getReceiverCount(String service) {

        URI serviceUri = URI.create(service);
        List<ConsumerWorker> consumerWorkers = consumerWorkerMap.get(serviceUri);
        if (consumerWorkers == null) {
            throw new IllegalArgumentException("Unknown service:" + service);
        }
        return consumerWorkers.size();

    }

    public void setReceiverCount(String service, int receiverCount) throws ConfigurationUpdateException {

        URI serviceUri = URI.create(service);
        List<ConsumerWorker> consumerWorkers = consumerWorkerMap.get(serviceUri);
        if (consumerWorkers == null) {
            throw new IllegalArgumentException("Unknown service:" + service);
        }

        if (receiverCount == consumerWorkers.size()) {
            return;
        }

        ConsumerWorkerTemplate template = templateMap.get(serviceUri);
        for (ConsumerWorker consumerWorker : consumerWorkers) {
            consumerWorker.stop();
        }
        consumerWorkers.clear();

        for (int i = 0; i < receiverCount; i++) {
            try {
                ConsumerWorker consumerWorker = new ConsumerWorker(template);
                workScheduler.scheduleWork(consumerWorker);
                consumerWorkers.add(consumerWorker);
            } catch (JMSException e) {
                throw new ConfigurationUpdateException("Error setting listener count for service: " + service, e);
            }
        }

    }

    public List<String> getReceivers() {

        List<String> receivers = new ArrayList<String>();
        for (URI destination : connectionMap.keySet()) {
            receivers.add(destination.toString());
        }
        return receivers;

    }

    // ---------------------------------- End of management operations -------------------

}
