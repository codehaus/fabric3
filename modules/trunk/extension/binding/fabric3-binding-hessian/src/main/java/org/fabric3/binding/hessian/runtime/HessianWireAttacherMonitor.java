package org.fabric3.binding.hessian.runtime;

import java.net.URI;

import org.fabric3.api.annotation.logging.Info;
import org.fabric3.api.annotation.logging.LogLevel;
import org.fabric3.api.annotation.logging.LogLevels;

/**
 * @version $Rev$ $Date$
 */
public interface HessianWireAttacherMonitor {

    /**
     * Callback when a service has been provisioned as a Hessian endpoint
     *
     * @param address the endpoint address
     */
    @Info
    void provisionedEndpoint(URI address);

    /**
     * Callback when a service endpoint has been de-provisioned
     *
     * @param address the endpoint address
     */
    @Info
    void removedEndpoint(URI address);

    /**
     * Callback indicating the extension has been initialized.
     */
    @Info
    void extensionStarted();

    /**
     * Callback indicating the extension has been stopped.
     */
    @Info
    void extensionStopped();


}
