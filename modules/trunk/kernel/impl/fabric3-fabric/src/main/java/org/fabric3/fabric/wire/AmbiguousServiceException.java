package org.fabric3.fabric.wire;

import java.net.URI;

import org.fabric3.spi.wire.PromotionException;

public class AmbiguousServiceException extends PromotionException {

    /**
     * 
     */
    private static final long serialVersionUID = -6600598658356829665L;
    
    /**
     * @param promotedComponentUri Promoted component URI.
     */
    public AmbiguousServiceException(URI promotedComponentUri) {
        super("More than one service available on component: " + promotedComponentUri);
    }

}
