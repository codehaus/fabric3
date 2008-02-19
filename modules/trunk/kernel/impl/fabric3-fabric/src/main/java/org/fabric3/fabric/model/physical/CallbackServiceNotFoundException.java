package org.fabric3.fabric.model.physical;

import org.fabric3.spi.generator.GenerationException;

/**
 * @version $Revision$ $Date$
 */
public class CallbackServiceNotFoundException extends GenerationException {

    public CallbackServiceNotFoundException(String message, String identifier) {
        super(message, identifier);
    }
}
