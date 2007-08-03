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
import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.fabric.assembly.ActivateException;
import org.fabric3.fabric.assembly.RuntimeAssembly;
import org.fabric3.fabric.component.ComponentManagerImpl;
import org.fabric3.fabric.monitor.NullMonitorFactory;
import static org.fabric3.fabric.runtime.ComponentNames.EVENT_SERVICE_URI;
import static org.fabric3.fabric.runtime.ComponentNames.EXTENSION_METADATA_STORE_URI;
import static org.fabric3.fabric.runtime.ComponentNames.RUNTIME_ASSEMBLY_URI;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.management.ManagementService;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.StartException;
import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.Include;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.ComponentManager;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;
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

    public void includeExtensionContributions(List<URI> contributionUris) throws InitializationException {
        RuntimeAssembly assembly = getSystemComponent(RuntimeAssembly.class, RUNTIME_ASSEMBLY_URI);
        Composite composite = createExensionComposite(contributionUris);
        try {
            assembly.includeInDomain(composite);
        } catch (ActivateException e) {
            throw new ExtensionInitializationException("Error activating extensions", e);
        }
    }

    protected ComponentManager getComponentManager() {
        return componentManager;
    }

    protected ScopeRegistry getScopeRegistry() {
        return getSystemComponent(ScopeRegistry.class, ComponentNames.SCOPE_REGISTRY_URI);
    }

    /**
     * Creates an extension composite by including deployables from contributions identified by the list of URIs
     *
     * @param contributionUris the contributions containing the deployables to include
     * @return the extension composite
     * @throws InitializationException if an error occurs creating the composite
     */
    @SuppressWarnings({"unchecked"})
    private Composite createExensionComposite(List<URI> contributionUris) throws InitializationException {
        MetaDataStore metaDataStore = getSystemComponent(MetaDataStore.class, EXTENSION_METADATA_STORE_URI);
        if (metaDataStore == null) {
            String id = EXTENSION_METADATA_STORE_URI.toString();
            throw new InitializationException("Extensions metadata store not configured", id);
        }
        QName qName = new QName(org.fabric3.spi.Constants.FABRIC3_SYSTEM_NS, "extensiosn");
        Composite composite = new Composite(qName);
        for (URI uri : contributionUris) {
            Contribution contribution = metaDataStore.find(uri);
            assert contribution != null;

            for (Resource resource : contribution.getResources()) {
                for (ResourceElement<?, ?> entry : resource.getResourceElements()) {

                    if (!(entry.getValue() instanceof Composite)) {
                        continue;
                    }
                    ResourceElement<QNameSymbol, Composite> element = (ResourceElement<QNameSymbol, Composite>) entry;
                    QName name = element.getSymbol().getKey();
                    Composite childComposite = (Composite) element.getValue();
                    for (Deployable deployable : contribution.getManifest().getDeployables()) {
                        if (deployable.getName().equals(name)) {
                            Include include = new Include();
                            include.setName(name);
                            include.setIncluded(childComposite);
                            composite.add(include);
                            break;
                        }
                    }
                }
            }
        }
        return composite;
    }

}
