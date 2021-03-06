package org.fabric3.binding.aq.runtime.host;

/**
 * Marker Interface for Polling consumers
 */
public interface PollingConsumer extends Runnable {
    
    /**
     * Stops the consumer from taking on more work
     */
    void stopConsumption();

}
