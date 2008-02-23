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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.fabric.assembly.RuntimeAssemblyImpl;
import org.fabric3.fabric.assembly.allocator.Allocator;
import org.fabric3.fabric.assembly.allocator.LocalAllocator;
import org.fabric3.fabric.assembly.normalizer.PromotionNormalizer;
import org.fabric3.fabric.assembly.normalizer.PromotionNormalizerImpl;
import org.fabric3.fabric.builder.ConnectorImpl;
import org.fabric3.fabric.builder.component.DefaultComponentBuilderRegistry;
import org.fabric3.fabric.builder.resource.ResourceContainerBuilderRegistryImpl;
import org.fabric3.fabric.classloader.ClassLoaderBuilder;
import org.fabric3.fabric.classloader.ClassLoaderGeneratorImpl;
import org.fabric3.fabric.command.CommandExecutorRegistryImpl;
import org.fabric3.fabric.command.InitializeComponentCommand;
import org.fabric3.fabric.command.InitializeComponentExecutor;
import org.fabric3.fabric.command.StartCompositeContextCommand;
import org.fabric3.fabric.command.StartCompositeContextExecutor;
import org.fabric3.fabric.command.StartCompositeContextGenerator;
import org.fabric3.fabric.deployer.DeployerImpl;
import org.fabric3.fabric.deployer.DeployerMonitor;
import org.fabric3.fabric.generator.GeneratorRegistryImpl;
import org.fabric3.fabric.implementation.singleton.SingletonGenerator;
import org.fabric3.fabric.implementation.singleton.SingletonWireAttacher;
import org.fabric3.fabric.implementation.singleton.SingletonWireTargetDefinition;
import org.fabric3.fabric.implementation.system.SystemComponentBuilder;
import org.fabric3.fabric.implementation.system.SystemComponentGenerator;
import org.fabric3.fabric.implementation.system.SystemWireAttacher;
import org.fabric3.fabric.model.logical.AtomicComponentInstantiator;
import org.fabric3.fabric.model.logical.CompositeComponentInstantiator;
import org.fabric3.fabric.model.logical.LogicalModelGenerator;
import org.fabric3.fabric.model.logical.LogicalModelGeneratorImpl;
import org.fabric3.fabric.model.physical.PhysicalModelGenerator;
import org.fabric3.fabric.model.physical.PhysicalModelGeneratorImpl;
import org.fabric3.fabric.model.physical.PhysicalOperationHelper;
import org.fabric3.fabric.model.physical.PhysicalOperationHelperImpl;
import org.fabric3.fabric.model.physical.PhysicalWireGenerator;
import org.fabric3.fabric.model.physical.PhysicalWireGeneratorImpl;
import org.fabric3.fabric.monitor.MonitorWireAttacher;
import org.fabric3.fabric.monitor.MonitorWireGenerator;
import org.fabric3.fabric.runtime.ComponentNames;
import org.fabric3.fabric.services.contribution.ArtifactResolverRegistryImpl;
import org.fabric3.fabric.services.contribution.ClasspathProcessorRegistryImpl;
import org.fabric3.fabric.services.contribution.FileSystemResolver;
import org.fabric3.fabric.services.contribution.processor.JarClasspathProcessor;
import org.fabric3.fabric.services.discovery.SingleVMDiscoveryService;
import org.fabric3.fabric.services.documentloader.DocumentLoader;
import org.fabric3.fabric.services.documentloader.DocumentLoaderImpl;
import org.fabric3.fabric.services.instancefactory.BuildHelperImpl;
import org.fabric3.fabric.services.instancefactory.DefaultInstanceFactoryBuilderRegistry;
import org.fabric3.fabric.services.instancefactory.GenerationHelperImpl;
import org.fabric3.fabric.services.instancefactory.ReflectiveInstanceFactoryBuilder;
import org.fabric3.fabric.services.routing.RoutingService;
import org.fabric3.fabric.services.routing.RuntimeRoutingService;
import org.fabric3.fabric.services.runtime.BootstrapRuntimeInfoService;
import org.fabric3.fabric.wire.DefaultWiringService;
import org.fabric3.fabric.wire.promotion.DefaultTargetPromotionService;
import org.fabric3.fabric.wire.resolve.ExplicitTargetResolutionService;
import org.fabric3.fabric.wire.resolve.TypeBasedAutoWireService;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.monitor.MonitorFactory;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuildHelper;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.spi.assembly.Assembly;
import org.fabric3.spi.assembly.AssemblyException;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.builder.resource.ResourceContainerBuilderRegistry;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.deployer.Deployer;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.policy.NullPolicyResolver;
import org.fabric3.spi.runtime.assembly.LogicalComponentManager;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.contribution.ArtifactResolverRegistry;
import org.fabric3.spi.services.contribution.ClasspathProcessorRegistry;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.discovery.DiscoveryService;
import org.fabric3.spi.services.runtime.RuntimeInfoService;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.wire.TargetPromotionService;
import org.fabric3.spi.wire.TargetResolutionService;
import org.fabric3.spi.wire.WiringService;
import org.fabric3.transform.DefaultTransformerRegistry;
import org.fabric3.transform.dom2java.String2Class;
import org.fabric3.transform.dom2java.String2Integer;
import org.fabric3.transform.dom2java.String2Map;
import org.fabric3.transform.dom2java.String2QName;
import org.fabric3.transform.dom2java.String2String;

/**
 * @version $Rev$ $Date$
 */
public class BootstrapAssemblyFactory {
    public static Assembly createAssembly(Fabric3Runtime<?> runtime) throws InitializationException {
        MonitorFactory monitorFactory = runtime.getMonitorFactory();
        ClassLoaderRegistry classLoaderRegistry =
                runtime.getSystemComponent(ClassLoaderRegistry.class, ComponentNames.CLASSLOADER_REGISTRY_URI);
        ComponentManager componentManager = runtime.getSystemComponent(ComponentManager.class,
                                                                       URI.create(ComponentNames.RUNTIME_NAME + "/ComponentManager"));
        LogicalComponentManager lcm = runtime.getSystemComponent(LogicalComponentManager.class,
                                                                 URI.create(ComponentNames.RUNTIME_NAME + "/LogicalComponentManager"));
        MetaDataStore metaDataStore =
                runtime.getSystemComponent(MetaDataStore.class, ComponentNames.METADATA_STORE_URI);
        ScopeRegistry scopeRegistry =
                runtime.getSystemComponent(ScopeRegistry.class, ComponentNames.SCOPE_REGISTRY_URI);
        return createAssembly(monitorFactory, classLoaderRegistry, scopeRegistry, componentManager, lcm, metaDataStore);
    }

    public static Assembly createAssembly(MonitorFactory monitorFactory,
                                          ClassLoaderRegistry classLoaderRegistry,
                                          ScopeRegistry scopeRegistry,
                                          ComponentManager componentManager,
                                          LogicalComponentManager logicalComponentManager,
                                          MetaDataStore metaDataStore) throws InitializationException {
        Allocator allocator = new LocalAllocator();

        Deployer deployer = createDeployer(monitorFactory, classLoaderRegistry, scopeRegistry, componentManager);
        CommandExecutorRegistry commandRegistry =
                createCommandExecutorRegistry(scopeRegistry, componentManager);
        RuntimeRoutingService routingService = new RuntimeRoutingService(deployer, commandRegistry);

        GeneratorRegistry generatorRegistry = createGeneratorRegistry(classLoaderRegistry);
        PhysicalOperationHelper physicalOperationHelper = new PhysicalOperationHelperImpl();
        PhysicalWireGenerator wireGenerator = new PhysicalWireGeneratorImpl(generatorRegistry,
                                                                            new NullPolicyResolver(),
                                                                            physicalOperationHelper);
        PhysicalModelGenerator physicalModelGenerator =
                createPhysicalModelGenerator(generatorRegistry, routingService, logicalComponentManager, wireGenerator);
        
        TargetPromotionService targetPromotionService = new DefaultTargetPromotionService();
        List<TargetResolutionService> targetResolutionServices = new ArrayList<TargetResolutionService>();
        targetResolutionServices.add(new ExplicitTargetResolutionService());
        targetResolutionServices.add(new TypeBasedAutoWireService());
        WiringService wiringService = new DefaultWiringService(targetPromotionService, targetResolutionServices);
        
        PromotionNormalizer normalizer = new PromotionNormalizerImpl();
        DocumentLoader documentLoader = new DocumentLoaderImpl();
        AtomicComponentInstantiator atomicComponentInstantiator = new AtomicComponentInstantiator(documentLoader);

        CompositeComponentInstantiator compositeComponentInstantiator =
                new CompositeComponentInstantiator(atomicComponentInstantiator, documentLoader);
        LogicalModelGenerator logicalModelGenerator = new LogicalModelGeneratorImpl(wiringService,
                                                                                    normalizer,
                                                                                    logicalComponentManager,
                                                                                    atomicComponentInstantiator,
                                                                                    compositeComponentInstantiator);

        Assembly runtimeAssembly = new RuntimeAssemblyImpl(allocator,
                                                           routingService,
                                                           metaDataStore,
                                                           physicalModelGenerator,
                                                           logicalModelGenerator,
                                                           logicalComponentManager,
                                                           wireGenerator);
        try {
            runtimeAssembly.initialize();
        } catch (AssemblyException e) {
            throw new InitializationException(e);
        }
        return runtimeAssembly;
    }

    private static DeployerImpl createDeployer(MonitorFactory monitorFactory,
                                               ClassLoaderRegistry classLoaderRegistry,
                                               ScopeRegistry scopeRegistry,
                                               ComponentManager componentManager) {

        InstanceFactoryBuilderRegistry providerRegistry = new DefaultInstanceFactoryBuilderRegistry();
        InstanceFactoryBuildHelper buildHelper = new BuildHelperImpl(classLoaderRegistry);
        ReflectiveInstanceFactoryBuilder provider = new ReflectiveInstanceFactoryBuilder(providerRegistry, buildHelper);
        provider.init();

        TransformerRegistry<PullTransformer<?, ?>> transformerRegistry =
                new DefaultTransformerRegistry<PullTransformer<?, ?>>();
        transformerRegistry.register(new String2String());
        transformerRegistry.register(new String2Integer());
        transformerRegistry.register(new String2Map());
        transformerRegistry.register(new String2Class(classLoaderRegistry));
        transformerRegistry.register(new String2QName());

        ComponentBuilderRegistry registry = new DefaultComponentBuilderRegistry();
        SystemComponentBuilder<?> builder = new SystemComponentBuilder<Object>(registry,
                                                                               scopeRegistry,
                                                                               providerRegistry,
                                                                               classLoaderRegistry,
                                                                               transformerRegistry);
        builder.init();

        SingletonWireAttacher singletonWireAttacher = new SingletonWireAttacher(componentManager);
        ConnectorImpl connector = new ConnectorImpl(null);
        connector.register(SingletonWireTargetDefinition.class, singletonWireAttacher);
        new SystemWireAttacher(componentManager, connector, connector, transformerRegistry, classLoaderRegistry).init();
        new MonitorWireAttacher(connector, monitorFactory, classLoaderRegistry).init();

        ResourceContainerBuilderRegistry resourceRegistry = createResourceBuilderRegistry(classLoaderRegistry);

        DeployerImpl deployer = new DeployerImpl(monitorFactory.getMonitor(DeployerMonitor.class));
        deployer.setBuilderRegistry(registry);
        deployer.setComponentManager(componentManager);
        deployer.setConnector(connector);
        deployer.setResourceBuilderRegistry(resourceRegistry);

        return deployer;

    }

    private static ResourceContainerBuilderRegistry createResourceBuilderRegistry(ClassLoaderRegistry classLoaderRegistry) {

        ResourceContainerBuilderRegistry resourceRegistry = new ResourceContainerBuilderRegistryImpl();
        ArtifactResolverRegistry artifactResolverRegistry = new ArtifactResolverRegistryImpl();

        FileSystemResolver resolver = new FileSystemResolver(artifactResolverRegistry);
        resolver.init();

        ClasspathProcessorRegistry classpathProcessorRegistry = new ClasspathProcessorRegistryImpl();
        JarClasspathProcessor classpathProcessor = new JarClasspathProcessor(classpathProcessorRegistry);
        classpathProcessor.init();
        ClassLoaderBuilder clBuilder = new ClassLoaderBuilder(resourceRegistry,
                                                              classLoaderRegistry,
                                                              artifactResolverRegistry,
                                                              classpathProcessorRegistry);
        clBuilder.init();

        return resourceRegistry;

    }

    private static CommandExecutorRegistry createCommandExecutorRegistry(ScopeRegistry scopeRegistry,
                                                                         ComponentManager componentManager) {

        CommandExecutorRegistryImpl commandRegistry = new CommandExecutorRegistryImpl();
        StartCompositeContextExecutor executor = new StartCompositeContextExecutor(scopeRegistry);
        InitializeComponentExecutor initExecutor = new InitializeComponentExecutor(scopeRegistry, componentManager);
        commandRegistry.register(StartCompositeContextCommand.class, executor);
        commandRegistry.register(InitializeComponentCommand.class, initExecutor);
        return commandRegistry;

    }

    private static GeneratorRegistry createGeneratorRegistry(ClassLoaderRegistry classLoaderRegistry) {
        GeneratorRegistryImpl registry = new GeneratorRegistryImpl();
        RuntimeInfoService infoService = new BootstrapRuntimeInfoService(classLoaderRegistry);
        DiscoveryService discoveryService = new SingleVMDiscoveryService(infoService);
        GenerationHelperImpl helper = new GenerationHelperImpl();
        new SystemComponentGenerator(registry, new ClassLoaderGeneratorImpl(discoveryService), helper);
        new SingletonGenerator(registry);
        new StartCompositeContextGenerator(registry).init();
        new MonitorWireGenerator(registry).init();
        return registry;
    }

    private static PhysicalModelGenerator createPhysicalModelGenerator(GeneratorRegistry generatorRegistry,
                                                                       RoutingService routingService,
                                                                       LogicalComponentManager logicalComponentManager,
                                                                       PhysicalWireGenerator wireGenerator) {

        return new PhysicalModelGeneratorImpl(generatorRegistry,
                                              routingService,
                                              logicalComponentManager,
                                              wireGenerator);

    }
}
