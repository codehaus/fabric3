/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
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
