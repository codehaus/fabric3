package org.fabric3.binding.ws.axis2.runtime;

import org.fabric3.api.annotation.logging.Info;

/**
 * @version $Revision$ $Date$
 */
public interface ServiceProvisionerMonitor {

    @Info
    void endpointProvisioned(String address);
}
