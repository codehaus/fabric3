package org.fabric3.java;

import org.fabric3.spi.generator.GenerationException;

/**
 * @version $Rev: 2779 $ $Date: 2008-02-16 03:02:28 -0800 (Sat, 16 Feb 2008) $
 */
public class CallbackSiteNotFound extends GenerationException {
    public CallbackSiteNotFound(String message, String identifier) {
        super(message, identifier);
    }
}
