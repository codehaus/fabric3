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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.fabric3.api.annotation.Monitor;
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
public class StandalonePushJmsHost implements JmsHost {

    private WorkScheduler workScheduler;
    private Connection connection;
    private JMSRuntimeMonitor monitor;
    private int receiverCount = 3;
    private List<JMSMessageListenerInvoker> jmsMessageListenerInvokers = new ArrayList<JMSMessageListenerInvoker>();

    /**
     * Injects the monitor.
     * @param monitor Monitor to be injected.
     */
    public StandalonePushJmsHost(@Monitor JMSRuntimeMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * Injects the work scheduler.     *
     * @param workScheduler Work scheduler to be used.
     */
    @Reference
    public void setWorkScheduler(WorkScheduler workScheduler) {
        this.workScheduler = workScheduler;
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
     * Stops the receiver threads.
     * @throws JMSException
     */
    @Destroy
    public void stop() throws JMSException {
        for (JMSMessageListenerInvoker invoker : jmsMessageListenerInvokers) {
            invoker.stop();
        }
        JmsHelper.closeQuietly(connection);
        monitor.jmsRuntimeStop();
    }

    public void registerResponseListener(JMSObjectFactory requestJMSObjectFactory, 
                                         JMSObjectFactory responseJMSObjectFactory,
                                         ResponseMessageListener messageListener, 
                                         TransactionType transactionType,
                                         TransactionHandler transactionHandler,
                                         ClassLoader cl,
                                         URI serviceUri) {
        JMSMessageListenerInvoker invoker = new JMSMessageListenerInvoker(
                requestJMSObjectFactory, responseJMSObjectFactory, messageListener, transactionType, transactionHandler, workScheduler);
        invoker.start(receiverCount);
        jmsMessageListenerInvokers.add(invoker);

    }

}
