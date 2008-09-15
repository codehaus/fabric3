package org.fabric3.java.control;

import org.fabric3.spi.generator.GenerationException;

/**
 * Thrown when a injection sire cannot be found for a callback.
 *
 * @version $Rev: 2779 $ $Date: 2008-02-16 03:02:28 -0800 (Sat, 16 Feb 2008) $
 */
public class CallbackSiteNotFound extends GenerationException {
    private static final long serialVersionUID = 6734181652978179903L;

    public CallbackSiteNotFound(String message, String identifier) {
        super(message, identifier);
    }
}
