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
import java.util.HashMap;
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
import org.fabric3.binding.jms.runtime.JmsHost;
import org.fabric3.binding.jms.runtime.JmsHostException;
import org.fabric3.binding.jms.runtime.JmsMonitor;
import org.fabric3.binding.jms.runtime.ServiceListenerConfiguration;
import org.fabric3.binding.jms.runtime.ServiceMessageListener;
import org.fabric3.binding.jms.runtime.tx.TransactionHandler;
import org.fabric3.host.work.WorkScheduler;

/**
 * A JmsHost implementation that uses the JMS ServerSessionPool API.
 *
 * @version $Revsion$ $Date: 2007-05-22 00:19:04 +0100 (Tue, 22 May 2007) $
 */
public class StandalonePushJmsHost implements JmsHost {

    private WorkScheduler workScheduler;
    private JmsMonitor monitor;
    private int receiverCount = 3;
    private Map<URI, JMSMessageListenerInvoker> jmsMessageListenerInvokers = new HashMap<URI, JMSMessageListenerInvoker>();

    /**
     * Constructor.
     *
     * @param workScheduler the work scheduler
     * @param monitor       Monitor to be injected.
     */
    public StandalonePushJmsHost(@Reference WorkScheduler workScheduler, @Monitor JmsMonitor monitor) {
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
     * Initializes the host.
     */
    @Init
    public void init() {
        monitor.start();
    }

    /**
     * Stops the receiver threads.
     *
     * @throws JMSException if an error stoping the JMSMessageListenerInvokers is raised.
     */
    @Destroy
    public void stop() throws JMSException {
        for (JMSMessageListenerInvoker invoker : jmsMessageListenerInvokers.values()) {
            invoker.stop();
        }
        jmsMessageListenerInvokers.clear();
        monitor.stop();
    }

    public boolean isRegistered(URI serviceUri) {
        return jmsMessageListenerInvokers.containsKey(serviceUri);
    }

    public void registerListener(ServiceListenerConfiguration configuration) throws JmsHostException {

        URI serviceUri = configuration.getServiceUri();
        try {

            Connection requestConnection = configuration.getRequestConnectionFactory().createConnection();
            Destination requestDestination = configuration.getRequestDestination();
            Connection responseConnection = null;
            ConnectionFactory responseConnectionFactory = configuration.getResponseConnectionFactory();
            if (responseConnectionFactory != null) {
                responseConnection = responseConnectionFactory.createConnection();
            }
            Destination responseDestination = configuration.getResponseDestination();
            ServiceMessageListener messageListener = configuration.getMessageListener();
            TransactionType transactionType = configuration.getTransactionType();
            TransactionHandler transactionHandler = configuration.getTransactionHandler();

            JMSMessageListenerInvoker invoker = new JMSMessageListenerInvoker(requestConnection,
                                                                              requestDestination,
                                                                              responseConnection,
                                                                              responseDestination,
                                                                              messageListener,
                                                                              transactionType,
                                                                              transactionHandler,
                                                                              workScheduler,
                                                                              monitor);
            invoker.start(receiverCount);
            jmsMessageListenerInvokers.put(serviceUri, invoker);
        } catch (JMSException e) {
            throw new JmsHostException("Unable to register service listener for: " + serviceUri, e);
        }

    }

    public void unregisterListener(URI serviceUri) {
        JMSMessageListenerInvoker invoker = jmsMessageListenerInvokers.remove(serviceUri);
        invoker.stop();
    }

}
