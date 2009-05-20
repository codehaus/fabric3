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

import javax.jms.Connection;
import javax.jms.Destination;

import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.runtime.JmsMonitor;
import org.fabric3.binding.jms.runtime.ServiceMessageListener;
import org.fabric3.binding.jms.runtime.tx.TransactionHandler;

/**
 * A thread pull message from destination and invoke Message listener.
 *
 * @version $Revision$ $Date$
 */
public class ConsumerWorkerTemplate {

    private TransactionHandler transactionHandler;
    private ServiceMessageListener listener;
    private Connection requestConnection;
    private Connection responseConnection;
    private Destination requestDestination;
    private Destination responseDestination;
    private long readTimeout;
    private TransactionType transactionType;
    private ClassLoader cl;
    private JmsMonitor monitor;

    public ConsumerWorkerTemplate(ServiceMessageListener listener,
                                  Connection requestConnection,
                                  Destination requestDestination,
                                  Connection responseConnection,
                                  Destination responseDestination,
                                  long readTimeout,
                                  TransactionType transactionType,
                                  TransactionHandler transactionHandler,
                                  ClassLoader cl,
                                  JmsMonitor monitor) {
        this.transactionHandler = transactionHandler;
        this.transactionType = transactionType;
        this.listener = listener;
        this.requestConnection = requestConnection;
        this.responseConnection = responseConnection;
        this.requestDestination = requestDestination;
        this.responseDestination = responseDestination;
        this.readTimeout = readTimeout;
        this.cl = cl;
        this.monitor = monitor;
    }

    public TransactionHandler getTransactionHandler() {
        return transactionHandler;
    }

    public ServiceMessageListener getListener() {
        return listener;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public ClassLoader getClassloader() {
        return cl;
    }

    public Connection getRequestConnection() {
        return requestConnection;
    }

    public Connection getResponseConnection() {
        return responseConnection;
    }

    public Destination getRequestDestination() {
        return requestDestination;
    }

    public Destination getResponseDestination() {
        return responseDestination;
    }

    public JmsMonitor getMonitor() {
        return monitor;
    }

}
