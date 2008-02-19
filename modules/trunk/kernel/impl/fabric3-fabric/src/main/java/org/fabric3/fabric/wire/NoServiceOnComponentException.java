package org.fabric3.fabric.wire;

import java.net.URI;

import org.fabric3.spi.wire.PromotionException;

public class NoServiceOnComponentException extends PromotionException {

    /**
     * 
     */
    private static final long serialVersionUID = -6600598658356829665L;
    
    /**
     * @param promotedComponentUri Promoted component URI.
     */
    public NoServiceOnComponentException(URI promotedComponentUri) {
        super("No services available on component: " + promotedComponentUri);
    }

}
