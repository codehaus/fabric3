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
package org.fabric3.fabric.runtime;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;

import org.fabric3.fabric.assembly.InstantiationException;
import org.fabric3.fabric.assembly.RuntimeAssembly;
import org.fabric3.fabric.assembly.RuntimeAssemblyImpl;
import org.fabric3.fabric.assembly.allocator.Allocator;
import org.fabric3.fabric.assembly.allocator.LocalAllocator;
import org.fabric3.fabric.assembly.normalizer.PromotionNormalizer;
import org.fabric3.fabric.assembly.normalizer.PromotionNormalizerImpl;
import org.fabric3.fabric.assembly.resolver.DefaultWireResolver;
import org.fabric3.fabric.assembly.resolver.WireResolver;
import org.fabric3.fabric.assembly.store.AssemblyStore;
import org.fabric3.fabric.assembly.store.NonPersistentAssemblyStore;
import org.fabric3.fabric.builder.Connector;
import org.fabric3.fabric.builder.ConnectorImpl;
import org.fabric3.fabric.builder.component.DefaultComponentBuilderRegistry;
import org.fabric3.fabric.builder.component.WireAttacherRegistryImpl;
import org.fabric3.fabric.builder.resource.ResourceContainerBuilderRegistryImpl;
import org.fabric3.fabric.classloader.ClassLoaderBuilder;
import org.fabric3.fabric.classloader.ClassLoaderGeneratorImpl;
import org.fabric3.fabric.command.CommandExecutorRegistryImpl;
import org.fabric3.fabric.command.InitializeComponentCommand;
import org.fabric3.fabric.command.InitializeComponentExecutor;
import org.fabric3.fabric.command.StartCompositeContextCommand;
import org.fabric3.fabric.command.StartCompositeContextExecutor;
import org.fabric3.fabric.command.StartCompositeContextGenerator;
import org.fabric3.fabric.component.GroupInitializationExceptionFormatter;
import org.fabric3.fabric.component.scope.CompositeScopeContainer;
import org.fabric3.fabric.component.scope.ScopeRegistryImpl;
import org.fabric3.fabric.deployer.Deployer;
import org.fabric3.fabric.deployer.DeployerImpl;
import org.fabric3.fabric.generator.GeneratorRegistryImpl;
import org.fabric3.fabric.idl.java.JavaInterfaceProcessorRegistryImpl;
import org.fabric3.fabric.implementation.IntrospectionRegistryImpl;
import org.fabric3.fabric.implementation.processor.ConstructorProcessor;
import org.fabric3.fabric.implementation.processor.DestroyProcessor;
import org.fabric3.fabric.implementation.processor.EagerInitProcessor;
import org.fabric3.fabric.implementation.processor.HeuristicPojoProcessor;
import org.fabric3.fabric.implementation.processor.ImplementationProcessorServiceImpl;
import org.fabric3.fabric.implementation.processor.InitProcessor;
import org.fabric3.fabric.implementation.processor.MonitorProcessor;
import org.fabric3.fabric.implementation.processor.PropertyProcessor;
import org.fabric3.fabric.implementation.processor.ReferenceProcessor;
import org.fabric3.fabric.implementation.processor.ResourceProcessor;
import org.fabric3.fabric.implementation.processor.ScopeProcessor;
import org.fabric3.fabric.implementation.processor.ServiceProcessor;
import org.fabric3.fabric.implementation.singleton.SingletonComponent;
import org.fabric3.fabric.implementation.singleton.SingletonGenerator;
import org.fabric3.fabric.implementation.singleton.SingletonImplementation;
import org.fabric3.fabric.implementation.singleton.SingletonWireAttacher;
import org.fabric3.fabric.implementation.singleton.SingletonWireTargetDefinition;
import org.fabric3.fabric.implementation.system.SystemComponentBuilder;
import org.fabric3.fabric.implementation.system.SystemComponentGenerator;
import org.fabric3.fabric.implementation.system.SystemComponentTypeLoader;
import org.fabric3.fabric.implementation.system.SystemComponentTypeLoaderImpl;
import org.fabric3.fabric.implementation.system.SystemImplementationLoader;
import org.fabric3.fabric.implementation.system.SystemWireAttacher;
import org.fabric3.fabric.loader.LoaderRegistryImpl;
import static org.fabric3.fabric.runtime.ComponentNames.APPLICATION_CLASSLOADER_ID;
import static org.fabric3.fabric.runtime.ComponentNames.BOOT_CLASSLOADER_ID;
import static org.fabric3.fabric.runtime.ComponentNames.CLASSLOADER_REGISTRY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.EXTENSION_METADATA_STORE_URI;
import static org.fabric3.fabric.runtime.ComponentNames.RUNTIME_ASSEMBLY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.RUNTIME_NAME;
import static org.fabric3.fabric.runtime.ComponentNames.RUNTIME_URI;
import static org.fabric3.fabric.runtime.ComponentNames.SCOPE_REGISTRY_URI;
import org.fabric3.fabric.services.advertsiement.FeatureLoader;
import org.fabric3.fabric.services.archive.JarService;
import org.fabric3.fabric.services.archive.JarServiceImpl;
import org.fabric3.fabric.services.classloading.ClassLoaderRegistryImpl;
import org.fabric3.fabric.services.contribution.ArtifactResolverRegistryImpl;
import org.fabric3.fabric.services.contribution.ClasspathProcessorRegistryImpl;
import org.fabric3.fabric.services.contribution.ContributionStoreRegistryImpl;
import org.fabric3.fabric.services.contribution.FileSystemResolver;
import org.fabric3.fabric.services.contribution.MetaDataStoreImpl;
import org.fabric3.fabric.services.contribution.processor.JarClasspathProcessor;
import org.fabric3.fabric.services.instancefactory.BuildHelperImpl;
import org.fabric3.fabric.services.instancefactory.DefaultInstanceFactoryBuilderRegistry;
import org.fabric3.fabric.services.instancefactory.GenerationHelperImpl;
import org.fabric3.fabric.services.instancefactory.ReflectiveInstanceFactoryBuilder;
import org.fabric3.fabric.services.routing.RuntimeRoutingService;
import org.fabric3.fabric.services.xstream.XStreamFactoryImpl;
import org.fabric3.fabric.util.JavaIntrospectionHelper;
import org.fabric3.host.monitor.FormatterRegistry;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.loader.common.ComponentReferenceLoader;
import org.fabric3.loader.common.DefaultPolicyHelper;
import org.fabric3.loader.common.LoaderContextImpl;
import org.fabric3.loader.composite.ComponentLoader;
import org.fabric3.loader.composite.CompositeLoader;
import org.fabric3.loader.composite.IncludeLoader;
import org.fabric3.loader.composite.PropertyValueLoader;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuildHelper;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.processor.ImplementationProcessorService;
import org.fabric3.pojo.processor.IntrospectionRegistry;
import org.fabric3.pojo.scdl.JavaMappedService;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.Autowire;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.assembly.AssemblyException;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.builder.resource.ResourceContainerBuilderRegistry;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.component.ComponentManager;
import org.fabric3.spi.component.RegistrationException;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.deployer.CompositeClassLoader;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.idl.InvalidServiceContractException;
import org.fabric3.spi.idl.java.JavaInterfaceProcessorRegistry;
import org.fabric3.spi.idl.java.JavaServiceContract;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.PolicyHelper;
import org.fabric3.spi.policy.registry.NullPolicyResolver;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.contribution.ArtifactResolverRegistry;
import org.fabric3.spi.services.contribution.ClasspathProcessorRegistry;
import org.fabric3.spi.services.contribution.ContributionStoreRegistry;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.transform.DefaultTransformerRegistry;
import org.fabric3.transform.dom2java.String2Integer;
import org.fabric3.transform.dom2java.String2String;

/**
 * Bootstrapper that initializes a runtime by reading a system SCDL file.
 *
 * @version $Rev$ $Date$
 */
public class ScdlBootstrapperImpl implements ScdlBootstrapper {
    private static final URI COMPONENT_MGR_URI = URI.create(RUNTIME_NAME + "/ComponentManager");
    private static final URI XML_INPUT_FACTORY_URI = URI.create(RUNTIME_NAME + "/XMLInputFactory");
    private static final URI MONITOR_URI = URI.create(RUNTIME_NAME + "/MonitorFactory");
    private static final URI RUNTIME_INFO_URI = URI.create(RUNTIME_NAME + "/HostInfo");
    private static final URI STORE_REGISTRY_URI = URI.create(RUNTIME_NAME + "/StoreRegistrt");
    private static final URI HOST_CLASSLOADER_ID = URI.create("sca://./hostClassLoader");

    private JavaInterfaceProcessorRegistry interfaceProcessorRegistry;

    private ComponentManager componentManager;
    private ScopeRegistry scopeRegistry;
    private MonitorFactory monitorFactory;

    private URL scdlLocation;
    private RuntimeAssembly runtimeAssembly;
    private XMLInputFactory xmlFactory;
    private LoaderRegistry loader;
    private ClassLoaderRegistry classLoaderRegistry;
    private ContributionStoreRegistry contributionStoreRegistry;
    private MetaDataStoreImpl metaDataStore;

    public ScdlBootstrapperImpl() {
    }

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
        try {
            // load the system composite
            ClassLoader bootCl = classLoaderRegistry.getClassLoader(BOOT_CLASSLOADER_ID);
            LoaderContext loaderContext = new LoaderContextImpl(bootCl, scdlLocation);
            Composite composite = loader.load(scdlLocation, Composite.class, loaderContext);

            // include in the runtime domain assembly
            runtimeAssembly.includeInDomain(composite);
        } catch (LoaderException e) {
            throw new InitializationException(e);
        } catch (ActivateException e) {
            throw new InitializationException(e);
        }

    }

    protected void createBootstrapComponents(Fabric3Runtime runtime) throws InitializationException {
        interfaceProcessorRegistry = new JavaInterfaceProcessorRegistryImpl();
        ClassLoader cl = getClass().getClassLoader();
        xmlFactory = XMLInputFactory.newInstance("javax.xml.stream.XMLInputFactory", cl);
        monitorFactory = runtime.getMonitorFactory();
        monitorFactory.register(new GroupInitializationExceptionFormatter(monitorFactory));
        // create the ClassLoaderRegistry
        classLoaderRegistry = new ClassLoaderRegistryImpl();
        componentManager = ((AbstractRuntime<?>) runtime).getComponentManager();
        WireResolver resolver = new DefaultWireResolver();
        scopeRegistry = new ScopeRegistryImpl();
        // create the COMPOSITE ScopeContainer
        CompositeScopeContainer scopeContainer = new CompositeScopeContainer(monitorFactory);
        scopeContainer.setScopeRegistry(scopeRegistry);
        scopeContainer.start();

        IntrospectionRegistry introspector = createIntrospector(interfaceProcessorRegistry);
        loader = createLoader(introspector);
        GeneratorRegistry generatorRegistry = createGeneratorRegistry();
        HostInfo info = runtime.getHostInfo();
        Deployer deployer = createDeployer(info);
        CommandExecutorRegistry commandRegistry = createCommandExecutorRegistry(scopeRegistry);

        contributionStoreRegistry = new ContributionStoreRegistryImpl();
        metaDataStore = new MetaDataStoreImpl(info, contributionStoreRegistry, new XStreamFactoryImpl());
        metaDataStore.setStoreId("extensions");
        metaDataStore.setPersistent("false");
        try {
            metaDataStore.init();
        } catch (IOException e) {
            throw new InitializationException(e);
        }
        RuntimeRoutingService routingService = new RuntimeRoutingService(deployer, commandRegistry, info);
        PromotionNormalizer normalizer = new PromotionNormalizerImpl();
        Allocator allocator = new LocalAllocator();

        // enable autowire for the runtime domain
        AssemblyStore store = new NonPersistentAssemblyStore(ComponentNames.RUNTIME_URI, Autowire.ON);
        runtimeAssembly = new RuntimeAssemblyImpl(generatorRegistry,
                                                  resolver,
                                                  normalizer,
                                                  allocator,
                                                  routingService,
                                                  store,
                                                  metaDataStore);
        try {
            runtimeAssembly.initialize();
        } catch (AssemblyException e) {
            throw new InitializationException(e);
        }
    }

    protected <I extends HostInfo> void registerBootstrapComponents(AbstractRuntime<I> runtime)
            throws InitializationException {
        registerSystemComponent(COMPONENT_MGR_URI, ComponentManager.class, componentManager);
        registerSystemComponent(SCOPE_REGISTRY_URI, ScopeRegistry.class, scopeRegistry);
        registerSystemComponent(RUNTIME_INFO_URI, runtime.getHostInfoType(), runtime.getHostInfo());
        registerSystemComponent(RUNTIME_ASSEMBLY_URI, RuntimeAssembly.class, runtimeAssembly);
        registerSystemComponent(XML_INPUT_FACTORY_URI, XMLInputFactory.class, xmlFactory);
        registerSystemComponent(STORE_REGISTRY_URI, ContributionStoreRegistry.class, contributionStoreRegistry);
        registerSystemComponent(EXTENSION_METADATA_STORE_URI, MetaDataStore.class, metaDataStore);

        // register the MonitorFactory provided by the host
        List<Class<?>> monitorServices = new ArrayList<Class<?>>();
        monitorServices.add(MonitorFactory.class);
        monitorServices.add(FormatterRegistry.class);
        registerSystemComponent(MONITOR_URI, monitorServices, monitorFactory);

        classLoaderRegistry.register(HOST_CLASSLOADER_ID, runtime.getHostClassLoader());
        registerSystemComponent(CLASSLOADER_REGISTRY_URI, ClassLoaderRegistry.class, classLoaderRegistry);

    }

    protected <S, I extends S> void registerSystemComponent(URI uri, Class<S> type, I instance)
            throws InitializationException {
        List<Class<?>> types = new ArrayList<Class<?>>(1);
        types.add(type);
        registerSystemComponent(uri, types, instance);
    }

    protected <I> void registerSystemComponent(URI uri, List<Class<?>> types, I instance)
            throws InitializationException {
        try {
            String name = RUNTIME_URI.relativize(uri).toString();
            Class<?> implClass = instance.getClass();
            List<JavaServiceContract> contracts = createServiceContacts(types);
            ComponentDefinition<SingletonImplementation> definition = createDefinition(name, contracts, implClass);
            SingletonComponent<I> component = new SingletonComponent<I>(uri, contracts, instance, null);

            componentManager.register(component);
            runtimeAssembly.instantiateHostComponentDefinition(definition);
        } catch (InvalidServiceContractException e) {
            throw new InitializationException(e);
        } catch (RegistrationException e) {
            throw new InitializationException(e);
        } catch (InstantiationException e) {
            throw new InitializationException(e);
        }
    }

    protected List<JavaServiceContract> createServiceContacts(List<Class<?>> types)
            throws InvalidServiceContractException {
        List<JavaServiceContract> contracts = new ArrayList<JavaServiceContract>(types.size());
        for (Class<?> type : types) {
            JavaServiceContract contract = interfaceProcessorRegistry.introspect(type);
            contracts.add(contract);
        }
        return contracts;
    }

    protected <I> ComponentDefinition<SingletonImplementation> createDefinition(String name,
                                                                                List<JavaServiceContract> contracts,
                                                                                Class<I> implClass)
            throws InvalidServiceContractException {

        PojoComponentType componentType = new PojoComponentType(implClass.getName());
        for (JavaServiceContract contract : contracts) {
            String serviceName = JavaIntrospectionHelper.getBaseName(contract.getInterfaceName());
            JavaMappedService service = new JavaMappedService(serviceName, contract);
            componentType.add(service);
        }
        SingletonImplementation impl = new SingletonImplementation(componentType, implClass.getName());
        ComponentDefinition<SingletonImplementation> def = new ComponentDefinition<SingletonImplementation>(name);
        def.setImplementation(impl);
        return def;
    }

    protected CommandExecutorRegistry createCommandExecutorRegistry(ScopeRegistry scopeRegistry) {
        CommandExecutorRegistryImpl commandRegistry = new CommandExecutorRegistryImpl();
        StartCompositeContextExecutor executor =
                new StartCompositeContextExecutor(commandRegistry, scopeRegistry, monitorFactory);
        InitializeComponentExecutor initExecutor =
                new InitializeComponentExecutor(null,
                                                null,
                                                commandRegistry,
                                                scopeRegistry,
                                                componentManager,
                                                monitorFactory);
        commandRegistry.register(StartCompositeContextCommand.class, executor);
        commandRegistry.register(InitializeComponentCommand.class, initExecutor);
        return commandRegistry;
    }

    protected IntrospectionRegistry createIntrospector(JavaInterfaceProcessorRegistry registry) {
        ImplementationProcessorService service = new ImplementationProcessorServiceImpl(registry);
        IntrospectionRegistryImpl.Monitor monitor = monitorFactory.getMonitor(IntrospectionRegistryImpl.Monitor.class);
        IntrospectionRegistryImpl introspectionRegistry = new IntrospectionRegistryImpl(monitor);
        introspectionRegistry.registerProcessor(new ConstructorProcessor(service));
        introspectionRegistry.registerProcessor(new DestroyProcessor());
        introspectionRegistry.registerProcessor(new InitProcessor());
        introspectionRegistry.registerProcessor(new EagerInitProcessor());
        introspectionRegistry.registerProcessor(new ScopeProcessor(scopeRegistry));
        introspectionRegistry.registerProcessor(new PropertyProcessor(service));
        introspectionRegistry.registerProcessor(new ReferenceProcessor(registry));
        introspectionRegistry.registerProcessor(new ResourceProcessor());
        introspectionRegistry.registerProcessor(new ServiceProcessor(service));
        introspectionRegistry.registerProcessor(new HeuristicPojoProcessor(service, interfaceProcessorRegistry));
        introspectionRegistry.registerProcessor(new MonitorProcessor(monitorFactory, service));
        return introspectionRegistry;
    }

    protected LoaderRegistry createLoader(IntrospectionRegistry introspector) {
        LoaderRegistry loaderRegistry = new LoaderRegistryImpl(monitorFactory, xmlFactory);

        // register element loaders
        PropertyValueLoader propertyValueLoader = new PropertyValueLoader();
        PolicyHelper policyHelper = new DefaultPolicyHelper();

        ComponentReferenceLoader componentReferenceLoader = new ComponentReferenceLoader(policyHelper);
        ComponentLoader componentLoader = new ComponentLoader(loaderRegistry,
                                                              propertyValueLoader,
                                                              componentReferenceLoader,
                                                              policyHelper);

        IncludeLoader includeLoader = new IncludeLoader(loaderRegistry);
        CompositeLoader compositeLoader = new CompositeLoader(loaderRegistry,
                                                              includeLoader,
                                                              null,
                                                              null,
                                                              null,
                                                              componentLoader,
                                                              null);
        compositeLoader.init();

        SystemComponentTypeLoader typeLoader = new SystemComponentTypeLoaderImpl(introspector);
        SystemImplementationLoader systemImplementationLoader =
                new SystemImplementationLoader(loaderRegistry, typeLoader);
        systemImplementationLoader.start();

        FeatureLoader featureLoader = new FeatureLoader(loaderRegistry, introspector);
        featureLoader.start();

        return loaderRegistry;
    }

    protected GeneratorRegistry createGeneratorRegistry() {
        GeneratorRegistryImpl registry = new GeneratorRegistryImpl();
        registry.setPolicyResolver(new NullPolicyResolver());
        new SystemComponentGenerator(registry, new ClassLoaderGeneratorImpl(), new GenerationHelperImpl());
        new SingletonGenerator(registry);
        StartCompositeContextGenerator contextGenerator = new StartCompositeContextGenerator(registry);
        contextGenerator.init();
        return registry;
    }

    protected DeployerImpl createDeployer(HostInfo info) {
        DeployerImpl deployer = new DeployerImpl(monitorFactory);
        ComponentBuilderRegistry registry = new DefaultComponentBuilderRegistry();

        InstanceFactoryBuilderRegistry providerRegistry = new DefaultInstanceFactoryBuilderRegistry();
        InstanceFactoryBuildHelper buildHelper = new BuildHelperImpl(classLoaderRegistry);
        ReflectiveInstanceFactoryBuilder provider = new ReflectiveInstanceFactoryBuilder(providerRegistry, buildHelper);
        provider.init();

        TransformerRegistry<PullTransformer<?, ?>> transformerRegistry =
                new DefaultTransformerRegistry<PullTransformer<?, ?>>();
        transformerRegistry.register(new String2String());
        transformerRegistry.register(new String2Integer());

        SystemComponentBuilder<?> builder = new SystemComponentBuilder<Object>(registry,
                                                                               scopeRegistry,
                                                                               providerRegistry,
                                                                               classLoaderRegistry,
                                                                               transformerRegistry);
        builder.init();

        WireAttacherRegistry wireAttacherRegistry = new WireAttacherRegistryImpl();
        SingletonWireAttacher<?, ?> singletonWireAttacher = new SingletonWireAttacher();
        wireAttacherRegistry.register(SingletonWireTargetDefinition.class, singletonWireAttacher);
        SystemWireAttacher wireAttacher = new SystemWireAttacher(componentManager, wireAttacherRegistry);
        wireAttacher.init();

        deployer.setBuilderRegistry(registry);
        deployer.setComponentManager(componentManager);
        Connector connector = new ConnectorImpl(null, wireAttacherRegistry);
        deployer.setConnector(connector);
        ResourceContainerBuilderRegistry resourceRegistry = createResourceBuilderRegistry(info);
        deployer.setResourceBuilderRegistry(resourceRegistry);
        return deployer;
    }

    protected ResourceContainerBuilderRegistry createResourceBuilderRegistry(HostInfo info) {
        ResourceContainerBuilderRegistry resourceRegistry = new ResourceContainerBuilderRegistryImpl();
        ArtifactResolverRegistry artifactResolverRegistry = new ArtifactResolverRegistryImpl();
        FileSystemResolver resolver = new FileSystemResolver(artifactResolverRegistry);
        resolver.init();
        ClasspathProcessorRegistry classpathProcessorRegistry = new ClasspathProcessorRegistryImpl();
        JarService jarService = new JarServiceImpl();
        new JarClasspathProcessor(classpathProcessorRegistry, jarService);
        ClassLoaderBuilder clBuilder = new ClassLoaderBuilder(resourceRegistry,
                                                              classLoaderRegistry,
                                                              artifactResolverRegistry,
                                                              classpathProcessorRegistry);
        clBuilder.init();
        return resourceRegistry;
    }

}
