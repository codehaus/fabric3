package org.fabric3.fabric.instantiator;

import java.net.URI;

import org.fabric3.fabric.instantiator.LogicalInstantiationException;

public class NoReferenceOnComponentException extends LogicalInstantiationException {
    private static final long serialVersionUID = -6600598658356829665L;

    /**
     * @param promotedComponentUri Promoted component URI.
     */
    public NoReferenceOnComponentException(URI promotedComponentUri) {
        super("No reference available on component: " + promotedComponentUri);
    }

}
