package org.fabric3.fabric.wire.resolve;

import org.fabric3.spi.wire.TargetResolutionException;

public class NoServiceOnComponentException extends TargetResolutionException {
    private static final long serialVersionUID = -6600598658356829665L;

    public NoServiceOnComponentException(String message) {
        super(message);
    }

}
