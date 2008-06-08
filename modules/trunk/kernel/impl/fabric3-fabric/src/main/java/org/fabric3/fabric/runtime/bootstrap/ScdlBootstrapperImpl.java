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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.management.MBeanServer;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.fabric3.fabric.instantiator.LogicalInstantiationException;
import org.fabric3.fabric.component.scope.ScopeRegistryImpl;
import org.fabric3.fabric.implementation.singleton.SingletonComponent;
import org.fabric3.fabric.implementation.singleton.SingletonImplementation;
import org.fabric3.fabric.instantiator.component.AtomicComponentInstantiator;
import org.fabric3.fabric.instantiator.component.ComponentInstantiator;
import org.fabric3.fabric.runtime.ComponentNames;
import static org.fabric3.fabric.runtime.ComponentNames.APPLICATION_CLASSLOADER_ID;
import static org.fabric3.fabric.runtime.ComponentNames.BOOT_CLASSLOADER_ID;
import static org.fabric3.fabric.runtime.ComponentNames.RUNTIME_URI;
import org.fabric3.fabric.services.classloading.ClassLoaderRegistryImpl;
import org.fabric3.fabric.services.contribution.ClasspathProcessorRegistryImpl;
import org.fabric3.fabric.services.contribution.MetaDataStoreImpl;
import org.fabric3.fabric.services.contribution.ProcessorRegistryImpl;
import org.fabric3.fabric.services.documentloader.DocumentLoader;
import org.fabric3.fabric.services.documentloader.DocumentLoaderImpl;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ValidationFailure;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.introspection.contract.ContractProcessor;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.introspection.validation.InvalidCompositeException;
import org.fabric3.introspection.xml.Loader;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.monitor.MonitorFactory;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.DefaultValidationContext;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.ValidationContext;
import org.fabric3.services.xmlfactory.XMLFactory;
import org.fabric3.services.xmlfactory.impl.XMLFactoryImpl;
import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.assembly.Assembly;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.runtime.RuntimeServices;
import org.fabric3.spi.services.lcm.LogicalComponentManager;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.services.componentmanager.RegistrationException;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.contribution.ClasspathProcessorRegistry;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.system.introspection.BootstrapLoaderFactory;

/**
 * Bootstrapper that initializes a runtime by reading a system SCDL file.
 *
 * @version $Rev$ $Date$
 */
public class ScdlBootstrapperImpl implements ScdlBootstrapper {

    private static final URI HOST_CLASSLOADER_ID = URI.create("sca://./hostClassLoader");
    private static final String USER_CONFIG = System.getProperty("user.home") + "/.fabric3/config.xml";

    private final XMLFactory xmlFactory;
    private final ContractProcessor interfaceProcessorRegistry;
    private final DocumentLoader documentLoader;
    private final ComponentInstantiator instantiator;

    private URL scdlLocation;
    private URL systemConfig;
    private LogicalCompositeComponent domain;
    private MetaDataStore metaDataStore;
    private ClassLoaderRegistry classLoaderRegistry;
    private ProcessorRegistry processorRegistry;
    private ScopeRegistry scopeRegistry;

    public ScdlBootstrapperImpl(XMLFactory xmlFactory, MetaDataStore metaDataStore) {
        this(xmlFactory);
        this.metaDataStore = metaDataStore;
    }

    public ScdlBootstrapperImpl() {
        this(new XMLFactoryImpl());
        this.metaDataStore = new MetaDataStoreImpl(classLoaderRegistry, processorRegistry);
    }

    private ScdlBootstrapperImpl(XMLFactory xmlFactory) {
        this.xmlFactory = xmlFactory;
        classLoaderRegistry = new ClassLoaderRegistryImpl();
        processorRegistry = new ProcessorRegistryImpl();
        scopeRegistry = new ScopeRegistryImpl();
        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        interfaceProcessorRegistry = new DefaultContractProcessor(helper);
        documentLoader = new DocumentLoaderImpl();
        instantiator = new AtomicComponentInstantiator(documentLoader);
    }

    public URL getScdlLocation() {
        return scdlLocation;
    }

    public void setScdlLocation(URL scdlLocation) {
        this.scdlLocation = scdlLocation;
    }

    public void setSystemConfig(URL systemConfig) {
        this.systemConfig = systemConfig;
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

    private LogicalCompositeComponent getDomain(Fabric3Runtime<?> runtime) {
        RuntimeServices runtimeServices = (RuntimeServices) runtime;
        LogicalComponentManager logicalComponentManager = runtimeServices.getLogicalComponentManager();
        return logicalComponentManager.getDomain();
    }

    public void bootSystem(Fabric3Runtime<?> runtime) throws InitializationException {
        ClassLoaderRegistry classLoaderRegistry =
                runtime.getSystemComponent(ClassLoaderRegistry.class, ComponentNames.CLASSLOADER_REGISTRY_URI);
        Loader loader = BootstrapLoaderFactory.createLoader(runtime.getMonitorFactory(), xmlFactory);
        Assembly runtimeAssembly = runtime.getSystemComponent(Assembly.class, ComponentNames.RUNTIME_ASSEMBLY_URI);
        try {

            // load the system composite
            ClassLoader bootCl = classLoaderRegistry.getClassLoader(BOOT_CLASSLOADER_ID);
            IntrospectionContext introspectionContext = new DefaultIntrospectionContext(bootCl, BOOT_CLASSLOADER_ID, scdlLocation);
            Composite composite = loader.load(scdlLocation, Composite.class, introspectionContext);
            composite.validate(introspectionContext);
            if (introspectionContext.hasErrors()) {
                QName name = composite.getName();
                List<ValidationFailure> errors = introspectionContext.getErrors();
                List<ValidationFailure> warnings = introspectionContext.getWarnings();
                throw new InvalidCompositeException(name, errors, warnings);
            }

            Document userConfig = loadUserConfig();
            if (userConfig != null) {
                domain.setPropertyValue("userConfig", userConfig);
            }

            Document systemConfig = loadSystemConfig();
            if (systemConfig != null) {
                domain.setPropertyValue("systemConfig", systemConfig);
            }

            // include in the runtime domain assembly
            runtimeAssembly.includeInDomain(composite);

        } catch (ContributionException e) {
            throw new InitializationException(e);
        } catch (ActivateException e) {
            throw new InitializationException(e);
        } catch (LoaderException e) {
            throw new InitializationException(e);
        }

    }

    private <T extends HostInfo> void registerRuntimeComponents(Fabric3Runtime<T> runtime) throws InitializationException {
        RuntimeServices runtimeServices = (RuntimeServices) runtime;

        // services available through the outward facing Fabric3Runtime API
        registerSystemComponent(runtimeServices, "MonitorFactory", MonitorFactory.class, runtime.getMonitorFactory());
        registerSystemComponent(runtimeServices, "HostInfo", runtime.getHostInfoType(), runtime.getHostInfo());
        MBeanServer mbServer = runtime.getMBeanServer();
        if (mbServer != null) {
            registerSystemComponent(runtimeServices, "MBeanServer", MBeanServer.class, mbServer);
        }

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

        registerSystemComponent(runtimeServices, "ClassLoaderRegistry", ClassLoaderRegistry.class, classLoaderRegistry);

        scopeRegistry.register(runtimeServices.getScopeContainer());
        registerSystemComponent(runtimeServices, "ScopeRegistry", ScopeRegistry.class, scopeRegistry);

        registerSystemComponent(runtimeServices, "ContributionProcessorRegistry", ProcessorRegistry.class, processorRegistry);

        if (metaDataStore != null) {
            registerSystemComponent(runtimeServices, "MetaDataStore", MetaDataStore.class, metaDataStore);
        }

        ClasspathProcessorRegistryImpl instance = new ClasspathProcessorRegistryImpl();
        registerSystemComponent(runtimeServices, "ClasspathProcessorRegistry", ClasspathProcessorRegistry.class, instance);

        Assembly runtimeAssembly = BootstrapAssemblyFactory.createAssembly(runtime);
        registerSystemComponent(runtimeServices, "RuntimeAssembly", Assembly.class, runtimeAssembly);
    }

    private void registerClassLoaders(Fabric3Runtime<?> runtime,
                                      ClassLoader bootClassLoader,
                                      ClassLoader appClassLoader) {

        ClassLoaderRegistry classLoaderRegistry =
                runtime.getSystemComponent(ClassLoaderRegistry.class, ComponentNames.CLASSLOADER_REGISTRY_URI);
        classLoaderRegistry.register(HOST_CLASSLOADER_ID, runtime.getHostClassLoader());
        classLoaderRegistry.register(BOOT_CLASSLOADER_ID, bootClassLoader);
        classLoaderRegistry.register(RUNTIME_URI, new MultiParentClassLoader(RUNTIME_URI, bootClassLoader));

        URI domainId = runtime.getHostInfo().getDomain();
        classLoaderRegistry.register(APPLICATION_CLASSLOADER_ID, appClassLoader);
        classLoaderRegistry.register(domainId, new MultiParentClassLoader(domainId, appClassLoader));
    }

    private <S, I extends S> void registerSystemComponent(RuntimeServices runtime,
                                                          String name,
                                                          Class<S> type,
                                                          I instance)
            throws InitializationException {

        try {
            LogicalComponent<?> logical = createLogicalComponent(name, type, instance);
            AtomicComponent<I> physical = createPhysicalComponent(name, instance);
            runtime.registerComponent(logical, physical);
        } catch (LogicalInstantiationException e) {
            throw new InitializationException(e);
        } catch (RegistrationException e) {
            throw new InitializationException(e);
        }
    }

    protected <I> AtomicComponent<I> createPhysicalComponent(String name, I instance) {
        URI uri = URI.create(domain.getUri() + "/" + name);
        return new SingletonComponent<I>(uri, instance, null);
    }

    protected <S, I extends S> LogicalComponent<Implementation<?>> createLogicalComponent(String name,
                                                                                          Class<S> type,
                                                                                          I instance)
            throws InvalidSystemServiceContractException, LogicalInstantiationException {

        ComponentDefinition<Implementation<?>> definition = createDefinition(name, type, instance);
        return instantiator.instantiate(domain, domain.getPropertyValues(), definition);
    }

    protected <S, I extends S> ComponentDefinition<Implementation<?>> createDefinition(String name, Class<S> type, I instance)
            throws InvalidSystemServiceContractException {

        String implClassName = instance.getClass().getName();

        ValidationContext context = new DefaultValidationContext();
        ServiceContract<?> contract = interfaceProcessorRegistry.introspect(new TypeMapping(), type, context);
        if (context.hasErrors()) {
            throw new InvalidSystemServiceContractException(context.getErrors());
        }
        String serviceName = contract.getInterfaceName();
        ServiceDefinition service = new ServiceDefinition(serviceName, contract);

        PojoComponentType componentType = new PojoComponentType(implClassName);
        componentType.add(service);

        SingletonImplementation impl = new SingletonImplementation(componentType, implClassName);

        ComponentDefinition<Implementation<?>> def = new ComponentDefinition<Implementation<?>>(name);
        def.setImplementation(impl);
        return def;
    }


    private Document loadUserConfig() {
        // Get the user config location
        File configFile = new File(USER_CONFIG);
        try {
            return documentLoader.load(configFile);
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        } catch (SAXException e) {
            return null;
        }
    }


    private Document loadSystemConfig() {
        // Get the system config location
        if (systemConfig == null) {
            return null;
        }
        try {
            return documentLoader.load(systemConfig);
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        } catch (SAXException e) {
            return null;
        }
    }
}
