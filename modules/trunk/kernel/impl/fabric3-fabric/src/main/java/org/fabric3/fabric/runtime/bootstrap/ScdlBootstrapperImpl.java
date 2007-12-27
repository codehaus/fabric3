/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.fabric.runtime.bootstrap;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.w3c.dom.Document;

import org.fabric3.fabric.assembly.InstantiationException;
import org.fabric3.fabric.component.scope.ScopeRegistryImpl;
import org.fabric3.fabric.idl.java.JavaInterfaceProcessorRegistryImpl;
import org.fabric3.fabric.implementation.singleton.SingletonComponent;
import org.fabric3.fabric.implementation.singleton.SingletonImplementation;
import org.fabric3.fabric.model.logical.AtomicComponentInstantiator;
import org.fabric3.fabric.model.logical.ComponentInstantiator;
import org.fabric3.fabric.runtime.ComponentNames;
import static org.fabric3.fabric.runtime.ComponentNames.APPLICATION_CLASSLOADER_ID;
import static org.fabric3.fabric.runtime.ComponentNames.BOOT_CLASSLOADER_ID;
import static org.fabric3.fabric.runtime.ComponentNames.RUNTIME_URI;
import org.fabric3.fabric.runtime.ConfigLoadException;
import org.fabric3.fabric.runtime.ConfigLoader;
import org.fabric3.fabric.runtime.DefaultConfigLoader;
import org.fabric3.fabric.services.classloading.ClassLoaderRegistryImpl;
import org.fabric3.fabric.services.contribution.MetaDataStoreImpl;
import org.fabric3.fabric.services.contribution.ProcessorRegistryImpl;
import org.fabric3.fabric.services.factories.xml.XMLFactoryImpl;
import org.fabric3.fabric.services.xstream.XStreamFactoryImpl;
import org.fabric3.host.monitor.FormatterRegistry;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.loader.common.LoaderContextImpl;
import org.fabric3.monitor.DefaultFormatterRegistry;
import org.fabric3.pojo.processor.JavaIntrospectionHelper;
import org.fabric3.pojo.scdl.JavaMappedService;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.Implementation;
import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.assembly.Assembly;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.deployer.CompositeClassLoader;
import org.fabric3.spi.idl.InvalidServiceContractException;
import org.fabric3.spi.idl.java.JavaInterfaceProcessorRegistry;
import org.fabric3.spi.idl.java.JavaServiceContract;
import org.fabric3.spi.loader.Loader;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.runtime.RuntimeServices;
import org.fabric3.spi.runtime.assembly.LogicalComponentManager;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.runtime.component.RegistrationException;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.factories.xml.XMLFactory;

/**
 * Bootstrapper that initializes a runtime by reading a system SCDL file.
 *
 * @version $Rev$ $Date$
 */
public class ScdlBootstrapperImpl implements ScdlBootstrapper {

    private static final URI HOST_CLASSLOADER_ID = URI.create("sca://./hostClassLoader");
    private static final String USER_CONFIG = System.getProperty("user.home") + "/.fabric3/config.xml";

    private final JavaInterfaceProcessorRegistry interfaceProcessorRegistry = new JavaInterfaceProcessorRegistryImpl();
    private final ConfigLoader configLoader = new DefaultConfigLoader();

    private URL scdlLocation;
    private LogicalComponent<CompositeImplementation> domain;

    public URL getScdlLocation() {
        return scdlLocation;
    }

    public void setScdlLocation(URL scdlLocation) {
        this.scdlLocation = scdlLocation;
    }

    public void bootPrimordial(Fabric3Runtime<?> runtime, ClassLoader bootClassLoader, ClassLoader appClassLoader)
            throws InitializationException {

        // get the runtime domain component (needed to register components)
        domain = getDomain(runtime);

        // register primordial components provided by the runtime itself
        registerRuntimeComponents(runtime);

        // create and register bootstrap components provided by this bootstrapper
        registerBootstrapComponents(runtime);

        // register the classloaders
        registerClassLoaders(runtime, bootClassLoader, appClassLoader);

    }

    private LogicalComponent<CompositeImplementation> getDomain(Fabric3Runtime<?> runtime) {
        RuntimeServices runtimeServices = (RuntimeServices) runtime;
        LogicalComponentManager logicalComponentManager = runtimeServices.getLogicalComponentManager();
        return logicalComponentManager.getDomain();
    }

    public void bootSystem(Fabric3Runtime<?> runtime) throws InitializationException {
        ClassLoaderRegistry classLoaderRegistry = runtime.getSystemComponent(ClassLoaderRegistry.class, ComponentNames.CLASSLOADER_REGISTRY_URI);
        Loader loader = BootstrapLoaderFactory.createLoader(runtime);
        Assembly runtimeAssembly = runtime.getSystemComponent(Assembly.class, ComponentNames.RUNTIME_ASSEMBLY_URI);
        try {

            // load the system composite
            ClassLoader bootCl = classLoaderRegistry.getClassLoader(BOOT_CLASSLOADER_ID);
            LoaderContext loaderContext = new LoaderContextImpl(bootCl, BOOT_CLASSLOADER_ID, scdlLocation);
            Composite composite = loader.load(scdlLocation, Composite.class, loaderContext);

            Document userConfig = loadUserConfig();
            if (userConfig != null) {
                domain.setPropertyValue("userConfig", userConfig);
            }

            // include in the runtime domain assembly
            runtimeAssembly.includeInDomain(composite);

        } catch (LoaderException e) {
            throw new InitializationException(e);
        } catch (ActivateException e) {
            throw new InitializationException(e);
        } catch (ConfigLoadException e) {
            throw new InitializationException(e);
        }

    }

    private <T extends HostInfo> void registerRuntimeComponents(Fabric3Runtime<T> runtime) throws InitializationException {
        RuntimeServices runtimeServices = (RuntimeServices) runtime;

        // services available through the outward facing Fabric3Runtime API
        registerSystemComponent(runtimeServices, "MonitorFactory", MonitorFactory.class, runtime.getMonitorFactory());
        registerSystemComponent(runtimeServices, "HostInfo", runtime.getHostInfoType(), runtime.getHostInfo());

        // services available through the inward facing RuntimeServices SPI
        ComponentManager componentManager = runtimeServices.getComponentManager();
        LogicalComponentManager logicalCM = runtimeServices.getLogicalComponentManager();
        ScopeContainer<?> scopeContainer = runtimeServices.getScopeContainer();
        registerSystemComponent(runtimeServices, "ComponentManager", ComponentManager.class, componentManager);
        registerSystemComponent(runtimeServices, "LogicalComponentManager", LogicalComponentManager.class, logicalCM);
        registerSystemComponent(runtimeServices, "CompositeScopeContainer", ScopeContainer.class, scopeContainer);
    }

    private void registerBootstrapComponents(Fabric3Runtime<?> runtime) throws InitializationException {


        RuntimeServices runtimeServices = (RuntimeServices) runtime;

        DefaultFormatterRegistry formatterRegistry = new DefaultFormatterRegistry();
        registerSystemComponent(runtimeServices, "FormatterRegistry", FormatterRegistry.class, formatterRegistry);

        XMLFactory xmlFactory = new XMLFactoryImpl();
        registerSystemComponent(runtimeServices, "XMLFactory", XMLFactory.class, xmlFactory);

        ClassLoaderRegistry classLoaderRegistry = new ClassLoaderRegistryImpl();
        registerSystemComponent(runtimeServices, "ClassLoaderRegistry", ClassLoaderRegistry.class, classLoaderRegistry);

        ScopeRegistry scopeRegistry = new ScopeRegistryImpl();
        scopeRegistry.register(runtimeServices.getScopeContainer());
        registerSystemComponent(runtimeServices, "ScopeRegistry", ScopeRegistry.class, scopeRegistry);

        ProcessorRegistry processorRegistry = new ProcessorRegistryImpl();
        registerSystemComponent(runtimeServices, "ContributionProcessorRegistry", ProcessorRegistry.class, processorRegistry);

        HostInfo info = runtime.getHostInfo();
        MetaDataStore metaDataStore = createMetaDataStore(info, classLoaderRegistry, processorRegistry);
        registerSystemComponent(runtimeServices, "MetaDataStore", MetaDataStore.class, metaDataStore);

        Assembly runtimeAssembly = BootstrapAssemblyFactory.createAssembly(runtime);
        registerSystemComponent(runtimeServices, "RuntimeAssembly", Assembly.class, runtimeAssembly);
    }

    private void registerClassLoaders(Fabric3Runtime<?> runtime,
                                      ClassLoader bootClassLoader,
                                      ClassLoader appClassLoader) {

        ClassLoaderRegistry classLoaderRegistry = runtime.getSystemComponent(ClassLoaderRegistry.class, ComponentNames.CLASSLOADER_REGISTRY_URI);
        classLoaderRegistry.register(HOST_CLASSLOADER_ID, runtime.getHostClassLoader());
        classLoaderRegistry.register(BOOT_CLASSLOADER_ID, bootClassLoader);
        classLoaderRegistry.register(RUNTIME_URI, new CompositeClassLoader(RUNTIME_URI, bootClassLoader));

        URI domainId = runtime.getHostInfo().getDomain();
        classLoaderRegistry.register(APPLICATION_CLASSLOADER_ID, appClassLoader);
        classLoaderRegistry.register(domainId, new CompositeClassLoader(domainId, appClassLoader));
    }

    private MetaDataStore createMetaDataStore(HostInfo info,
                                              ClassLoaderRegistry classLoaderRegistry,
                                              ProcessorRegistry processorRegistry) throws InitializationException {
        MetaDataStoreImpl metaDataStore = new MetaDataStoreImpl(info,
                                                                classLoaderRegistry,
                                                                processorRegistry,
                                                                new XStreamFactoryImpl());
        metaDataStore.setPersistent("false");
        try {
            metaDataStore.init();
        } catch (IOException e) {
            throw new InitializationException(e);
        }
        return metaDataStore;
    }

    private <S, I extends S> void registerSystemComponent(RuntimeServices runtime,
                                                          String name,
                                                          Class<S> type,
                                                          I instance)
            throws InitializationException {

        try {
            LogicalComponent<?> logical = createLogicalComponent(name, type, instance);
            AtomicComponent<I> physical = createPhysicalComponent(name, type, instance);
            runtime.registerComponent(logical, physical);
        } catch (InvalidServiceContractException e) {
            throw new InitializationException(e);
        } catch (InstantiationException e) {
            throw new InitializationException(e);
        } catch (RegistrationException e) {
            throw new InitializationException(e);
        }
    }

    protected <S, I extends S> AtomicComponent<I> createPhysicalComponent(String name, Class<S> type, I instance) {
        URI uri = URI.create(domain.getUri() + "/" + name);
        return new SingletonComponent<I>(uri, instance, null);
    }

    protected <S, I extends S> LogicalComponent<Implementation<?>> createLogicalComponent(String name,
                                                                                          Class<S> type,
                                                                                          I instance)
            throws InvalidServiceContractException, InstantiationException {
        ComponentDefinition<Implementation<?>> definition = createDefinition(name, type, instance);

        ComponentInstantiator<Implementation<?>> instantiator = new AtomicComponentInstantiator();
        return instantiator.instantiate(domain, definition);
    }

    protected <S, I extends S> ComponentDefinition<Implementation<?>> createDefinition(String name,
                                                                                       Class<S> type,
                                                                                       I instance)
            throws InvalidServiceContractException {

        String implClassName = instance.getClass().getName();

        JavaServiceContract contract = interfaceProcessorRegistry.introspect(type);
        String serviceName = JavaIntrospectionHelper.getBaseName(contract.getInterfaceName());
        JavaMappedService service = new JavaMappedService(serviceName, contract);

        PojoComponentType componentType = new PojoComponentType(implClassName);
        componentType.add(service);

        SingletonImplementation impl = new SingletonImplementation(componentType, implClassName);

        ComponentDefinition<Implementation<?>> def = new ComponentDefinition<Implementation<?>>(name);
        def.setImplementation(impl);
        return def;
    }


    private Document loadUserConfig() throws ConfigLoadException {
        // Get the user config location
        File configFile = new File(USER_CONFIG);
        if (configFile.exists()) {
            try {
                URL url = configFile.toURI().toURL();
                return configLoader.loadConfig(url);
            } catch (MalformedURLException e) {
                return null;
            }
        } else {
            return null;
        }
    }
}
