package org.fabric3.fabric.wire.resolve;

import java.net.URI;

import org.fabric3.spi.wire.PromotionException;

public class TargetComponentNotFoundException extends PromotionException {
    private static final long serialVersionUID = -6600598658356829665L;

    public TargetComponentNotFoundException(String message) {
        super(message);
    }

}
