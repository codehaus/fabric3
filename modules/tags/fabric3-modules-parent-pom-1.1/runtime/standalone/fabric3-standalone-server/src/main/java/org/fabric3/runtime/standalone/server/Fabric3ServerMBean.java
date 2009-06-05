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
package org.fabric3.runtime.standalone.server;

import org.fabric3.host.RuntimeMode;

/**
 * Management interface for the Fabric3 server.
 *
 * @version $Revision$ $Date$
 */
public interface Fabric3ServerMBean {

    /**
     * Starts a runtime specified by the bootpath.
     *
     * @param mode      the mode to start the runtime in
     * @param jmxDomain JMX domain the runtime.
     */
    public void startRuntime(RuntimeMode mode, String jmxDomain);

    /**
     * Shuts down a runtime specified by the bootpath.
     *
     * @param bootPath Bootpath for the runtime.
     */
    public void shutdownRuntime(String bootPath);

    /**
     * Starts the server.
     */
    public void shutdown();

}
