package org.fabric3.binding.aq.test;



/**
 * This Mesaage is One way
 */
public class EchoServiceImpl implements EchoService {

    /**
     * @see org.fabric3.binding.aq.test.EchoService#areYouThere(java.lang.String)
     */
    public void areYouThere(final String echoMessage) {       
        if(echoMessage != null){
            System.out.println("Echo Message Received " + echoMessage);
        }else {
            throw new AssertionError("Echo Message Should not be Null");
        }
        
    }

}
