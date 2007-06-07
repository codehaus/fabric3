package org.fabric3.fabric.runtime;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;

import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.fabric.assembly.ActivateException;
import org.fabric3.fabric.assembly.AssemblyException;
import org.fabric3.fabric.assembly.DistributedAssembly;
import org.fabric3.fabric.assembly.InstantiationException;
import org.fabric3.fabric.assembly.RuntimeAssembly;
import org.fabric3.fabric.assembly.RuntimeAssemblyImpl;
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
import org.fabric3.fabric.command.CommandExecutorRegistryImpl;
import org.fabric3.fabric.command.StartCompositeContextCommand;
import org.fabric3.fabric.command.StartCompositeContextExecutor;
import org.fabric3.fabric.command.StartCompositeContextGenerator;
import org.fabric3.fabric.component.GroupInitializationExceptionFormatter;
import org.fabric3.fabric.component.instancefactory.IFProviderBuilderRegistry;
import org.fabric3.fabric.component.instancefactory.impl.DefaultIFProviderBuilderRegistry;
import org.fabric3.fabric.component.instancefactory.impl.ReflectiveIFProviderBuilder;
import org.fabric3.fabric.component.scope.CompositeScopeContainer;
import org.fabric3.fabric.component.scope.ScopeRegistryImpl;
import org.fabric3.fabric.deployer.Deployer;
import org.fabric3.fabric.deployer.DeployerImpl;
import org.fabric3.fabric.generator.GeneratorRegistryImpl;
import org.fabric3.fabric.idl.java.InterfaceJavaLoader;
import org.fabric3.fabric.idl.java.JavaInterfaceProcessorRegistryImpl;
import org.fabric3.fabric.implementation.IntrospectionRegistryImpl;
import org.fabric3.fabric.implementation.composite.CompositeComponentTypeLoader;
import org.fabric3.fabric.implementation.composite.CompositeLoader;
import org.fabric3.fabric.implementation.pojo.HelperImpl;
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
import org.fabric3.fabric.implementation.system.SystemImplementation;
import org.fabric3.fabric.implementation.system.SystemImplementationLoader;
import org.fabric3.fabric.loader.ComponentLoader;
import org.fabric3.fabric.loader.ComponentTypeElementLoader;
import org.fabric3.fabric.loader.IncludeLoader;
import org.fabric3.fabric.loader.LoaderContextImpl;
import org.fabric3.fabric.loader.LoaderRegistryImpl;
import org.fabric3.fabric.loader.PropertyLoader;
import org.fabric3.fabric.loader.ReferenceLoader;
import org.fabric3.fabric.loader.ServiceLoader;
import org.fabric3.fabric.marshaller.MarshallerLoader;
import static org.fabric3.fabric.runtime.ComponentNames.CLASSLOADER_REGISTRY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.DISTRIBUTED_ASSEMBLY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.RUNTIME_ASSEMBLY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.RUNTIME_NAME;
import static org.fabric3.fabric.runtime.ComponentNames.SCOPE_REGISTRY_URI;
import org.fabric3.fabric.services.classloading.ClassLoaderRegistryImpl;
import org.fabric3.fabric.services.routing.RuntimeRoutingService;
import org.fabric3.fabric.util.JavaIntrospectionHelper;
import org.fabric3.host.monitor.FormatterRegistry;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.component.ComponentManager;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.RegistrationException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.idl.InvalidServiceContractException;
import org.fabric3.spi.idl.java.JavaInterfaceProcessorRegistry;
import org.fabric3.spi.idl.java.JavaServiceContract;
import org.fabric3.spi.implementation.java.ImplementationProcessorService;
import org.fabric3.spi.implementation.java.Introspector;
import org.fabric3.spi.implementation.java.JavaMappedService;
import org.fabric3.spi.implementation.java.PojoComponentType;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.CompositeImplementation;
import org.fabric3.spi.model.type.Scope;
import org.fabric3.spi.model.type.ServiceContract;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
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
    private static final URI WIRE_RESOLVER_URI = URI.create(RUNTIME_NAME + "/WireResolver");
    private static final URI COMPONENT_MGR_URI = URI.create(RUNTIME_NAME + "/ComponentManager");
    private static final URI XML_INPUT_FACTORY_URI = URI.create(RUNTIME_NAME + "/XMLInputFactory");
    private static final URI MONITOR_URI = URI.create(RUNTIME_NAME + "/MonitorFactory");
    private static final URI RUNTIME_INFO_URI = URI.create(RUNTIME_NAME + "/HostInfo");
    private static final URI HOST_CLASSLOADER_ID = URI.create("sca://./hostClassLoader");
    private static final URI BOOT_CLASSLOADER_ID = URI.create("sca://./bootClassLoader");
    private static final URI APPLICATION_CLASSLOADER_ID = URI.create("sca://./applicationClassLoader");

    private JavaInterfaceProcessorRegistry interfaceProcessorRegistry;

    private ComponentManager componentManager;
    private WireResolver resolver;
    private ScopeRegistry scopeRegistry;
    private MonitorFactory monitorFactory;

    private URL scdlLocation;
    private RuntimeAssembly runtimeAssembly;
    private XMLInputFactory xmlFactory;
    private LoaderRegistry loader;
    private ClassLoaderRegistry classLoaderRegistry;

    public ScdlBootstrapperImpl() {
    }

    public URL getScdlLocation() {
        return scdlLocation;
    }

    public void setScdlLocation(URL scdlLocation) {
        this.scdlLocation = scdlLocation;
    }

    public void bootstrap(Fabric3Runtime<?> runtime, ClassLoader bootClassLoader, ClassLoader applicationClassLoader)
            throws InitializationException {
        initializeRuntime(runtime);
        createBootstrapComponents(runtime);
        registerBootstrapComponents((AbstractRuntime<?>) runtime);
        classLoaderRegistry.register(BOOT_CLASSLOADER_ID, bootClassLoader);
        classLoaderRegistry.register(APPLICATION_CLASSLOADER_ID, applicationClassLoader);
        try {
            // FIXME come up with a better alternative: the domain context needs to be started so system services such 
            // as the contribution service that operate on it may be initialized as system components
            startDomainContext(runtime);
            deploySystemScdl((AbstractRuntime<?>) runtime);
            recoverDomain(runtime);
        } catch (GroupInitializationException e) {
            throw new InitializationException(e);
        } catch (AssemblyException e) {
            throw new InitializationException(e);
        }
    }

    protected void recoverDomain(Fabric3Runtime<?> runtime) throws AssemblyException {
        DistributedAssembly assembly =
                runtime.getSystemComponent(DistributedAssembly.class, DISTRIBUTED_ASSEMBLY_URI);
        assembly.initialize();
    }

    protected void startDomainContext(Fabric3Runtime runtime) throws GroupInitializationException {
        // start the domain context
        ScopeContainer<URI> container = scopeRegistry.getScopeContainer(Scope.COMPOSITE);
        WorkContext workContext = new SimpleWorkContext();
        URI domainUri = runtime.getHostInfo().getDomain();
        URI groupId = URI.create(domainUri.toString() + "/");
        workContext.setScopeIdentifier(Scope.COMPOSITE, groupId);
        container.startContext(workContext, groupId);
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
        resolver = new DefaultWireResolver();
        scopeRegistry = new ScopeRegistryImpl();
        // create and register COMPOSITE ScopeContainer
        CompositeScopeContainer scopeContainer = new CompositeScopeContainer(monitorFactory);
        scopeContainer.setScopeRegistry(scopeRegistry);
        scopeContainer.start();

        Introspector introspector = createIntrospector(interfaceProcessorRegistry);
        loader = createLoader(introspector);
        GeneratorRegistry generatorRegistry = createGeneratorRegistry();
        Deployer deployer = createFederatedDeployer();
        CommandExecutorRegistry commandRegistry = createCommandExecutorRegistry(scopeRegistry);
        HostInfo info = runtime.getHostInfo();
        RuntimeRoutingService routingService = new RuntimeRoutingService(deployer, commandRegistry, info);
        PromotionNormalizer normalizer = new PromotionNormalizerImpl();
        AssemblyStore store = new NonPersistentAssemblyStore(info);
        runtimeAssembly =
                new RuntimeAssemblyImpl(generatorRegistry, resolver, normalizer, routingService, store);
        try {
            runtimeAssembly.initialize();
        } catch (AssemblyException e) {
            throw new InitializationException(e);
        }
    }

    protected void initializeRuntime(Fabric3Runtime runtime) throws InitializationException {
        runtime.initialize();
    }

    protected <I extends HostInfo> void registerBootstrapComponents(AbstractRuntime<I> runtime)
            throws InitializationException {
        registerSystemComponent(COMPONENT_MGR_URI, ComponentManager.class, componentManager);
        registerSystemComponent(WIRE_RESOLVER_URI, WireResolver.class, resolver);
        registerSystemComponent(SCOPE_REGISTRY_URI, ScopeRegistry.class, scopeRegistry);
        registerSystemComponent(RUNTIME_INFO_URI, runtime.getHostInfoType(), runtime.getHostInfo());
        registerSystemComponent(RUNTIME_ASSEMBLY_URI, RuntimeAssembly.class, runtimeAssembly);
        registerSystemComponent(XML_INPUT_FACTORY_URI, XMLInputFactory.class, xmlFactory);

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
        try {
            JavaServiceContract<S> contract = interfaceProcessorRegistry.introspect(type);
            List<JavaServiceContract<?>> contracts = new ArrayList<JavaServiceContract<?>>();
            contracts.add(contract);
            SingletonComponent<I> component =
                    new SingletonComponent<I>(uri, contracts, instance);
            componentManager.register(component);
            resolver.addHostUri(contract, uri);
            String serviceName = "#" + JavaIntrospectionHelper.getBaseName(type);
            ComponentDefinition<?> definition = createDefinition(uri,
                                                                 uri.resolve(serviceName),
                                                                 ComponentManager.class,
                                                                 componentManager.getClass());
            runtimeAssembly.instantiateHostComponentDefinition(uri, definition);
        } catch (RegistrationException e) {
            throw new InitializationException(e);
        } catch (InvalidServiceContractException e) {
            throw new InitializationException(e);
        } catch (org.fabric3.fabric.assembly.InstantiationException e) {
            throw new InitializationException(e);
        }
    }

    protected <I> void registerSystemComponent(URI uri, List<Class<?>> types, I instance)
            throws InitializationException {
        try {
            List<JavaServiceContract<?>> contracts = new ArrayList<JavaServiceContract<?>>(types.size());
            for (Class<?> type : types) {
                contracts.add(interfaceProcessorRegistry.introspect(type));

            }
            SingletonComponent<I> component =
                    new SingletonComponent<I>(uri, contracts, instance);
            componentManager.register(component);
            for (ServiceContract<?> contract : component.getServiceContracts()) {
                resolver.addHostUri(contract, uri);
            }
            List<URI> serviceNames = new ArrayList<URI>();
            for (Class<?> type : types) {
                String serviceName = "#" + JavaIntrospectionHelper.getBaseName(type);
                serviceNames.add(uri.resolve(serviceName));
            }
            ComponentDefinition<?> definition = createDefinition(uri,
                                                                 serviceNames,
                                                                 types,
                                                                 instance.getClass());
            runtimeAssembly.instantiateHostComponentDefinition(uri, definition);
        } catch (RegistrationException e) {
            throw new InitializationException(e);
        } catch (InvalidServiceContractException e) {
            throw new InitializationException(e);
        } catch (InstantiationException e) {
            throw new InitializationException(e);
        }
    }

    protected void deploySystemScdl(AbstractRuntime<?> runtime) throws InitializationException {
        CompositeImplementation impl = new CompositeImplementation();
        impl.setScdlLocation(scdlLocation);
        impl.setClassLoader(classLoaderRegistry.getClassLoader(BOOT_CLASSLOADER_ID));
        ComponentDefinition<CompositeImplementation> definition =
                new ComponentDefinition<CompositeImplementation>("main", impl);
        try {
            LoaderContext loaderContext =
                    new LoaderContextImpl(null, null);
            // load system extensions
            loader.loadComponentType(impl, loaderContext);
            runtimeAssembly.activate(definition, false);

        } catch (LoaderException e) {
            throw new InitializationException(e);
        } catch (ActivateException e) {
            throw new InitializationException(e);
        }
    }

    protected CommandExecutorRegistry createCommandExecutorRegistry(ScopeRegistry scopeRegistry) {
        CommandExecutorRegistryImpl commandRegistry = new CommandExecutorRegistryImpl();
        StartCompositeContextExecutor executor =
                new StartCompositeContextExecutor(commandRegistry, scopeRegistry, monitorFactory);
        commandRegistry.register(StartCompositeContextCommand.class, executor);
        return commandRegistry;
    }

    protected Introspector createIntrospector(JavaInterfaceProcessorRegistry registry) {
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
        introspectionRegistry.registerProcessor(new HeuristicPojoProcessor(service));
        introspectionRegistry.registerProcessor(new MonitorProcessor(monitorFactory, service));
        return introspectionRegistry;
    }

    protected LoaderRegistry createLoader(Introspector introspector) {
        LoaderRegistryImpl loaderRegistry = new LoaderRegistryImpl(monitorFactory, xmlFactory);

        // register component type loaders
        SystemComponentTypeLoader componentTypeLoader = new SystemComponentTypeLoader(introspector);
        loaderRegistry.registerLoader(SystemImplementation.class, componentTypeLoader);
        CompositeComponentTypeLoader compositeTypeLoader = new CompositeComponentTypeLoader(loaderRegistry);
        loaderRegistry.registerLoader(CompositeImplementation.class, compositeTypeLoader);

        // register element loaders
        registerLoader(loaderRegistry, new ComponentLoader(loaderRegistry));
        registerLoader(loaderRegistry, new ComponentTypeElementLoader(loaderRegistry));
        registerLoader(loaderRegistry, new CompositeLoader(loaderRegistry, null));
        registerLoader(loaderRegistry, new IncludeLoader(loaderRegistry));
        registerLoader(loaderRegistry, new InterfaceJavaLoader(loaderRegistry, interfaceProcessorRegistry));
        registerLoader(loaderRegistry, new PropertyLoader(loaderRegistry));
        registerLoader(loaderRegistry, new ReferenceLoader(loaderRegistry));
        registerLoader(loaderRegistry, new ServiceLoader(loaderRegistry));
        registerLoader(loaderRegistry, new SystemImplementationLoader(loaderRegistry, componentTypeLoader));
        registerLoader(loaderRegistry, new MarshallerLoader(loaderRegistry, introspector));
        return loaderRegistry;
    }

    protected void registerLoader(LoaderRegistry registry, LoaderExtension<?, ?> loader) {
        registry.registerLoader(loader.getXMLType(), loader);
    }

    protected GeneratorRegistry createGeneratorRegistry() {
        GeneratorRegistry registry = new GeneratorRegistryImpl();
        new SystemComponentGenerator(registry, new HelperImpl());
        new SingletonGenerator(registry);
        StartCompositeContextGenerator contextGenerator = new StartCompositeContextGenerator(registry);
        contextGenerator.init();
        return registry;
    }

    protected ComponentDefinition<SingletonImplementation> createDefinition(URI componentUri,
                                                                            URI serviceName,
                                                                            Class<?> serviceClass,
                                                                            Class<?> implClass)
            throws InvalidServiceContractException {
        PojoComponentType type =
                new PojoComponentType(implClass);
        JavaServiceContract contract = interfaceProcessorRegistry.introspect(serviceClass);
        JavaMappedService service = new JavaMappedService(serviceName, contract, false);
        type.add(service);
        SingletonImplementation implementation = new SingletonImplementation(type, implClass);
        return new ComponentDefinition<SingletonImplementation>(componentUri.toString(), implementation);
    }

    protected ComponentDefinition<SingletonImplementation> createDefinition(URI componentUri,
                                                                            List<URI> serviceNames,
                                                                            List<Class<?>> serviceClasses,
                                                                            Class<?> implClass)
            throws InvalidServiceContractException {
        PojoComponentType type =
                new PojoComponentType(implClass);
        int i = 0;
        for (URI serviceName : serviceNames) {
            Class<?> serviceClass = serviceClasses.get(i);
            JavaServiceContract contract = interfaceProcessorRegistry.introspect(serviceClass);
            JavaMappedService service = new JavaMappedService(serviceName, contract, false);
            type.add(service);
            i++;
        }
        SingletonImplementation implementation = new SingletonImplementation(type, implClass);
        return new ComponentDefinition<SingletonImplementation>(componentUri.toString(), implementation);
    }

    protected DeployerImpl createFederatedDeployer() {
        DeployerImpl deployer = new DeployerImpl();
        ComponentBuilderRegistry registry = new DefaultComponentBuilderRegistry();

        WireAttacherRegistry wireAttacherRegistry = new WireAttacherRegistryImpl();
        SingletonWireAttacher<?, ?> singletonWireAttacher = new SingletonWireAttacher();
        wireAttacherRegistry.register(SingletonWireTargetDefinition.class, singletonWireAttacher);
        IFProviderBuilderRegistry providerRegistry = new DefaultIFProviderBuilderRegistry();
        ReflectiveIFProviderBuilder provider = new ReflectiveIFProviderBuilder();
        provider.setBuilderRegistry(providerRegistry);
        TransformerRegistry<PullTransformer<?, ?>> transformerRegistry =
                new DefaultTransformerRegistry<PullTransformer<?, ?>>();
        transformerRegistry.register(new String2String());
        transformerRegistry.register(new String2Integer());
        SystemComponentBuilder<?> builder = new SystemComponentBuilder<Object>(registry,
                                                                               componentManager,
                                                                               wireAttacherRegistry,
                                                                               scopeRegistry,
                                                                               providerRegistry,
                                                                               classLoaderRegistry,
                                                                               transformerRegistry);
        builder.init();
        deployer.setBuilderRegistry(registry);
        deployer.setComponentManager(componentManager);
        Connector connector = new ConnectorImpl(null, wireAttacherRegistry);
        deployer.setConnector(connector);
        return deployer;
    }


}
