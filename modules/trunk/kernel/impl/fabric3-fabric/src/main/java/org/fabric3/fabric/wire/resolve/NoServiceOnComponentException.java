package org.fabric3.fabric.wire.resolve;

import java.net.URI;

import org.fabric3.spi.wire.TargetResolutionException;

public class NoServiceOnComponentException extends TargetResolutionException {

    /**
     * 
     */
    private static final long serialVersionUID = -6600598658356829665L;
    
    /**
     * @param targetUri Target component URI.
     */
    public NoServiceOnComponentException(URI targetUri) {
        super("No services available on component: " + targetUri);
    }

}
