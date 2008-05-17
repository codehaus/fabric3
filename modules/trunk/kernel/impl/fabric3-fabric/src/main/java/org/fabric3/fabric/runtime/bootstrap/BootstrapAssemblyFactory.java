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
import javax.management.MBeanServer;

import org.fabric3.fabric.assembly.RuntimeAssemblyImpl;
import org.fabric3.fabric.assembly.allocator.Allocator;
import org.fabric3.fabric.assembly.allocator.LocalAllocator;
import org.fabric3.fabric.assembly.normalizer.PromotionNormalizer;
import org.fabric3.fabric.assembly.normalizer.PromotionNormalizerImpl;
import org.fabric3.fabric.builder.ConnectorImpl;
import org.fabric3.fabric.builder.classloader.ClassLoaderBuilder;
import org.fabric3.fabric.builder.classloader.ClassLoaderBuilderImpl;
import org.fabric3.fabric.builder.component.DefaultComponentBuilderRegistry;
import org.fabric3.fabric.command.ClassloaderProvisionCommand;
import org.fabric3.fabric.command.ComponentBuildCommand;
import org.fabric3.fabric.command.InitializeComponentCommand;
import org.fabric3.fabric.command.StartComponentCommand;
import org.fabric3.fabric.command.StartCompositeContextCommand;
import org.fabric3.fabric.command.WireAttachCommand;
import org.fabric3.fabric.executor.ClassloaderProvisionCommandExecutor;
import org.fabric3.fabric.executor.CommandExecutorRegistryImpl;
import org.fabric3.fabric.executor.ComponentBuildCommandExecutor;
import org.fabric3.fabric.executor.InitializeComponentCommandExecutor;
import org.fabric3.fabric.executor.StartComponentCommandExecutor;
import org.fabric3.fabric.executor.StartCompositeContextCommandExecutor;
import org.fabric3.fabric.executor.WireAttachCommandExecutor;
import org.fabric3.fabric.generator.GeneratorRegistryImpl;
import org.fabric3.fabric.generator.PhysicalModelGenerator;
import org.fabric3.fabric.generator.PhysicalModelGeneratorImpl;
import org.fabric3.fabric.generator.classloader.ClassLoaderGenerator;
import org.fabric3.fabric.generator.classloader.ClassLoaderGeneratorImpl;
import org.fabric3.fabric.generator.classloader.ClassloaderProvisionCommandGenerator;
import org.fabric3.fabric.generator.component.ComponentBuildCommandGenerator;
import org.fabric3.fabric.generator.component.InitializeComponentCommandGenerator;
import org.fabric3.fabric.generator.component.StartComponentCommandGenerator;
import org.fabric3.fabric.generator.component.StartCompositeContextCommandGenerator;
import org.fabric3.fabric.generator.component.StopComponentCommandGenerator;
import org.fabric3.fabric.generator.component.StopCompositeContextCommandGenerator;
import org.fabric3.fabric.generator.wire.LocalWireCommandGenerator;
import org.fabric3.fabric.generator.wire.PhysicalOperationHelper;
import org.fabric3.fabric.generator.wire.PhysicalOperationHelperImpl;
import org.fabric3.fabric.generator.wire.PhysicalWireGenerator;
import org.fabric3.fabric.generator.wire.PhysicalWireGeneratorImpl;
import org.fabric3.fabric.generator.wire.ResourceWireCommandGenerator;
import org.fabric3.fabric.generator.wire.ServiceWireCommandGenerator;
import org.fabric3.fabric.implementation.singleton.SingletonGenerator;
import org.fabric3.fabric.implementation.singleton.SingletonWireAttacher;
import org.fabric3.fabric.implementation.singleton.SingletonWireTargetDefinition;
import org.fabric3.fabric.model.logical.AtomicComponentInstantiator;
import org.fabric3.fabric.model.logical.CompositeComponentInstantiator;
import org.fabric3.fabric.model.logical.LogicalModelGenerator;
import org.fabric3.fabric.model.logical.LogicalModelGeneratorImpl;
import org.fabric3.fabric.monitor.MonitorWireAttacher;
import org.fabric3.fabric.monitor.MonitorWireGenerator;
import org.fabric3.fabric.monitor.MonitorWireTargetDefinition;
import org.fabric3.fabric.policy.NullPolicyResolver;
import org.fabric3.fabric.runtime.ComponentNames;
import org.fabric3.fabric.services.contribution.LocalContributionUriResolver;
import org.fabric3.fabric.services.contribution.processor.JarClasspathProcessor;
import org.fabric3.fabric.services.documentloader.DocumentLoader;
import org.fabric3.fabric.services.documentloader.DocumentLoaderImpl;
import org.fabric3.fabric.services.instancefactory.BuildHelperImpl;
import org.fabric3.fabric.services.instancefactory.DefaultInstanceFactoryBuilderRegistry;
import org.fabric3.fabric.services.instancefactory.GenerationHelperImpl;
import org.fabric3.fabric.services.instancefactory.ReflectiveInstanceFactoryBuilder;
import org.fabric3.fabric.services.routing.RuntimeRoutingService;
import org.fabric3.fabric.wire.DefaultWiringService;
import org.fabric3.fabric.wire.promotion.DefaultTargetPromotionService;
import org.fabric3.fabric.wire.resolve.ExplicitTargetResolutionService;
import org.fabric3.fabric.wire.resolve.TypeBasedAutoWireService;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.jmx.control.JMXBindingGenerator;
import org.fabric3.jmx.provision.JMXWireSourceDefinition;
import org.fabric3.jmx.runtime.JMXWireAttacher;
import org.fabric3.jmx.scdl.JMXBinding;
import org.fabric3.monitor.MonitorFactory;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuildHelper;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.spi.assembly.Assembly;
import org.fabric3.spi.assembly.AssemblyException;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.generator.AddCommandGenerator;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.RemoveCommandGenerator;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.runtime.assembly.LogicalComponentManager;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.contribution.ClasspathProcessorRegistry;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.wire.TargetPromotionService;
import org.fabric3.spi.wire.TargetResolutionService;
import org.fabric3.spi.wire.WiringService;
import org.fabric3.system.control.SystemComponentGenerator;
import org.fabric3.system.provision.SystemWireSourceDefinition;
import org.fabric3.system.provision.SystemWireTargetDefinition;
import org.fabric3.system.runtime.SystemComponentBuilder;
import org.fabric3.system.runtime.SystemSourceWireAttacher;
import org.fabric3.system.runtime.SystemTargetWireAttacher;
import org.fabric3.transform.DefaultTransformerRegistry;
import org.fabric3.transform.PullTransformer;
import org.fabric3.transform.TransformerRegistry;
import org.fabric3.transform.dom2java.String2Boolean;
import org.fabric3.transform.dom2java.String2Class;
import org.fabric3.transform.dom2java.String2Integer;
import org.fabric3.transform.dom2java.String2QName;
import org.fabric3.transform.dom2java.String2String;
import org.fabric3.transform.dom2java.generics.list.String2ListOfString;
import org.fabric3.transform.dom2java.generics.map.String2MapOfString2String;

/**
 * @version $Rev$ $Date$
 */
public class BootstrapAssemblyFactory {
    public static Assembly createAssembly(Fabric3Runtime<?> runtime) throws InitializationException {
        MonitorFactory monitorFactory = runtime.getMonitorFactory();
        MBeanServer mbeanServer = runtime.getMBeanServer();
        String jmxDomain = runtime.getJMXDomain();
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

        ClasspathProcessorRegistry cpRegistry = runtime.getSystemComponent(ClasspathProcessorRegistry.class,
                                                                           URI.create(ComponentNames.RUNTIME_NAME + "/ClasspathProcessorRegistry"));
        return createAssembly(monitorFactory,
                              classLoaderRegistry,
                              scopeRegistry,
                              componentManager,
                              lcm,
                              metaDataStore,
                              cpRegistry,
                              mbeanServer,
                              jmxDomain);
    }

    public static Assembly createAssembly(MonitorFactory monitorFactory,
                                          ClassLoaderRegistry classLoaderRegistry,
                                          ScopeRegistry scopeRegistry,
                                          ComponentManager componentManager,
                                          LogicalComponentManager logicalComponentManager,
                                          MetaDataStore metaDataStore,
                                          ClasspathProcessorRegistry cpRegistry,
                                          MBeanServer mbServer,
                                          String jmxDomain) throws InitializationException {

        Allocator allocator = new LocalAllocator();

        CommandExecutorRegistry commandRegistry =
                createCommandExecutorRegistry(monitorFactory,
                                              classLoaderRegistry,
                                              scopeRegistry,
                                              componentManager,
                                              cpRegistry,
                                              mbServer,
                                              metaDataStore,
                                              jmxDomain);

        RuntimeRoutingService routingService = new RuntimeRoutingService(commandRegistry, scopeRegistry);

        PhysicalModelGenerator physicalModelGenerator =
                createPhysicalModelGenerator(logicalComponentManager, metaDataStore);

        LogicalModelGenerator logicalModelGenerator = createLogicalModelGenerator(logicalComponentManager);

        Assembly runtimeAssembly = new RuntimeAssemblyImpl(allocator,
                                                           metaDataStore,
                                                           physicalModelGenerator,
                                                           logicalModelGenerator,
                                                           logicalComponentManager,
                                                           routingService);
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
                                                                         ComponentManager componentManager,
                                                                         ClasspathProcessorRegistry cpRegistry,
                                                                         MBeanServer mbeanServer,
                                                                         MetaDataStore metaDataStore,
                                                                         String jmxDomain) {

        InstanceFactoryBuilderRegistry providerRegistry = new DefaultInstanceFactoryBuilderRegistry();
        InstanceFactoryBuildHelper buildHelper = new BuildHelperImpl(classLoaderRegistry);
        ReflectiveInstanceFactoryBuilder provider = new ReflectiveInstanceFactoryBuilder(providerRegistry, buildHelper);
        provider.init();

        TransformerRegistry<PullTransformer<?, ?>> transformerRegistry =
                new DefaultTransformerRegistry<PullTransformer<?, ?>>();
        transformerRegistry.register(new String2String());
        transformerRegistry.register(new String2Integer());
        transformerRegistry.register(new String2Boolean());
        transformerRegistry.register(new String2MapOfString2String());
        transformerRegistry.register(new String2Class(classLoaderRegistry));
        transformerRegistry.register(new String2QName());
        transformerRegistry.register(new String2ListOfString());

        ComponentBuilderRegistry registry = new DefaultComponentBuilderRegistry();

        SystemComponentBuilder<?> builder = new SystemComponentBuilder<Object>(registry,
                                                                               scopeRegistry,
                                                                               providerRegistry,
                                                                               classLoaderRegistry,
                                                                               transformerRegistry);
        builder.init();

        Map<Class<? extends PhysicalWireSourceDefinition>, SourceWireAttacher<? extends PhysicalWireSourceDefinition>> sourceAttachers =
                new ConcurrentHashMap<Class<? extends PhysicalWireSourceDefinition>, SourceWireAttacher<? extends PhysicalWireSourceDefinition>>();
        sourceAttachers.put(SystemWireSourceDefinition.class,
                            new SystemSourceWireAttacher(componentManager, transformerRegistry, classLoaderRegistry));
        sourceAttachers.put(JMXWireSourceDefinition.class, new JMXWireAttacher(mbeanServer, classLoaderRegistry, jmxDomain));

        Map<Class<? extends PhysicalWireTargetDefinition>, TargetWireAttacher<? extends PhysicalWireTargetDefinition>> targetAttachers =
                new ConcurrentHashMap<Class<? extends PhysicalWireTargetDefinition>, TargetWireAttacher<? extends PhysicalWireTargetDefinition>>();
        targetAttachers.put(SingletonWireTargetDefinition.class, new SingletonWireAttacher(componentManager));
        targetAttachers.put(SystemWireTargetDefinition.class, new SystemTargetWireAttacher(componentManager, classLoaderRegistry));
        targetAttachers.put(MonitorWireTargetDefinition.class, new MonitorWireAttacher(monitorFactory, classLoaderRegistry));

        ConnectorImpl connector = new ConnectorImpl();
        connector.setSourceAttachers(sourceAttachers);
        connector.setTargetAttachers(targetAttachers);

        ClassLoaderBuilder classLoaderBuilder = createClassLoaderBuilder(classLoaderRegistry, cpRegistry, metaDataStore);

        CommandExecutorRegistryImpl commandRegistry = new CommandExecutorRegistryImpl();

        commandRegistry.register(StartCompositeContextCommand.class, new StartCompositeContextCommandExecutor(scopeRegistry));
        commandRegistry.register(InitializeComponentCommand.class, new InitializeComponentCommandExecutor(scopeRegistry, componentManager));
        commandRegistry.register(ComponentBuildCommand.class, new ComponentBuildCommandExecutor(registry, componentManager));
        commandRegistry.register(WireAttachCommand.class, new WireAttachCommandExecutor(connector));
        commandRegistry.register(StartComponentCommand.class, new StartComponentCommandExecutor(componentManager));
        commandRegistry.register(ClassloaderProvisionCommand.class, new ClassloaderProvisionCommandExecutor(classLoaderBuilder));

        return commandRegistry;

    }

    private static ClassLoaderBuilder createClassLoaderBuilder(ClassLoaderRegistry classLoaderRegistry,
                                                               ClasspathProcessorRegistry cpRegistry,
                                                               MetaDataStore metaDataStore) {

        LocalContributionUriResolver resolver = new LocalContributionUriResolver(metaDataStore);

        JarClasspathProcessor classpathProcessor = new JarClasspathProcessor(cpRegistry);
        classpathProcessor.init();
        return new ClassLoaderBuilderImpl(classLoaderRegistry, resolver, cpRegistry);
    }

    private static PhysicalModelGenerator createPhysicalModelGenerator(LogicalComponentManager logicalComponentManager,
                                                                       MetaDataStore metaDataStore) {

        GeneratorRegistry generatorRegistry = createGeneratorRegistry();
        PhysicalOperationHelper physicalOperationHelper = new PhysicalOperationHelperImpl();
        PhysicalWireGenerator wireGenerator = new PhysicalWireGeneratorImpl(generatorRegistry, new NullPolicyResolver(), physicalOperationHelper);

        ClassLoaderGenerator classLoaderGenerator = new ClassLoaderGeneratorImpl(metaDataStore);

        List<AddCommandGenerator> commandGenerators = new ArrayList<AddCommandGenerator>();
        commandGenerators.add(new ClassloaderProvisionCommandGenerator(classLoaderGenerator, 0));
        commandGenerators.add(new ComponentBuildCommandGenerator(generatorRegistry, 1));
        commandGenerators.add(new LocalWireCommandGenerator(wireGenerator, logicalComponentManager, 2));
        commandGenerators.add(new ServiceWireCommandGenerator(wireGenerator, 2));
        commandGenerators.add(new ResourceWireCommandGenerator(wireGenerator, 2));
        commandGenerators.add(new StartComponentCommandGenerator(3));
        commandGenerators.add(new StartCompositeContextCommandGenerator(4));
        commandGenerators.add(new InitializeComponentCommandGenerator(5));
        List<RemoveCommandGenerator> removeCmdGenerator = new ArrayList<RemoveCommandGenerator>(2);
        removeCmdGenerator.add(new StopCompositeContextCommandGenerator(0));
        removeCmdGenerator.add(new StopComponentCommandGenerator(1));
        return new PhysicalModelGeneratorImpl(commandGenerators, removeCmdGenerator);
    }

    private static GeneratorRegistry createGeneratorRegistry() {
        GeneratorRegistryImpl registry = new GeneratorRegistryImpl();
        GenerationHelperImpl helper = new GenerationHelperImpl();
        new SystemComponentGenerator(registry, helper);
        new SingletonGenerator(registry);
        registry.register(JMXBinding.class, new JMXBindingGenerator());
        new MonitorWireGenerator(registry).init();
        return registry;
    }

}
