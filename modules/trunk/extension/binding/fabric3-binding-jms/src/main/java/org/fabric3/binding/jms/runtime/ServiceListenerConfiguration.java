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
