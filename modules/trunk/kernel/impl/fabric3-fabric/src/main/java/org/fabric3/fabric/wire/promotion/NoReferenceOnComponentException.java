package org.fabric3.fabric.wire.promotion;

import java.net.URI;

import org.fabric3.spi.wire.PromotionException;

public class NoReferenceOnComponentException extends PromotionException {

    /**
     * 
     */
    private static final long serialVersionUID = -6600598658356829665L;
    
    /**
     * @param promotedComponentUri Promoted component URI.
     */
    public NoReferenceOnComponentException(URI promotedComponentUri) {
        super("No reference available on component: " + promotedComponentUri);
    }

}
