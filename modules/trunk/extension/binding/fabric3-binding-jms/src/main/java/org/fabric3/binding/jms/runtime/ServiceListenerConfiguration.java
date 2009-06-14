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
   */
package org.fabric3.binding.jms.runtime;

import java.net.URI;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.runtime.tx.TransactionHandler;

/**
 * A configuration template used when registering a ServiceMessageListener with a JmsHost.
 *
 * @version $Revision$ $Date$
 */
public class ServiceListenerConfiguration {

    private URI serviceUri;
    private ServiceMessageListener messageListener;
    private ConnectionFactory requestConnectionFactory;
    private Destination requestDestination;
    private ConnectionFactory responseConnectionFactory;
    private Destination responseDestination;
    private TransactionType transactionType;
    private TransactionHandler transactionHandler;
    private ClassLoader classloader;

    public URI getServiceUri() {
        return serviceUri;
    }

    public void setServiceUri(URI serviceUri) {
        this.serviceUri = serviceUri;
    }

    public ServiceMessageListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(ServiceMessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public ConnectionFactory getRequestConnectionFactory() {
        return requestConnectionFactory;
    }

    public void setRequestConnectionFactory(ConnectionFactory requestConnectionFactory) {
        this.requestConnectionFactory = requestConnectionFactory;
    }

    public Destination getRequestDestination() {
        return requestDestination;
    }

    public void setRequestDestination(Destination requestDestination) {
        this.requestDestination = requestDestination;
    }

    public ConnectionFactory getResponseConnectionFactory() {
        return responseConnectionFactory;
    }

    public void setResponseConnectionFactory(ConnectionFactory responseConnectionFactory) {
        this.responseConnectionFactory = responseConnectionFactory;
    }

    public Destination getResponseDestination() {
        return responseDestination;
    }

    public void setResponseDestination(Destination responseDestination) {
        this.responseDestination = responseDestination;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public TransactionHandler getTransactionHandler() {
        return transactionHandler;
    }

    public void setTransactionHandler(TransactionHandler transactionHandler) {
        this.transactionHandler = transactionHandler;
    }

    public ClassLoader getClassloader() {
        return classloader;
    }

    public void setClassloader(ClassLoader classloader) {
        this.classloader = classloader;
    }
}
