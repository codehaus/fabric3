package org.fabric3.spi.services.synthesize;

import org.fabric3.host.Fabric3Exception;

/**
 * @version $Revision$ $Date$
 */
public class ComponentRegistrationException extends Fabric3Exception {
    private static final long serialVersionUID = -8515386804100570512L;

    public ComponentRegistrationException(Throwable cause) {
        super(cause);
    }
}
