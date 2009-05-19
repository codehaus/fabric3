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
package org.fabric3.binding.jms.runtime.lookup;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionFactory;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;

/**
 * A ConnectionFactory that delegates to an underlying ConnectionFactory to create a JMS connection. The connection will then be shared.
 *
 * @version $Revision$ $Date$
 */
public class SharedConnectionFactory implements ConnectionFactory {
    private ConnectionFactory delegate;
    private volatile ConnectionWrapper connection;

    /**
     * Constructor.
     *
     * @param delegate the underlying ConnectionFactory. 
     */
    public SharedConnectionFactory(ConnectionFactory delegate) {
        this.delegate = delegate;
    }

    public Connection createConnection() throws JMSException {
        if (connection == null) {
            synchronized (this) {
                if (connection == null) {
                    connection = new ConnectionWrapper(delegate.createConnection());
                }
            }
        }
        return connection;
    }

    public Connection createConnection(String username, String password) throws JMSException {
        if (connection == null) {
            synchronized (this) {
                if (connection == null) {
                    connection = new ConnectionWrapper(delegate.createConnection(username, password));
                }
            }
        }
        return connection;
    }

    /**
     * Closes and resets the connection if one is created.
     *
     * @throws JMSException if there is an error closing the connection
     */
    public synchronized void reset() throws JMSException {
        if (connection == null) {
            return;
        }
        connection.closeWrapped();
        connection = null;
    }

    /**
     * Wrapper for a Connection that avoids closing it so the connection may be shared.
     */
    private class ConnectionWrapper implements QueueConnection, TopicConnection {
        private Connection wrapped;

        private ConnectionWrapper(Connection wrapped) {
            this.wrapped = wrapped;
        }

        public Session createSession(boolean b, int i) throws JMSException {
            return wrapped.createSession(b, i);
        }

        public String getClientID() throws JMSException {
            return wrapped.getClientID();
        }

        public void setClientID(String s) throws JMSException {
            wrapped.setClientID(s);
        }

        public ConnectionMetaData getMetaData() throws JMSException {
            return wrapped.getMetaData();
        }

        public ExceptionListener getExceptionListener() throws JMSException {
            return wrapped.getExceptionListener();
        }

        public void setExceptionListener(ExceptionListener exceptionListener) throws JMSException {
            wrapped.setExceptionListener(exceptionListener);
        }

        public void start() throws JMSException {
            wrapped.start();
        }

        public void stop() throws JMSException {
            wrapped.stop();
        }

        public void close() throws JMSException {
            // avoid closing
        }

        public ConnectionConsumer createConnectionConsumer(Destination destination, String s, ServerSessionPool serverSessionPool, int i)
                throws JMSException {
            return wrapped.createConnectionConsumer(destination, s, serverSessionPool, i);
        }

        public ConnectionConsumer createDurableConnectionConsumer(Topic topic, String s, String s1, ServerSessionPool serverSessionPool, int i)
                throws JMSException {
            return wrapped.createDurableConnectionConsumer(topic, s, s1, serverSessionPool, i);
        }

        public void closeWrapped() throws JMSException {
            wrapped.close();
        }

        public QueueSession createQueueSession(boolean b, int i) throws JMSException {
            return getQueueConnection().createQueueSession(b, i);
        }

        public ConnectionConsumer createConnectionConsumer(Queue queue, String s, ServerSessionPool serverSessionPool, int i) throws JMSException {
            return getQueueConnection().createConnectionConsumer(queue, s, serverSessionPool, i);
        }

        public TopicSession createTopicSession(boolean b, int i) throws JMSException {
            return getTopicConnection().createTopicSession(b, i);
        }

        public ConnectionConsumer createConnectionConsumer(Topic topic, String s, ServerSessionPool serverSessionPool, int i) throws JMSException {
            return getTopicConnection().createConnectionConsumer(topic, s, serverSessionPool, i);
        }

        private QueueConnection getQueueConnection() {
            if (!(wrapped instanceof QueueConnection)) {
                // programming error
                throw new AssertionError("Connection must be a QueueConnection");
            }
            return (QueueConnection) wrapped;
        }

        private TopicConnection getTopicConnection() {
            if (!(wrapped instanceof TopicConnection)) {
                // programming error
                throw new AssertionError("Connection must be a TopicConnection");
            }
            return (TopicConnection) wrapped;
        }

    }
}
