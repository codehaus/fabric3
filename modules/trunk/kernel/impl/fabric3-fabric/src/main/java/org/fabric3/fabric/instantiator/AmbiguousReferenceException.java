package org.fabric3.fabric.instantiator;

import java.net.URI;

import org.fabric3.fabric.instantiator.LogicalInstantiationException;

public class AmbiguousReferenceException extends LogicalInstantiationException {
    private static final long serialVersionUID = -6600598658356829665L;

    /**
     * @param promotedComponentUri Promoted component URI.
     */
    public AmbiguousReferenceException(URI promotedComponentUri) {
        super("More than one reference available on component: " + promotedComponentUri);
    }

}
