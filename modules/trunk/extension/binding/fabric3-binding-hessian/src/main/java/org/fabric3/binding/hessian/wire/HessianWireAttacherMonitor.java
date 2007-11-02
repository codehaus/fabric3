package org.fabric3.binding.hessian.wire;

import java.net.URI;

import org.fabric3.api.annotation.LogLevel;

/**
 * @version $Rev$ $Date$
 */
public interface HessianWireAttacherMonitor {

    /**
     * Callback when a service has been provisioned as a Hessian endpoint
     *
     * @param address the endpoint address
     */
    @LogLevel("INFO")
    void provisionedEndpoint(URI address);

    /**
     * Callback when a service endpoint has been de-provisioned
     *
     * @param address the endpoint address
     */
    @LogLevel("INFO")
    void removedEndpoint(URI address);

    /**
     * Callback indicating the extension has been initialized.
     */
    @LogLevel("INFO")
    void extensionStarted();

    /**
     * Callback indicating the extension has been stopped.
     */
    @LogLevel("INFO")
    void extensionStopped();


}
