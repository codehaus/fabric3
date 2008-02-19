package org.fabric3.fabric.wire;

import java.net.URI;

import org.fabric3.spi.wire.PromotionException;

public class PromotedComponentNotFoundException extends PromotionException {

    /**
     * 
     */
    private static final long serialVersionUID = -6600598658356829665L;
    
    /**
     * @param promotedComponentUri Promoted component URI.
     */
    public PromotedComponentNotFoundException(URI promotedComponentUri) {
        super("Promoted component not found: " + promotedComponentUri);
    }

}
