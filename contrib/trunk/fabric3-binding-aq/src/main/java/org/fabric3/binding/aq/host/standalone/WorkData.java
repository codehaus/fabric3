package org.fabric3.binding.aq.host.standalone;

import javax.jms.Destination;
import javax.jms.MessageListener;
import javax.jms.XAQueueConnectionFactory;

import org.fabric3.binding.aq.tx.TransactionHandler;
/**
 * Object used for Retaining data that is used by the ConsumerWorker
 */
class WorkData {
    
    private XAQueueConnectionFactory connectionFactory;
    private Destination destination;
    private MessageListener listener;
    private ClassLoader classLoader;
    private TransactionHandler transactionHandler;

    /**
     * Sets the Connection Factory
     * @param connectionFactory
     */
    void setConnectionFactory(final XAQueueConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Set the Destination
     * @param destination
     */
    void setDestination(final Destination destination) {
        this.destination = destination;
    }

    /**
     * Sets the Listener
     * @param listener
     */
    void setListener(final MessageListener listener) {
        this.listener = listener;
    }
    
    /**
     * Sets the Transaction Handler
     * @param transactionHandler
     */
    void setTxHandler(final TransactionHandler transactionHandler) {
        this.transactionHandler = transactionHandler;
    }

    /**
     * Sets the Class loader
     * @param classLoader
     */
    void setClassLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Gets the XAQueueConnection Factory
     * @return
     */
    XAQueueConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * Gets the Destination
     * @return
     */
    Destination getDestination() {
        return destination;
    }

    /**
     * Gets the {@link MessageListener}
     * @return
     */
    MessageListener getListener() {
        return listener;
    }

    /**
     * Gets the ClassLoder
     * @return
     */
    ClassLoader getClassLoader() {
        return classLoader;
    }
    
    /**
     * Gets the Transaction Handler
     * @return Transaction Handler
     */
    TransactionHandler getTxHandler(){
        return transactionHandler;
    }
}
