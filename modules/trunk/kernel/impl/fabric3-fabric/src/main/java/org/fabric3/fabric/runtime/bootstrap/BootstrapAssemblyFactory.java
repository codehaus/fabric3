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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import org.fabric3.fabric.command.ClassloaderProvisionCommand;
import org.fabric3.fabric.command.ClassloaderProvisionCommandExecutor;
import org.fabric3.fabric.command.ClassloaderProvisionCommandGenerator;
import org.fabric3.fabric.command.CommandExecutorRegistryImpl;
import org.fabric3.fabric.command.ComponentBuildCommand;
import org.fabric3.fabric.command.ComponentBuildCommandExecutor;
import org.fabric3.fabric.command.ComponentBuildCommandGenerator;
import org.fabric3.fabric.command.ComponentStartCommand;
import org.fabric3.fabric.command.ComponentStartCommandExecutor;
import org.fabric3.fabric.command.ComponentStartCommandGenerator;
import org.fabric3.fabric.command.InitializeComponentCommand;
import org.fabric3.fabric.command.InitializeComponentCommandExecutor;
import org.fabric3.fabric.command.InitializeComponentCommandGenerator;
import org.fabric3.fabric.command.StartCompositeContextCommand;
import org.fabric3.fabric.command.StartCompositeContextCommandExecutor;
import org.fabric3.fabric.command.StartCompositeContextCommandGenerator;
import org.fabric3.fabric.command.WireAttachCommand;
import org.fabric3.fabric.command.WireAttachCommandExecutor;
import org.fabric3.fabric.command.WireAttachCommandGenerator;
import org.fabric3.fabric.generator.GeneratorRegistryImpl;
import org.fabric3.fabric.implementation.singleton.SingletonGenerator;
import org.fabric3.fabric.implementation.singleton.SingletonWireAttacher;
import org.fabric3.fabric.implementation.singleton.SingletonWireTargetDefinition;
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
import org.fabric3.fabric.monitor.MonitorWireTargetDefinition;
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
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.resource.ResourceContainerBuilderRegistry;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.CommandGenerator;
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
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.system.control.SystemComponentGenerator;
import org.fabric3.system.runtime.SystemComponentBuilder;
import org.fabric3.system.runtime.SystemSourceWireAttacher;
import org.fabric3.system.runtime.SystemTargetWireAttacher;
import org.fabric3.system.provision.SystemWireSourceDefinition;
import org.fabric3.system.provision.SystemWireTargetDefinition;
import org.fabric3.transform.DefaultTransformerRegistry;
import org.fabric3.transform.dom2java.String2Class;
import org.fabric3.transform.dom2java.String2Integer;
import org.fabric3.transform.dom2java.String2List;
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

        CommandExecutorRegistry commandRegistry =
                createCommandExecutorRegistry(monitorFactory, classLoaderRegistry, scopeRegistry, componentManager);

        RuntimeRoutingService routingService = new RuntimeRoutingService(commandRegistry, scopeRegistry);

        PhysicalModelGenerator physicalModelGenerator =
                createPhysicalModelGenerator(logicalComponentManager, routingService, classLoaderRegistry);

        LogicalModelGenerator logicalModelGenerator = createLogicalModelGenerator(logicalComponentManager);

        Assembly runtimeAssembly = new RuntimeAssemblyImpl(allocator,
                                                           metaDataStore,
                                                           physicalModelGenerator,
                                                           logicalModelGenerator,
                                                           logicalComponentManager);
        try {
            runtimeAssembly.initialize();
        } catch (AssemblyException e) {
            throw new InitializationException(e);
        }
        return runtimeAssembly;
    }

    private static LogicalModelGenerator createLogicalModelGenerator(LogicalComponentManager logicalComponentManager) {
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
        return new LogicalModelGeneratorImpl(wiringService,
                                             normalizer,
                                             logicalComponentManager,
                                             atomicComponentInstantiator,
                                             compositeComponentInstantiator);
    }

    private static CommandExecutorRegistry createCommandExecutorRegistry(MonitorFactory monitorFactory,
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
        transformerRegistry.register(new String2List());

        ComponentBuilderRegistry registry = new DefaultComponentBuilderRegistry();

        SystemComponentBuilder<?> builder = new SystemComponentBuilder<Object>(registry,
                                                                               scopeRegistry,
                                                                               providerRegistry,
                                                                               classLoaderRegistry,
                                                                               transformerRegistry);
        builder.init();

        Map<Class<? extends PhysicalWireSourceDefinition>, SourceWireAttacher<? extends PhysicalWireSourceDefinition>> sourceAttachers =
                new ConcurrentHashMap<Class<? extends PhysicalWireSourceDefinition>, SourceWireAttacher<? extends PhysicalWireSourceDefinition>>();
        sourceAttachers.put(SystemWireSourceDefinition.class, new SystemSourceWireAttacher(componentManager, transformerRegistry, classLoaderRegistry));

        Map<Class<? extends PhysicalWireTargetDefinition>, TargetWireAttacher<? extends PhysicalWireTargetDefinition>> targetAttachers =
                new ConcurrentHashMap<Class<? extends PhysicalWireTargetDefinition>, TargetWireAttacher<? extends PhysicalWireTargetDefinition>>();
        targetAttachers.put(SingletonWireTargetDefinition.class, new SingletonWireAttacher(componentManager));
        targetAttachers.put(SystemWireTargetDefinition.class, new SystemTargetWireAttacher(componentManager));
        targetAttachers.put(MonitorWireTargetDefinition.class, new MonitorWireAttacher(monitorFactory, classLoaderRegistry));

        ConnectorImpl connector = new ConnectorImpl();
        connector.setSourceAttachers(sourceAttachers);
        connector.setTargetAttachers(targetAttachers);

        ResourceContainerBuilderRegistry resourceRegistry = createResourceBuilderRegistry(classLoaderRegistry);

        CommandExecutorRegistryImpl commandRegistry = new CommandExecutorRegistryImpl();

        commandRegistry.register(StartCompositeContextCommand.class, new StartCompositeContextCommandExecutor(scopeRegistry));
        commandRegistry.register(InitializeComponentCommand.class, new InitializeComponentCommandExecutor(scopeRegistry, componentManager));
        commandRegistry.register(ComponentBuildCommand.class, new ComponentBuildCommandExecutor(registry, componentManager));
        commandRegistry.register(WireAttachCommand.class, new WireAttachCommandExecutor(connector));
        commandRegistry.register(ComponentStartCommand.class, new ComponentStartCommandExecutor(componentManager));
        commandRegistry.register(ClassloaderProvisionCommand.class, new ClassloaderProvisionCommandExecutor(resourceRegistry));

        return commandRegistry;

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

    private static PhysicalModelGenerator createPhysicalModelGenerator(LogicalComponentManager logicalComponentManager,
                                                                       RoutingService routingService,
                                                                       ClassLoaderRegistry classLoaderRegistry) {

        GeneratorRegistry generatorRegistry = createGeneratorRegistry();
        PhysicalOperationHelper physicalOperationHelper = new PhysicalOperationHelperImpl();
        PhysicalWireGenerator wireGenerator = new PhysicalWireGeneratorImpl(generatorRegistry,
                                                                            new NullPolicyResolver(),
                                                                            physicalOperationHelper);

        RuntimeInfoService infoService = new BootstrapRuntimeInfoService(classLoaderRegistry);
        DiscoveryService discoveryService = new SingleVMDiscoveryService(infoService);
        ClassLoaderGenerator classLoaderGenerator = new ClassLoaderGeneratorImpl(discoveryService);

        List<CommandGenerator> commandGenerators = new ArrayList<CommandGenerator>();
        commandGenerators.add(new ClassloaderProvisionCommandGenerator(classLoaderGenerator, 0));
        commandGenerators.add(new ComponentBuildCommandGenerator(generatorRegistry, 1));
        commandGenerators.add(new WireAttachCommandGenerator(wireGenerator, logicalComponentManager, 2));
        commandGenerators.add(new ComponentStartCommandGenerator(3));
        commandGenerators.add(new StartCompositeContextCommandGenerator(4));
        commandGenerators.add(new InitializeComponentCommandGenerator(5));
        return new PhysicalModelGeneratorImpl(commandGenerators, routingService);
    }

    private static GeneratorRegistry createGeneratorRegistry() {
        GeneratorRegistryImpl registry = new GeneratorRegistryImpl();
        GenerationHelperImpl helper = new GenerationHelperImpl();
        new SystemComponentGenerator(registry, helper);
        new SingletonGenerator(registry);
        new MonitorWireGenerator(registry).init();
        return registry;
    }

}
