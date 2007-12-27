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
import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.fabric3.fabric.assembly.InstantiationException;
import org.fabric3.fabric.assembly.RuntimeAssembly;
import org.fabric3.fabric.component.scope.ScopeRegistryImpl;
import org.fabric3.fabric.idl.java.JavaInterfaceProcessorRegistryImpl;
import org.fabric3.fabric.implementation.singleton.SingletonComponent;
import org.fabric3.fabric.implementation.singleton.SingletonImplementation;
import org.fabric3.fabric.runtime.AbstractRuntime;
import static org.fabric3.fabric.runtime.ComponentNames.APPLICATION_CLASSLOADER_ID;
import static org.fabric3.fabric.runtime.ComponentNames.BOOT_CLASSLOADER_ID;
import static org.fabric3.fabric.runtime.ComponentNames.CLASSLOADER_REGISTRY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.METADATA_STORE_URI;
import static org.fabric3.fabric.runtime.ComponentNames.PROCESSOR_REGISTY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.RUNTIME_ASSEMBLY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.RUNTIME_NAME;
import static org.fabric3.fabric.runtime.ComponentNames.RUNTIME_URI;
import static org.fabric3.fabric.runtime.ComponentNames.SCOPE_REGISTRY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.XML_FACTORY_URI;
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
import org.fabric3.scdl.Property;
import org.fabric3.spi.Constants;
import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.deployer.CompositeClassLoader;
import org.fabric3.spi.idl.InvalidServiceContractException;
import org.fabric3.spi.idl.java.JavaInterfaceProcessorRegistry;
import org.fabric3.spi.idl.java.JavaServiceContract;
import org.fabric3.spi.loader.Loader;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
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

    private static final URI COMPONENT_MGR_URI = URI.create(RUNTIME_NAME + "/ComponentManager");
    private static final URI MONITOR_URI = URI.create(RUNTIME_NAME + "/MonitorFactory");
    private static final URI FORMATTER_REGISTRY_URI = URI.create(RUNTIME_NAME + "/FormatterRegistry");
    private static final URI RUNTIME_INFO_URI = URI.create(RUNTIME_NAME + "/HostInfo");
    private static final URI HOST_CLASSLOADER_ID = URI.create("sca://./hostClassLoader");
    private static final String USER_CONFIG = System.getProperty("user.home") + "/.fabric3/config.xml";

    private JavaInterfaceProcessorRegistry interfaceProcessorRegistry;

    private LogicalComponentManager logicalComponentManager;
    private ComponentManager componentManager;
    private ScopeContainer<?> scopeContainer;

    private ScopeRegistry scopeRegistry;
    private MonitorFactory monitorFactory;

    private URL scdlLocation;
    private RuntimeAssembly runtimeAssembly;
    private ClassLoaderRegistry classLoaderRegistry;
    private MetaDataStore metaDataStore;
    private XMLFactory xmlFactory;
    private ConfigLoader configLoader;
    private URL userConfigLocation;
    private ProcessorRegistry processorRegistry;

    public URL getScdlLocation() {
        return scdlLocation;
    }

    public void setScdlLocation(URL scdlLocation) {
        this.scdlLocation = scdlLocation;
    }

    public void bootPrimordial(Fabric3Runtime<?> runtime, ClassLoader bootClassLoader, ClassLoader appClassLoader)
            throws InitializationException {

        createBootstrapComponents(runtime);
        registerBootstrapComponents((AbstractRuntime<?>) runtime);

        classLoaderRegistry.register(BOOT_CLASSLOADER_ID, bootClassLoader);
        classLoaderRegistry.register(RUNTIME_URI, new CompositeClassLoader(RUNTIME_URI, bootClassLoader));

        URI domainId = runtime.getHostInfo().getDomain();
        classLoaderRegistry.register(APPLICATION_CLASSLOADER_ID, appClassLoader);
        classLoaderRegistry.register(domainId, new CompositeClassLoader(domainId, appClassLoader));

    }

    public void bootSystem(Fabric3Runtime<?> runtime) throws InitializationException {
        Loader loader = createLoader();
        try {

            // load the system composite
            ClassLoader bootCl = classLoaderRegistry.getClassLoader(BOOT_CLASSLOADER_ID);
            LoaderContext loaderContext = new LoaderContextImpl(bootCl, BOOT_CLASSLOADER_ID, scdlLocation);
            Composite composite = loader.load(scdlLocation, Composite.class, loaderContext);

            setUserConfigProperty(composite);

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

    private void createBootstrapComponents(Fabric3Runtime<?> runtime) throws InitializationException {

        monitorFactory = runtime.getMonitorFactory();
        HostInfo info = runtime.getHostInfo();

        RuntimeServices runtimeServices = (RuntimeServices) runtime;
        logicalComponentManager = runtimeServices.getLogicalComponentManager();
        componentManager = runtimeServices.getComponentManager();
        scopeContainer = runtimeServices.getScopeContainer();

        xmlFactory = new XMLFactoryImpl();
        interfaceProcessorRegistry = new JavaInterfaceProcessorRegistryImpl();
        configLoader = new DefaultConfigLoader();

        getUserConfig();

        // create the ClassLoaderRegistry
        classLoaderRegistry = new ClassLoaderRegistryImpl();
        scopeRegistry = new ScopeRegistryImpl();
        scopeRegistry.register(scopeContainer);

        processorRegistry = createProcessorRegistry();
        metaDataStore = createMetaDataStore(info);
        runtimeAssembly = createRuntimeAssembly(scopeRegistry);
    }

    private ProcessorRegistryImpl createProcessorRegistry() {
        return new ProcessorRegistryImpl();
    }

    private MetaDataStore createMetaDataStore(HostInfo info) throws InitializationException {
        MetaDataStoreImpl metaDataStore = new MetaDataStoreImpl(info, classLoaderRegistry,
                                                                processorRegistry, new XStreamFactoryImpl());
        metaDataStore.setPersistent("false");
        try {
            metaDataStore.init();
        } catch (IOException e) {
            throw new InitializationException(e);
        }
        return metaDataStore;
    }

    private Loader createLoader() {
        return BootstrapLoaderFactory.createLoader(monitorFactory, xmlFactory, metaDataStore);
    }

    private RuntimeAssembly createRuntimeAssembly(ScopeRegistry scopeRegistry) throws InitializationException {
        return BootstrapAssemblyFactory.createAssembly(monitorFactory, classLoaderRegistry, scopeRegistry, componentManager, logicalComponentManager, metaDataStore);
    }

    private <I extends HostInfo> void registerBootstrapComponents(AbstractRuntime<I> runtime)
            throws InitializationException {

        registerSystemComponent(COMPONENT_MGR_URI, ComponentManager.class, componentManager);
        registerSystemComponent(SCOPE_REGISTRY_URI, ScopeRegistry.class, scopeRegistry);
        registerSystemComponent(RUNTIME_INFO_URI, runtime.getHostInfoType(), runtime.getHostInfo());
        registerSystemComponent(RUNTIME_ASSEMBLY_URI, RuntimeAssembly.class, runtimeAssembly);
        registerSystemComponent(METADATA_STORE_URI, MetaDataStore.class, metaDataStore);
        registerSystemComponent(PROCESSOR_REGISTY_URI, ProcessorRegistry.class, processorRegistry);

        registerSystemComponent(MONITOR_URI, MonitorFactory.class, monitorFactory);
        registerSystemComponent(FORMATTER_REGISTRY_URI, FormatterRegistry.class, new DefaultFormatterRegistry());
        registerSystemComponent(XML_FACTORY_URI, XMLFactory.class, xmlFactory);

        classLoaderRegistry.register(HOST_CLASSLOADER_ID, runtime.getHostClassLoader());
        registerSystemComponent(CLASSLOADER_REGISTRY_URI, ClassLoaderRegistry.class, classLoaderRegistry);

    }

    private <S, I extends S> void registerSystemComponent(URI uri, Class<S> type, I instance)
            throws InitializationException {

        try {
            JavaServiceContract contract = interfaceProcessorRegistry.introspect(type);

            String name = RUNTIME_URI.relativize(uri).toString();
            Class<?> implClass = instance.getClass();
            ComponentDefinition<SingletonImplementation> definition = createDefinition(name, contract, implClass);
            SingletonComponent<I> component = new SingletonComponent<I>(uri, instance, null);

            runtimeAssembly.instantiateHostComponentDefinition(definition);
            componentManager.register(component);
            scopeContainer.register(component);
        } catch (InvalidServiceContractException e) {
            throw new InitializationException(e);
        } catch (RegistrationException e) {
            throw new InitializationException(e);
        } catch (InstantiationException e) {
            throw new InitializationException(e);
        } catch (ActivateException e) {
            throw new InitializationException(e);
        }

    }

    private <I> ComponentDefinition<SingletonImplementation> createDefinition(String name,
                                                                              JavaServiceContract contract,
                                                                              Class<I> implClass)
            throws InvalidServiceContractException {

        PojoComponentType componentType = new PojoComponentType(implClass.getName());
        String serviceName = JavaIntrospectionHelper.getBaseName(contract.getInterfaceName());
        JavaMappedService service = new JavaMappedService(serviceName, contract);
        componentType.add(service);

        SingletonImplementation impl = new SingletonImplementation(componentType, implClass.getName());
        ComponentDefinition<SingletonImplementation> def = new ComponentDefinition<SingletonImplementation>(name);
        def.setImplementation(impl);

        return def;

    }

    private void getUserConfig() throws InitializationException {

        // Get the user config location
        File configFile = new File(USER_CONFIG);
        if (configFile.exists()) {
            try {
                userConfigLocation = configFile.toURL();
            } catch (MalformedURLException e) {
                throw new InitializationException(e);
            }
        }

    }

    private void setUserConfigProperty(Composite composite) throws ConfigLoadException {

        if (userConfigLocation != null) {

            Document userConfig = configLoader.loadConfig(userConfigLocation);

            Property<Document> userConfigProperty = new Property<Document>();
            userConfigProperty.setName("userConfig");
            userConfigProperty.setJavaType(Document.class);
            userConfigProperty.setXmlType(new QName(Constants.FABRIC3_NS, "userConfig"));
            userConfigProperty.setDefaultValue(userConfig);

            composite.add(userConfigProperty);

        }

    }

}
