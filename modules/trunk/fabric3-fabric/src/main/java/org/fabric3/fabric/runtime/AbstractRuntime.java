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

import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.fabric.component.ComponentManagerImpl;
import org.fabric3.fabric.deployer.Deployer;
import org.fabric3.fabric.implementation.pojo.PojoWorkContextTunnel;
import org.fabric3.fabric.monitor.NullMonitorFactory;
import static org.fabric3.fabric.runtime.ComponentNames.EVENT_SERVICE_URI;
import org.fabric3.host.management.ManagementService;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.StartException;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.ComponentManager;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.model.type.Scope;
import org.fabric3.spi.services.event.EventService;
import org.fabric3.spi.services.event.RuntimeStart;
import org.fabric3.spi.services.management.Fabric3ManagementService;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractRuntime<I extends HostInfo> implements Fabric3Runtime<I> {
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
     * The ComponentManager that manages all components in this runtime.
     */
    private ComponentManager componentManager;

    private Component systemComponent;
    private Component fabric3System;
    private ClassLoader hostClassLoader;


    protected AbstractRuntime(Class<I> runtimeInfoType) {
        this(runtimeInfoType, new NullMonitorFactory());
    }

    protected AbstractRuntime(Class<I> runtimeInfoType, MonitorFactory monitorFactory) {
        this.hostInfoType = runtimeInfoType;
        this.monitorFactory = monitorFactory;
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
        componentManager = new ComponentManagerImpl((Fabric3ManagementService) getManagementService());
    }


    public void start() throws StartException {
        // starts the runtime by publishing a start event
        EventService eventService = getSystemComponent(EventService.class, EVENT_SERVICE_URI);
        eventService.publish(new RuntimeStart());
    }

    public void destroy() {
        if (fabric3System != null) {
            fabric3System.stop();
            fabric3System = null;
        }
        if (systemComponent != null) {
            systemComponent.stop();
            systemComponent = null;
        }
    }

    public <I> I getSystemComponent(Class<I> service, URI uri) {
        // JFM FIXME WorkContext should be moved down to host-api and should be created by the host
        URI parent = uri.resolve(".");
        AtomicComponent component = (AtomicComponent) componentManager.getComponent(uri);
        WorkContext workContext = new SimpleWorkContext();
        workContext.setScopeIdentifier(Scope.COMPOSITE, parent);
        PojoWorkContextTunnel.setThreadWorkContext(workContext);

        // FIXME we should get the InstanceWrapper from the composite scope container and then get the instance from it
        try {
            return service.cast(component.createObjectFactory().getInstance());
        } catch (ObjectCreationException e) {
            throw new AssertionError();
        }
    }

    protected ComponentManager getComponentManager() {
        return componentManager;
    }

    protected ScopeRegistry getScopeRegistry() {
        return getSystemComponent(ScopeRegistry.class, ComponentNames.SCOPE_REGISTRY_URI);
    }

    protected Deployer getDeployer() {
        return getSystemComponent(Deployer.class, ComponentNames.DEPLOYER_URI);
    }

}
