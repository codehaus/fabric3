package org.fabric3.binding.aq.test;

/**
 * Test the message bing sent in a fire and forget mode
 */
public interface EchoService {
    
    /**
     * @param echoMessage
     */
    void areYouThere(String echoMessage);

}
