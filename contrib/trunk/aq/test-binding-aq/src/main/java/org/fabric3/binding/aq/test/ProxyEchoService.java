package org.fabric3.binding.aq.test;

/**
 * Forwards the Request to the Echo Service
 */
public interface ProxyEchoService {
    
    /**
     * Forwards the Request for the EchoService
     * @param message
     */
    void forwardRequest(String message);

}
