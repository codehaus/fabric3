package org.fabric3.binding.hessian.wire;

import java.net.URI;

import org.fabric3.api.annotation.LogLevel;

/**
 * @version $Rev$ $Date$
 */
public interface HessianWireAttacherMonitor {

    @LogLevel("INFO")
    void provisionedEndpoint(URI address);

    @LogLevel("INFO")
    void removedEndpoint(URI address);

}
