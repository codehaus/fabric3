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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.runtime.JmsMonitor;
import org.fabric3.binding.jms.runtime.tx.TransactionHandler;
import org.fabric3.host.work.WorkScheduler;

/**
 * A JmsHost implementation that initializes one or more message consumers per service that receive and dispatch messages in separate threads. The
 * message consumers are registered with the kernel WorkScheduler.
 *
 * @version $Revsion$ $Date$
 */
public class StandalonePullJmsHost implements StandalonePullJmsHostMBean {
    private WorkScheduler workScheduler;
    private long readTimeout = 30000L;
    private JmsMonitor monitor;
    private int receiverCount = 3;
    private Map<URI, List<ConsumerWorker>> consumerWorkerMap = new HashMap<URI, List<ConsumerWorker>>();
    private Map<URI, ConsumerWorkerTemplate> templateMap = new HashMap<URI, ConsumerWorkerTemplate>();

    /**
     * Constructor.
     *
     * @param workScheduler the work scheduler
     * @param monitor       Monitor used for logging.
     */
    public StandalonePullJmsHost(@Reference WorkScheduler workScheduler, @Monitor JmsMonitor monitor) {
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
//        monitor.unRegisterListener(serviceUri);
    }

    public void registerListener(ServiceListenerConfiguration configuration) throws JmsHostException {

        URI serviceUri = configuration.getServiceUri();
        try {

            List<ConsumerWorker> consumerWorkers = new ArrayList<ConsumerWorker>();
            ConsumerWorkerTemplate template = createTemplate(configuration);
            templateMap.put(serviceUri, template);

            for (int i = 0; i < receiverCount; i++) {
                ConsumerWorker work = new ConsumerWorker(template);
                workScheduler.scheduleWork(work);
                consumerWorkers.add(work);
            }
            template.getRequestConnection().start();
            consumerWorkerMap.put(serviceUri, consumerWorkers);

        } catch (JMSException ex) {
            throw new JmsHostException("Unable to register service listener for: " + serviceUri, ex);
        }

       // monitor.registerListener(serviceUri);

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
        for (URI destination : templateMap.keySet()) {
            receivers.add(destination.toString());
        }
        return receivers;

    }

    // ---------------------------------- End of management operations -------------------

    private ConsumerWorkerTemplate createTemplate(ServiceListenerConfiguration configuration) throws JMSException {

        Connection requestConnection = configuration.getRequestConnectionFactory().createConnection();
        Destination requestDestination = configuration.getRequestDestination();
        Connection responseConnection = null;
        ConnectionFactory responseConnectionFactory = configuration.getResponseConnectionFactory();
        if (responseConnectionFactory != null) {
            responseConnection = responseConnectionFactory.createConnection();
        }
        Destination responseDestination = configuration.getResponseDestination();
        ServiceMessageListener listener = configuration.getMessageListener();
        TransactionType transactionType = configuration.getTransactionType();
        TransactionHandler handler = configuration.getTransactionHandler();
        ClassLoader classloader = configuration.getClassloader();
        return new ConsumerWorkerTemplate(listener,
                                          requestConnection,
                                          requestDestination,
                                          responseConnection,
                                          responseDestination,
                                          readTimeout,
                                          transactionType,
                                          handler,
                                          classloader,
                                          monitor);
    }

}
