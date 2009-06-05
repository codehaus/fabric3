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

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.fabric3.binding.jms.common.CorrelationScheme;

/**
 * Template for configuring non-operation specific objects for a JmsInterceptor. Used by InterceptorConfiguration.
 *
 * @version $Revision$ $Date$
 */
public class WireConfiguration {
    private CorrelationScheme correlationScheme;
    private ConnectionFactory requestConnectionFactory;
    private Destination requestDestination;
    private ClassLoader classloader;
    private JmsResponseMessageListener messageReceiver;

    public CorrelationScheme getCorrelationScheme() {
        return correlationScheme;
    }

    public void setCorrelationScheme(CorrelationScheme correlationScheme) {
        this.correlationScheme = correlationScheme;
    }

    public Destination getRequestDestination() {
        return requestDestination;
    }

    public void setRequestDestination(Destination requestDestination) {
        this.requestDestination = requestDestination;
    }

    public ConnectionFactory getRequestConnectionFactory() {
        return requestConnectionFactory;
    }

    public void setRequestConnectionFactory(ConnectionFactory requestConnectionFactory) {
        this.requestConnectionFactory = requestConnectionFactory;
    }

    public ClassLoader getClassloader() {
        return classloader;
    }

    public void setClassloader(ClassLoader classloader) {
        this.classloader = classloader;
    }

    public JmsResponseMessageListener getMessageReceiver() {
        return messageReceiver;
    }

    public void setMessageReceiver(JmsResponseMessageListener messageReceiver) {
        this.messageReceiver = messageReceiver;
    }
}