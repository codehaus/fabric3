package org.fabric3.binding.aq.test;

import org.osoa.sca.annotations.Reference;

/**
 * ProxyEchoService Implementation
 */
public class ProxyEchoServiceImpl implements ProxyEchoService{
    
    
    private EchoService echoService;

    /**
     * Sets the Echo Service
     */
    @Reference
    public void setEchoService(EchoService echoService) {
        this.echoService = echoService;
    }

    /**
     * Forwards the Request to The EchoService
     */
    public void forwardRequest(String message) {        
        echoService.areYouThere(message);
    }

}
