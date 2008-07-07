package org.fabric3.fabric.instantiator;

import java.net.URI;

import org.fabric3.fabric.instantiator.LogicalInstantiationException;

public class PromotedComponentNotFoundException extends LogicalInstantiationException {
    private static final long serialVersionUID = -6600598658356829665L;

    /**
     * @param promotedComponentUri Promoted component URI.
     */
    public PromotedComponentNotFoundException(URI promotedComponentUri) {
        super("Promoted component not found: " + promotedComponentUri);
    }

}
