package org.fabric3.fabric.instantiator.resolve;

import org.fabric3.fabric.instantiator.resolve.TargetResolutionException;

public class AmbiguousServiceException extends TargetResolutionException {
    private static final long serialVersionUID = -6600598658356829665L;
    
    public AmbiguousServiceException(String message) {
        super(message);
    }

}
