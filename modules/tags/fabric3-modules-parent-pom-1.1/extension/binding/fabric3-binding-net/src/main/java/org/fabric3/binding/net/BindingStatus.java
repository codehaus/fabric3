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
package org.fabric3.binding.net;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.spi.services.VoidService;

/**
 * Reports the status of the binding extension.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class BindingStatus implements VoidService {
    private NetBindingMonitor monitor;

    public BindingStatus(@Monitor NetBindingMonitor monitor) {
        this.monitor = monitor;
    }

    @Init
    public void init() {
        monitor.extensionStarted();
    }


    @Destroy
    public void destroy() {
        monitor.extensionStopped();
    }
}
