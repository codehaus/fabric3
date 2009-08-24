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

import javax.jms.Connection;
import javax.jms.Destination;

import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.runtime.JmsMonitor;
import org.fabric3.binding.jms.runtime.host.standalone.ServiceMessageListener;
import org.fabric3.binding.jms.runtime.tx.TransactionHandler;

/**
 * A thread pull message from destination and invoke Message listener.
 *
 * @version $Rev$ $Date$
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
