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
package org.fabric3.host.runtime;

import java.net.URI;
import javax.management.MBeanServer;
import javax.xml.namespace.QName;

import org.fabric3.host.monitor.MonitorFactory;

/**
 * Represents a runtime in a domain.
 *
 * @version $Rev$ $Date$
 */
public interface Fabric3Runtime<HI extends HostInfo> {

    /**
     * Returns the host ClassLoader that is parent to all Fabric3 classloaders.
     *
     * @return the host's ClassLoader
     */
    ClassLoader getHostClassLoader();

    /**
     * Sets the host ClassLoader; this will be a parent for all Fabric3 classloaders.
     *
     * @param classLoader the host's ClassLoader
     */
    void setHostClassLoader(ClassLoader classLoader);

    /**
     * Returns the type of info supplied by the host.
     *
     * @return the type of info supplied by the host
     */
    Class<HI> getHostInfoType();

    /**
     * Returns the info this runtime will make available to service components.
     *
     * @return the info this runtime will make available to service components
     */
    HI getHostInfo();

    /**
     * Sets the info this runtime should make available to service components.
     *
     * @param hostInfo the information this runtime should make available to service components
     */
    void setHostInfo(HI hostInfo);

    /**
     * Returns the MonitorFactory that this runtime is using.
     *
     * @return the MonitorFactory that this runtime is using
     */
    MonitorFactory getMonitorFactory();

    /**
     * Sets the MonitorFactory that this runtime should use.
     *
     * @param monitorFactory the MonitorFactory that this runtime should use
     */
    void setMonitorFactory(MonitorFactory monitorFactory);

    /**
     * Returns the MBeanServer this runtime should use.
     *
     * @return the MBeanServer
     */
    MBeanServer getMBeanServer();

    /**
     * Sets the MBeanServer this runtime should use.
     * <p/>
     * This allows the host environment to specify an MBeanServer with which any manageable runtime components should be registered.
     *
     * @param mbServer the MBeanServer this runtime should use
     */
    void setMBeanServer(MBeanServer mbServer);

    /**
     * Returns the JMX sub domain this runtime should use.
     *
     * @return the JMX sub domain this runtime should use
     */
    String getJMXSubDomain();

    /**
     * Sets the JMX sub domain this runtime should use.
     * <p/>
     * This will be used as the sub domain portion of the ObjectName for all MBeans registered with the MBeanServer
     *
     * @param jmxSubDomain the JMX domain this runtime should use
     */
    void setJmxSubDomain(String jmxSubDomain);

    /**
     * Initialize a runtime. An initialized runtime has has completed core service initialization, recovery operations, and is ready to be started.
     *
     * @throws InitializationException if there is an error initializing the runtime
     */
    void initialize() throws InitializationException;

    /**
     * Starts the runtime. A runtime is ready to process requests when it has been started.
     *
     * @throws StartException if there is an error starting the runtime
     */
    void start() throws StartException;

    /**
     * Destroy the runtime. Any further invocations should result in an error.
     *
     * @throws ShutdownException if there is an error destroying the runtime
     */
    void destroy() throws ShutdownException;

    /**
     * Returns the system component providing the designated service.
     *
     * @param service the service interface required
     * @param uri     the id of the system component
     * @param <I>     the Java type for the service interface
     * @return an implementation of the requested service
     */
    <I> I getSystemComponent(Class<I> service, URI uri);

    /**
     * Starts the component context.
     *
     * @param compositeId the context id
     * @throws ContextStartException if an error starting the context is encountered
     */
    void startContext(QName compositeId) throws ContextStartException;

}
