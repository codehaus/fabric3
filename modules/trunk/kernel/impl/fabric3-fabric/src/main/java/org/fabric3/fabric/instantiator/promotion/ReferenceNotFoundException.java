package org.fabric3.fabric.instantiator.promotion;

import java.net.URI;

import org.fabric3.fabric.instantiator.PromotionException;

public class ReferenceNotFoundException extends PromotionException {
    private static final long serialVersionUID = -6600598658356829665L;

    /**
     * @param promotedComponentUri Promoted component URI.
     */
    public ReferenceNotFoundException(URI promotedComponentUri, String referenceName) {
        super("Service: " + referenceName + " not found on component: " + promotedComponentUri);
    }

}
