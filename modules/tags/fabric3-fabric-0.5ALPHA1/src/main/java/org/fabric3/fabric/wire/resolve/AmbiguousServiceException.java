package org.fabric3.fabric.wire.resolve;

import java.net.URI;

import org.fabric3.spi.wire.TargetResolutionException;

public class AmbiguousServiceException extends TargetResolutionException {

    /**
     * 
     */
    private static final long serialVersionUID = -6600598658356829665L;
    
    /**
     * @param targetUri Target component URI.
     */
    public AmbiguousServiceException(URI targetUri) {
        super("More than one service available on component: " + targetUri);
    }

}
