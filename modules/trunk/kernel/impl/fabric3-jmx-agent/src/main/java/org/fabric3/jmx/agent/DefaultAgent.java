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
package org.fabric3.jmx.agent;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

/**
 * Default agent.
 *
 * @version $Revison$ $Date$
 */
public class DefaultAgent implements Agent {

    private static final String DOMAIN = "fabric3";
    private MBeanServer mBeanServer;

    /**
     * Initialies the server.
     *
     * @throws ManagementException If unable to start the agent.
     */
    public DefaultAgent() throws ManagementException {
        mBeanServer = MBeanServerFactory.createMBeanServer(DOMAIN);
    }

    /**
     * @see org.fabric3.jmx.agent.Agent#getMBeanServer()
     */
    public MBeanServer getMBeanServer() {
        return mBeanServer;
    }

    /**
     * @see org.fabric3.jmx.agent.Agent#register(java.lang.Object,java.lang.String)
     */
    public final void register(Object instance, String name) throws ManagementException {

        try {
            mBeanServer.registerMBean(instance, new ObjectName(name));
        } catch (Exception ex) {
            throw new ManagementException(ex);
        }

    }



}
