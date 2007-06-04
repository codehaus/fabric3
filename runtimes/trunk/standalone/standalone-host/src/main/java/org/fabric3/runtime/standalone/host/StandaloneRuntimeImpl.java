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
package org.fabric3.runtime.standalone.host;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.fabric3.api.annotation.LogLevel;
import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.fabric.assembly.DistributedAssembly;
import org.fabric3.fabric.implementation.java.JavaComponent;
import org.fabric3.fabric.implementation.java.JavaInvokerInterceptor;
import org.fabric3.fabric.implementation.pojo.PojoWorkContextTunnel;
import org.fabric3.fabric.loader.LoaderContextImpl;
import org.fabric3.fabric.monitor.JavaLoggingMonitorFactory;
import org.fabric3.fabric.runtime.AbstractRuntime;
import static org.fabric3.fabric.runtime.ComponentNames.DISTRIBUTED_ASSEMBLY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.LOADER_URI;
import org.fabric3.runtime.standalone.StandaloneHostInfo;
import org.fabric3.runtime.standalone.StandaloneRuntime;
import org.fabric3.runtime.standalone.host.implementation.launched.Launched;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.CompositeComponentType;
import org.fabric3.spi.model.type.CompositeImplementation;
import org.fabric3.spi.model.type.Implementation;
import org.fabric3.spi.model.type.Scope;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;

/**
 * @version $Rev$ $Date$
 */
public class StandaloneRuntimeImpl extends AbstractRuntime<StandaloneHostInfo> implements StandaloneRuntime {
    JavaLoggingMonitorFactory monitorFactory;
    StandaloneMonitor monitor;

    public StandaloneRuntimeImpl() {
        super(StandaloneHostInfo.class);
        monitorFactory = new JavaLoggingMonitorFactory();
        setMonitorFactory(monitorFactory);
        monitor = monitorFactory.getMonitor(StandaloneMonitor.class);
    }


    /**
     * Deploys the specified application SCDL and runs the lauched component within the deployed composite.
     *
     * @param applicationScdl        Application SCDL that implements the composite.
     * @param applicationClassLoader Classloader used to deploy the composite.
     * @param args                   Arguments to be passed to the lauched component.
     * @deprecated This is a hack for deployment and should be removed.
     */
    public int deployAndRun(URL applicationScdl, ClassLoader applicationClassLoader, String[] args) throws Exception {
        URI compositeUri = new URI("fabric3://./domain/main/");

        CompositeImplementation impl = new CompositeImplementation();
        impl.setScdlLocation(applicationScdl);
        impl.setClassLoader(applicationClassLoader);

        ComponentDefinition<CompositeImplementation> definition =
                new ComponentDefinition<CompositeImplementation>("main", impl);
        try {
            LoaderRegistry loader = getSystemComponent(LoaderRegistry.class, LOADER_URI);
            DistributedAssembly assembly = getSystemComponent(DistributedAssembly.class, DISTRIBUTED_ASSEMBLY_URI);
            // deploy the components
            LoaderContext loaderContext = new LoaderContextImpl(null, null);
            loader.loadComponentType(impl, loaderContext);
            assembly.activate(definition, false);
            ScopeRegistry scopeRegistry = getScopeRegistry();
            ScopeContainer<URI> container = scopeRegistry.getScopeContainer(Scope.COMPOSITE);
            WorkContext workContext = new SimpleWorkContext();
            workContext.setScopeIdentifier(Scope.COMPOSITE, compositeUri);

            container.startContext(workContext, compositeUri);
            PojoWorkContextTunnel.setThreadWorkContext(workContext);
            try {
                return run(impl, args, compositeUri, workContext);
            } finally {
                PojoWorkContextTunnel.setThreadWorkContext(null);
                container.stopContext(workContext);
            }
        } catch (Exception e) {
            monitor.runError(e);
        }
        return -1;

    }

    private int run(CompositeImplementation impl, String[] args, URI compositeUri, WorkContext workContext)
            throws Exception {
        CompositeComponentType componentType = impl.getComponentType();
        Map<String, ComponentDefinition<? extends Implementation<?>>> components = componentType.getComponents();
        for (Map.Entry<String, ComponentDefinition<? extends Implementation<?>>> entry : components.entrySet()) {
            String name = entry.getKey();
            ComponentDefinition<? extends Implementation<?>> launchedDefinition = entry.getValue();
            Implementation implementation = launchedDefinition.getImplementation();
            if (implementation.getClass().isAssignableFrom(Launched.class)) {
                return run(compositeUri.resolve(name), args, workContext);
            }
        }
        return -1;
    }

    @SuppressWarnings({"unchecked"})
    private int run(URI componentUri, String[] args, WorkContext workContext)
            throws InvocationTargetException, NoSuchMethodException {
        WorkContext oldContext = PojoWorkContextTunnel.getThreadWorkContext();
        try {
            // FIXME we should not be creating a JavaInvokerInterceptor here
            // FIXME this should create a wire to the Launched component and invoke the head interceptor on the chain
            JavaComponent component = (JavaComponent) getComponentManager().getComponent(componentUri);
            PojoWorkContextTunnel.setThreadWorkContext(workContext);
            Object instance = component.createObjectFactory().getInstance();
            Method m = instance.getClass().getMethod("main", String[].class);
            ScopeContainer scopeContainer = component.getScopeContainer();
            JavaInvokerInterceptor<?, ?> interceptor = new JavaInvokerInterceptor(m, component, scopeContainer);
            Message msg = new MessageImpl();
            msg.setWorkContext(workContext);
            msg.setBody(new Object[]{args});
            Message result = interceptor.invoke(msg);
            try {
                if (result != null) {
                    return int.class.cast(result.getBody());
                }
            } catch (ClassCastException e) {
                return 0;
            }
            return 0;
        } catch (ObjectCreationException e) {
            throw new AssertionError(e);
        } finally {
            PojoWorkContextTunnel.setThreadWorkContext(oldContext);
        }
    }

    public interface StandaloneMonitor {
        @LogLevel("SEVERE")
        void runError(Exception e);
    }
}
