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

import org.fabric3.api.annotation.logging.Severe;
import org.fabric3.api.annotation.logging.Info;

/**
 * Monitor for communications events.
 *
 * @version $Revision$ $Date$
 */
public interface NetBindingMonitor {

    @Info
    void extensionStarted();

    @Info
    void extensionStopped();

    @Info
    void startHttpListener(int port);

    @Info
    void startHttpsListener(int port);

    @Info
    void startTcpListener(int port);

    @Severe
    void error(Throwable e);

    @Severe
    void errorMessage(String msg);

}
