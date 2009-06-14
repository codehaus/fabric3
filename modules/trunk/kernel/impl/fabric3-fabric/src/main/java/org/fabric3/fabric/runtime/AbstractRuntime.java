/*
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.runtime;

import java.net.URI;
import javax.management.MBeanServer;
import javax.xml.namespace.QName;

import org.fabric3.contribution.MetaDataStoreImpl;
import org.fabric3.contribution.ProcessorRegistryImpl;
import org.fabric3.fabric.classloader.ClassLoaderRegistryImpl;
import org.fabric3.fabric.component.scope.CompositeScopeContainer;
import org.fabric3.fabric.component.scope.ScopeContainerMonitor;
import org.fabric3.fabric.component.scope.ScopeRegistryImpl;
import org.fabric3.fabric.services.componentmanager.ComponentManagerImpl;
import org.fabric3.fabric.services.lcm.LogicalComponentManagerImpl;
import org.fabric3.fabric.services.lcm.TransientLogicalComponentStore;
import static org.fabric3.host.Names.RUNTIME_URI;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.ContextStartException;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.work.WorkScheduler;
import org.fabric3.model.type.component.Autowire;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.services.lcm.LogicalComponentManager;
import org.fabric3.spi.services.lcm.LogicalComponentStore;
import org.fabric3.spi.services.lcm.ReadException;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractRuntime<HI extends HostInfo> implements Fabric3Runtime<HI>, RuntimeServices {
    private Class<HI> hostInfoType;
    private MBeanServer mbServer;
    private String jmxSubDomain;
    private WorkScheduler workScheduler;

    /**
     * Information provided by the host about its runtime environment.
     */
    private HI hostInfo;

    /**
     * MonitorFactory provided by the host for directing events to its management framework.
     */
    private MonitorFactory monitorFactory;

    /**
     * The LogicalComponentManager that manages all logical components in this runtime.
     */
    private LogicalComponentManager logicalComponentManager;

    /**
     * The ComponentManager that manages all physical components in this runtime.
     */
    private ComponentManager componentManager;

    /**
     * The ScopeContainer used to managed system component instances.
     */
    private ScopeContainer scopeContainer;

    /**
     * The ClassLoaderRegristy that manages all runtime classloaders.
     */
    private ClassLoaderRegistry classLoaderRegistry;

    /**
     * The MetaDataStore that indexes contribution metadata and artifacts.
     */
    private MetaDataStore metaDataStore;

    /**
     * The ScopeRegistry that manages runtime ScopeContainers
     */
    private ScopeRegistry scopeRegistry;

    private ClassLoader hostClassLoader;


    protected AbstractRuntime(Class<HI> runtimeInfoType, MonitorFactory monitorFactory) {
        this.hostInfoType = runtimeInfoType;
        this.monitorFactory = monitorFactory;
    }

    protected AbstractRuntime(Class<HI> runtimeInfoType) {
        this.hostInfoType = runtimeInfoType;
    }

    public ClassLoader getHostClassLoader() {
        return hostClassLoader;
    }

    public void setHostClassLoader(ClassLoader hostClassLoader) {
        this.hostClassLoader = hostClassLoader;
    }

    public Class<HI> getHostInfoType() {
        return hostInfoType;
    }

    public HI getHostInfo() {
        return hostInfo;
    }

    public void setHostInfo(HI hostInfo) {
        this.hostInfo = hostInfo;
    }

    public MonitorFactory getMonitorFactory() {
        return monitorFactory;
    }

    public void setMonitorFactory(MonitorFactory monitorFactory) {
        this.monitorFactory = monitorFactory;
    }

    public MBeanServer getMBeanServer() {
        return mbServer;
    }

    public void setMBeanServer(MBeanServer mbServer) {
        this.mbServer = mbServer;
    }

    public String getJMXSubDomain() {
        return jmxSubDomain;
    }

    public void setJmxSubDomain(String jmxDomain) {
        this.jmxSubDomain = jmxDomain;
    }

    public void boot() throws InitializationException {
        LogicalComponentStore store = new TransientLogicalComponentStore(RUNTIME_URI, Autowire.ON);
        logicalComponentManager = new LogicalComponentManagerImpl(store);
        try {
            logicalComponentManager.initialize();
        } catch (ReadException e) {
            throw new InitializationException(e);
        }
        componentManager = new ComponentManagerImpl();
        classLoaderRegistry = new ClassLoaderRegistryImpl();
        ProcessorRegistry processorRegistry = new ProcessorRegistryImpl();
        metaDataStore = new MetaDataStoreImpl(classLoaderRegistry, processorRegistry);
        scopeContainer = new CompositeScopeContainer(getMonitorFactory().getMonitor(ScopeContainerMonitor.class));
        scopeContainer.start();
        scopeRegistry = new ScopeRegistryImpl();
        scopeRegistry.register(scopeContainer);
    }

    public void destroy() {
        // destroy system components
        WorkContext workContext = new WorkContext();
        scopeContainer.stopAllContexts(workContext);
    }

    public <I> I getSystemComponent(Class<I> service, URI uri) {
        if (RuntimeServices.class.equals(service)) {
            return service.cast(this);
        }
        AtomicComponent<?> component = (AtomicComponent<?>) componentManager.getComponent(uri);
        if (component == null) {
            return null;
        }

        WorkContext workContext = new WorkContext();
        WorkContext oldContext = WorkContextTunnel.setThreadWorkContext(workContext);
        try {
            InstanceWrapper<?> wrapper = scopeContainer.getWrapper(component, workContext);
            return service.cast(wrapper.getInstance());
        } catch (InstanceLifecycleException e) {
            // this is an error with the runtime and not something that is recoverable
            throw new AssertionError(e);
        } finally {
            WorkContextTunnel.setThreadWorkContext(oldContext);
        }
    }

    public void startContext(QName deployable) throws ContextStartException {
        WorkContext workContext = new WorkContext();
        CallFrame frame = new CallFrame(deployable);
        workContext.addCallFrame(frame);
        try {
            scopeContainer.startContext(workContext);
        } catch (GroupInitializationException e) {
            throw new ContextStartException(e);
        }
    }

    public LogicalComponentManager getLogicalComponentManager() {
        return logicalComponentManager;
    }

    public ComponentManager getComponentManager() {
        return componentManager;
    }

    public ScopeContainer getScopeContainer() {
        return scopeContainer;
    }

    public ClassLoaderRegistry getClassLoaderRegistry() {
        return classLoaderRegistry;
    }

    public MetaDataStore getMetaDataStore() {
        return metaDataStore;
    }

    public ScopeRegistry getScopeRegistry() {
        return scopeRegistry;
    }

    public WorkScheduler getWorkScheduler() {
        return workScheduler;
    }

    public void setWorkScheduler(WorkScheduler workScheduler) {
        this.workScheduler = workScheduler;
    }
}
