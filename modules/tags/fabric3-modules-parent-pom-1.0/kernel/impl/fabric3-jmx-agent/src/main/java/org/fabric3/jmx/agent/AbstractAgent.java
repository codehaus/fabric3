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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Default agent.
 *
 * @version $Revison$ $Date$
 */
public abstract class AbstractAgent implements Agent {

    private static final String DOMAIN = "fabric3";
    private MBeanServer mBeanServer;
    private AtomicBoolean started = new AtomicBoolean();
    private JMXConnectorServer connectorServer;
    protected int minPort;
    private int maxPort;

    /**
     * Constructor using the default RMI port (1099).
     *
     * @throws ManagementException If unable to start the agent.
     */
    public AbstractAgent() throws ManagementException {
        this(1099, -1);
    }

    /**
     * Constructor using the given port range.
     *
     * @param minPort the minimum port number
     * @param maxPort the maximum port number
     * @throws ManagementException If unable to start the agent.
     */
    protected AbstractAgent(int minPort, int maxPort) {
        this.minPort = minPort;
        this.maxPort = maxPort;
        mBeanServer = MBeanServerFactory.createMBeanServer(DOMAIN);
    }

    public MBeanServer getMBeanServer() {
        return mBeanServer;
    }

    public final void register(Object instance, String name) throws ManagementException {

        try {
            mBeanServer.registerMBean(instance, new ObjectName(name));
        } catch (Exception ex) {
            throw new ManagementException(ex);
        }

    }

    public final void start() throws ManagementException {

        try {

            if (started.get()) {
                throw new IllegalArgumentException("Agent already started");
            }

            preStart();

            JMXServiceURL url = getAdaptorUrl();
            connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, mBeanServer);

            connectorServer.start();

            started.set(true);

        } catch (MalformedURLException ex) {
            throw new ManagementException(ex);
        } catch (IOException ex) {
            throw new ManagementException(ex);
        }

    }

    public final void run() {
        while (started.get()) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // continue;
                }
            }
        }
    }

    public final void shutdown() throws ManagementException {

        try {

            if (!started.get()) {
                throw new IllegalArgumentException("Agent not started");
            }

            connectorServer.stop();
            postStop();
            started.set(false);
            synchronized (this) {
                notify();
            }

        } catch (IOException ex) {
            throw new ManagementException(ex);
        }

    }

    public int getMinPort() {
        return minPort;
    }

    public int getMaxPort() {
        return maxPort;
    }

    /**
     * Gets the adaptor URL.
     *
     * @return Adaptor URL.
     */
    protected abstract JMXServiceURL getAdaptorUrl();

    /**
     * Any initialiation required for protocol specific agent.
     */
    protected abstract void preStart();

    /**
     * Any initialiation required for protocol specific agent.
     */
    protected abstract void postStop();


}