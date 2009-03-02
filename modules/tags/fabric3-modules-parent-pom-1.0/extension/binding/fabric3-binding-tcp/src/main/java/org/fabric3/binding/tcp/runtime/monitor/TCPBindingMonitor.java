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
package org.fabric3.binding.tcp.runtime.monitor;

import org.fabric3.api.annotation.logging.Info;
import org.fabric3.api.annotation.logging.Severe;

/**
 * Monitor interface to log significant events for TCP binding.
 */
public interface TCPBindingMonitor {
    /**
     * Log event of successful TCP extension started.
     * 
     * @param msg start event message
     */
    @Info
    void onTcpExtensionStarted(String msg);

    /**
     * Log event of exception occurred in TCP extension.
     * 
     * @param msg Error message
     * @param throwable exception
     */
    @Severe
    void onException(String msg, Throwable throwable);

}
