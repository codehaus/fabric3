package org.fabric3.fabric.wire.resolve;

import java.net.URI;

import org.fabric3.spi.wire.PromotionException;

public class TargetComponentNotFoundException extends PromotionException {

    /**
     * 
     */
    private static final long serialVersionUID = -6600598658356829665L;
    
    /**
     * @param componentUri Component URI.
     */
    public TargetComponentNotFoundException(URI componentUri) {
        super("Promoted component not found: " + componentUri);
    }

}
