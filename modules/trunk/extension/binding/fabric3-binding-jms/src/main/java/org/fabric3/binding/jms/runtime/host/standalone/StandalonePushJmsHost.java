/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
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
import java.util.HashMap;
import java.util.Map;
import javax.jms.Connection;
import javax.jms.JMSException;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.runtime.JmsFactory;
import org.fabric3.binding.jms.runtime.JMSRuntimeMonitor;
import org.fabric3.binding.jms.runtime.JmsHost;
import org.fabric3.binding.jms.runtime.JmsHostException;
import org.fabric3.binding.jms.runtime.ServiceMessageListener;
import org.fabric3.binding.jms.runtime.helper.JmsHelper;
import org.fabric3.binding.jms.runtime.tx.TransactionHandler;
import org.fabric3.host.work.WorkScheduler;

/**
 * A JmsHost implementation that uses the JMS ServerSessionPool API.
 *
 * @version $Revsion$ $Date: 2007-05-22 00:19:04 +0100 (Tue, 22 May 2007) $
 */
public class StandalonePushJmsHost implements JmsHost {

    private WorkScheduler workScheduler;
    private Connection connection;
    private JMSRuntimeMonitor monitor;
    private int receiverCount = 3;
    private Map<URI, JMSMessageListenerInvoker> jmsMessageListenerInvokers = new HashMap<URI, JMSMessageListenerInvoker>();

    /**
     * Constructor.
     *
     * @param workScheduler the work scheduler
     * @param monitor       Monitor to be injected.
     */
    public StandalonePushJmsHost(@Reference WorkScheduler workScheduler, @Monitor JMSRuntimeMonitor monitor) {
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
        JmsHelper.closeQuietly(connection);
        jmsMessageListenerInvokers.clear();
        monitor.stop();
    }

    public boolean isRegistered(URI serviceUri) {
        return jmsMessageListenerInvokers.containsKey(serviceUri);
    }

    public void registerResponseListener(JmsFactory requestFactory,
                                         JmsFactory responseFactory,
                                         ServiceMessageListener messageListener,
                                         TransactionType transactionType,
                                         TransactionHandler transactionHandler,
                                         ClassLoader cl,
                                         URI serviceUri) throws JmsHostException {
        JMSMessageListenerInvoker invoker = new JMSMessageListenerInvoker(requestFactory,
                                                                          responseFactory,
                                                                          messageListener,
                                                                          transactionType,
                                                                          transactionHandler,
                                                                          workScheduler,
                                                                          monitor);
        try {
            invoker.start(receiverCount);
        } catch (JMSException e) {
            throw new JmsHostException("Unable to register service listener for: " + serviceUri, e);
        }
        jmsMessageListenerInvokers.put(serviceUri, invoker);

    }

    public void unregisterListener(URI serviceUri) {
        JMSMessageListenerInvoker invoker = jmsMessageListenerInvokers.remove(serviceUri);
        invoker.stop();
    }

}
