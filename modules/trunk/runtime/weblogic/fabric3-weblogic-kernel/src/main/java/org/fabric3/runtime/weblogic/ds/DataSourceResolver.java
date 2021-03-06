/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.runtime.weblogic.ds;

import java.util.HashSet;
import java.util.Set;
import javax.management.AttributeChangeNotification;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.datasource.spi.DataSourceRegistry;

import static org.fabric3.runtime.weblogic.api.Constants.WLS_RUNTIME_SERVICE_MBEAN;

/**
 * Resolves datasources configured in WebLogic via JMX and populates the {@link DataSourceRegistry}. Specifically, this implementation walks the
 * WebLogic MBean hierarchy:
 * <pre>
 *      RuntimeServiceMBean
 *          |
 *          ---DomainConfiguration
 *                  |
 *                  ---JDBCSystemResources
 *                        |
 *                        ---JDBCResource[]
 *                              |
 *                              ---JDBCDataSourceParams#JNDINames
 * <p/>
 * </pre>
 * The <code>JDBCResource</code> beans are iterated to determine the JNDI names where all system datasources are bound. The corresponding DataSource
 * instances are then resolved through JNDI and the Fabric3 DataSourceRegistry is populated.
 * <p/>
 * This implementation also dynamically updates the Fabric3 datasource registry if a configuration change is made to a live WebLogic domain or
 * runtime.
 * <p/>
 * Note that only system datasources will be resolved, not application-level datasources (i.e. those defined in Java EE modules).
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class DataSourceResolver {
    private DataSourceRegistry registry;
    private MBeanServer mbServer;
    private DataSourceResolverMonitor monitor;
    private Set<String> previousDataSources = new HashSet<String>();

    public DataSourceResolver(@Reference DataSourceRegistry registry, @Reference MBeanServer mbServer, @Monitor DataSourceResolverMonitor monitor) {
        this.registry = registry;
        this.mbServer = mbServer;
        this.monitor = monitor;
    }

    @Init
    public void init() throws JMException, NamingException {
        updateDataSources(true);
    }

    /**
     * Resolves WebLogic datasources and updates the Fabric3 datasource registry. If a datasource exists in the registry, it will be overwritten.
     *
     * @param initialize true if the call is being made during server initialization
     * @throws NamingException if a JNDI connection cannot be established
     * @throws JMException     if there is an error looking up the JMX datasource mbeans
     */
    private void updateDataSources(boolean initialize) throws NamingException, JMException {
        InitialContext context = null;
        try {
            context = new InitialContext();
            ObjectName domainConfig = (ObjectName) mbServer.getAttribute(WLS_RUNTIME_SERVICE_MBEAN, "DomainConfiguration");
            ObjectName[] systemResources = (ObjectName[]) mbServer.getAttribute(domainConfig, "JDBCSystemResources");
            if (initialize) {
                // add a listener to be notified of changes
                mbServer.addNotificationListener(domainConfig, new DataSourceChangeListener(), null, null);
            }
            Set<String> newDataSources = new HashSet<String>();
            for (ObjectName systemResource : systemResources) {
                ObjectName resource = (ObjectName) mbServer.getAttribute(systemResource, "JDBCResource");
                ObjectName params = (ObjectName) mbServer.getAttribute(resource, "JDBCDataSourceParams");
                String[] jndiNames = (String[]) mbServer.getAttribute(params, "JNDINames");
                registerDataSources(jndiNames, newDataSources, context);
            }
            for (String previous : previousDataSources) {
                if (!newDataSources.contains(previous)) {
                    monitor.removeDatasource(previous);
                    // datasource that was found previously was deleted - remove it from the registry
                    registry.unregister(previous);
                }
            }
            previousDataSources = newDataSources;
        } finally {
            if (context != null) {
                context.close();
            }
        }
    }

    private void registerDataSources(String[] jndiNames, Set<String> newDataSources, InitialContext context) throws NamingException {
        for (String name : jndiNames) {
            try {
                DataSource dataSource = (DataSource) context.lookup(name);
                monitor.registerDatasource(name);
                newDataSources.add(name);
                registry.register(name, dataSource);
            } catch (NameNotFoundException e) {
                // This can happen if the datasource is configured on the admin server and not targeted to the current managed server
                // Issue a warning
                monitor.dataSourceNotFound(name);
            }
        }
    }

    /**
     * Listens for datasource configuration changes and updates the registry accordingly.
     */
    private class DataSourceChangeListener implements NotificationListener {

        public void handleNotification(Notification notification, Object handback) {
            if (!(notification instanceof AttributeChangeNotification)) {
                return;
            }
            AttributeChangeNotification change = (AttributeChangeNotification) notification;
            if (!"JDBCSystemResources".equals(change.getAttributeName())) {
                return;
            }
            try {
                updateDataSources(false);
            } catch (JMException e) {
                monitor.error(e);
            } catch (NamingException e) {
                monitor.error(e);
            }
        }
    }


}
