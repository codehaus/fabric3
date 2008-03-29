/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.fabric3.fabric.runtime;

import java.net.URI;
import java.net.URL;

import org.fabric3.fabric.component.scope.CompositeScopeContainer;
import org.fabric3.fabric.component.scope.ScopeContainerMonitor;
import static org.fabric3.fabric.runtime.ComponentNames.EVENT_SERVICE_URI;
import static org.fabric3.fabric.runtime.ComponentNames.RUNTIME_URI;
import org.fabric3.fabric.services.componentmanager.ComponentManagerImpl;
import org.fabric3.fabric.services.domain.LogicalComponentManagerImpl;
import org.fabric3.fabric.services.domain.NonPersistentLogicalComponentStore;
import org.fabric3.host.management.ManagementService;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.StartException;
import org.fabric3.monitor.MonitorFactory;
import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.scdl.Autowire;
import org.fabric3.spi.assembly.AssemblyException;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.TargetResolutionException;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.runtime.RuntimeServices;
import org.fabric3.spi.runtime.assembly.LogicalComponentManager;
import org.fabric3.spi.runtime.assembly.LogicalComponentStore;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.runtime.component.RegistrationException;
import org.fabric3.spi.services.event.EventService;
import org.fabric3.spi.services.event.RuntimeStart;
import org.fabric3.spi.services.management.Fabric3ManagementService;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractRuntime<I extends HostInfo> implements Fabric3Runtime<I>, RuntimeServices {
    private String applicationName;
    private URL applicationScdl;
    private Class<I> hostInfoType;
    private ManagementService<?> managementService;

    /**
     * Information provided by the host about its runtime environment.
     */
    private I hostInfo;

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
    private CompositeScopeContainer scopeContainer;

    private ClassLoader hostClassLoader;

    protected AbstractRuntime(Class<I> runtimeInfoType, MonitorFactory monitorFactory) {
        this.hostInfoType = runtimeInfoType;
        this.monitorFactory = monitorFactory;
    }

    protected AbstractRuntime(Class<I> runtimeInfoType) {
        this.hostInfoType = runtimeInfoType;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public URL getApplicationScdl() {
        return applicationScdl;
    }

    public void setApplicationScdl(URL applicationScdl) {
        this.applicationScdl = applicationScdl;
    }

    public ClassLoader getHostClassLoader() {
        return hostClassLoader;
    }

    public void setHostClassLoader(ClassLoader hostClassLoader) {
        this.hostClassLoader = hostClassLoader;
    }

    public Class<I> getHostInfoType() {
        return hostInfoType;
    }

    public I getHostInfo() {
        return hostInfo;
    }

    public void setHostInfo(I hostInfo) {
        this.hostInfo = hostInfo;
    }

    public MonitorFactory getMonitorFactory() {
        return monitorFactory;
    }

    public void setMonitorFactory(MonitorFactory monitorFactory) {
        this.monitorFactory = monitorFactory;
    }

    public ManagementService<?> getManagementService() {
        return managementService;
    }

    public void setManagementService(ManagementService<?> managementService) {
        this.managementService = managementService;
    }

    public void initialize() throws InitializationException {
        LogicalComponentStore store = new NonPersistentLogicalComponentStore(RUNTIME_URI, Autowire.ON);
        logicalComponentManager = new LogicalComponentManagerImpl(store);
        try {
            logicalComponentManager.initialize();
        } catch (AssemblyException e) {
            throw new InitializationException(e);
        }
        componentManager = new ComponentManagerImpl((Fabric3ManagementService) getManagementService());
        scopeContainer = new CompositeScopeContainer(getMonitorFactory().getMonitor(ScopeContainerMonitor.class));
        scopeContainer.start();
    }


    public void start() throws StartException {
        // starts the runtime by publishing a start event
        EventService eventService = getSystemComponent(EventService.class, EVENT_SERVICE_URI);
        eventService.publish(new RuntimeStart());
    }

    public void destroy() {
    }

    public void registerComponent(LogicalComponent<?> logical, AtomicComponent<?> physical) throws RegistrationException {
        LogicalCompositeComponent domain = logicalComponentManager.getDomain();
        domain.addComponent(logical);
        componentManager.register(physical);
        scopeContainer.register(physical);
    }

    public <I> I getSystemComponent(Class<I> service, URI uri) {

        // JFM FIXME WorkContext should be moved down to host-api and should be created by the host
        AtomicComponent<?> component = (AtomicComponent<?>) componentManager.getComponent(uri);
        WorkContext workContext = new WorkContext();
        PojoWorkContextTunnel.setThreadWorkContext(workContext);

        try {
            InstanceWrapper<?> wrapper = scopeContainer.getWrapper(component, workContext);
            return service.cast(wrapper.getInstance());
        } catch (TargetResolutionException e) {
            // FIXME throw something better
            throw new AssertionError();
        }
    }

    public LogicalComponentManager getLogicalComponentManager() {
        return logicalComponentManager;
    }

    public ComponentManager getComponentManager() {
        return componentManager;
    }

    public ScopeContainer<?> getScopeContainer() {
        return scopeContainer;
    }
}
