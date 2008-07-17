package org.fabric3.fabric.generator.wire;

import org.fabric3.spi.generator.GenerationException;

/**
 * @version $Revision$ $Date$
 */
public class CallbackServiceNotFoundException extends GenerationException {
    private static final long serialVersionUID = 5437567367368328467L;

    public CallbackServiceNotFoundException(String message, String identifier) {
        super(message, identifier);
    }
}
