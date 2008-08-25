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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.management.MBeanServer;

import org.fabric3.fabric.allocator.Allocator;
import org.fabric3.fabric.allocator.LocalAllocator;
import org.fabric3.fabric.binding.BindingSelector;
import org.fabric3.fabric.binding.BindingSelectorImpl;
import org.fabric3.fabric.builder.ConnectorImpl;
import org.fabric3.fabric.builder.classloader.ClassLoaderBuilder;
import org.fabric3.fabric.builder.classloader.ClassLoaderBuilderImpl;
import org.fabric3.fabric.builder.component.DefaultComponentBuilderRegistry;
import org.fabric3.fabric.command.AttachWireCommand;
import org.fabric3.fabric.command.BuildComponentCommand;
import org.fabric3.fabric.command.InitializeComponentCommand;
import org.fabric3.fabric.command.ProvisionClassloaderCommand;
import org.fabric3.fabric.command.StartComponentCommand;
import org.fabric3.fabric.command.StartCompositeContextCommand;
import org.fabric3.fabric.domain.RuntimeDomain;
import org.fabric3.fabric.executor.AttachWireCommandExecutor;
import org.fabric3.fabric.executor.BuildComponentCommandExecutor;
import org.fabric3.fabric.executor.CommandExecutorRegistryImpl;
import org.fabric3.fabric.executor.InitializeComponentCommandExecutor;
import org.fabric3.fabric.executor.ProvisionClassloaderCommandExecutor;
import org.fabric3.fabric.executor.StartComponentCommandExecutor;
import org.fabric3.fabric.executor.StartCompositeContextCommandExecutor;
import org.fabric3.fabric.generator.GeneratorRegistryImpl;
import org.fabric3.fabric.generator.PhysicalModelGenerator;
import org.fabric3.fabric.generator.PhysicalModelGeneratorImpl;
import org.fabric3.fabric.generator.classloader.ClassLoaderCommandGenerator;
import org.fabric3.fabric.generator.classloader.ClassLoaderCommandGeneratorImpl;
import org.fabric3.fabric.generator.component.BuildComponentCommandGenerator;
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
import org.fabric3.fabric.implementation.singleton.SingletonComponentGenerator;
import org.fabric3.fabric.implementation.singleton.SingletonSourceWireAttacher;
import org.fabric3.fabric.implementation.singleton.SingletonTargetWireAttacher;
import org.fabric3.fabric.implementation.singleton.SingletonWireSourceDefinition;
import org.fabric3.fabric.implementation.singleton.SingletonWireTargetDefinition;
import org.fabric3.fabric.instantiator.LogicalModelInstantiator;
import org.fabric3.fabric.instantiator.LogicalModelInstantiatorImpl;
import org.fabric3.fabric.instantiator.ResolutionService;
import org.fabric3.fabric.instantiator.ResolutionServiceImpl;
import org.fabric3.fabric.instantiator.component.AtomicComponentInstantiator;
import org.fabric3.fabric.instantiator.component.CompositeComponentInstantiator;
import org.fabric3.fabric.instantiator.component.WireInstantiator;
import org.fabric3.fabric.instantiator.component.WireInstantiatorImpl;
import org.fabric3.fabric.instantiator.normalize.PromotionNormalizer;
import org.fabric3.fabric.instantiator.normalize.PromotionNormalizerImpl;
import org.fabric3.fabric.instantiator.promotion.DefaultPromotionResolutionService;
import org.fabric3.fabric.instantiator.promotion.PromotionResolutionService;
import org.fabric3.fabric.instantiator.target.ExplicitTargetResolutionService;
import org.fabric3.fabric.instantiator.target.TargetResolutionService;
import org.fabric3.fabric.instantiator.target.TypeBasedAutowireResolutionService;
import org.fabric3.fabric.monitor.MonitorWireAttacher;
import org.fabric3.fabric.monitor.MonitorWireGenerator;
import org.fabric3.fabric.monitor.MonitorWireTargetDefinition;
import org.fabric3.fabric.policy.NullPolicyResolver;
import org.fabric3.fabric.services.contribution.ClasspathProcessorRegistryImpl;
import org.fabric3.fabric.services.contribution.LocalContributionUriResolver;
import org.fabric3.fabric.services.contribution.processor.JarClasspathProcessor;
import org.fabric3.fabric.services.documentloader.DocumentLoader;
import org.fabric3.fabric.services.documentloader.DocumentLoaderImpl;
import org.fabric3.fabric.services.routing.RuntimeRoutingService;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.jmx.control.JMXBindingGenerator;
import org.fabric3.jmx.provision.JMXWireSourceDefinition;
import org.fabric3.jmx.runtime.JMXWireAttacher;
import org.fabric3.jmx.scdl.JMXBinding;
import org.fabric3.monitor.MonitorFactory;
import org.fabric3.pojo.control.GenerationHelperImpl;
import org.fabric3.pojo.instancefactory.BuildHelperImpl;
import org.fabric3.pojo.instancefactory.DefaultInstanceFactoryBuilderRegistry;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuildHelper;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.instancefactory.ReflectiveInstanceFactoryBuilder;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.domain.Domain;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.generator.AddCommandGenerator;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.RemoveCommandGenerator;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.services.contribution.ClasspathProcessorRegistry;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.lcm.LogicalComponentManager;
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

    public static Domain createDomain(MonitorFactory monitorFactory,
                                      ClassLoaderRegistry classLoaderRegistry,
                                      ScopeRegistry scopeRegistry,
                                      ComponentManager componentManager,
                                      LogicalComponentManager logicalComponentManager,
                                      MetaDataStore metaDataStore,
                                      MBeanServer mbServer,
                                      String jmxSubDomain,
                                      HostInfo info) throws InitializationException {

        Allocator allocator = new LocalAllocator();
        BindingSelector bindingSelector = new BindingSelectorImpl(logicalComponentManager);
        CommandExecutorRegistry commandRegistry =
                createCommandExecutorRegistry(monitorFactory,
                                              classLoaderRegistry,
                                              scopeRegistry,
                                              componentManager,
                                              mbServer,
                                              metaDataStore,
                                              jmxSubDomain,
                                              info);

        RuntimeRoutingService routingService = new RuntimeRoutingService(commandRegistry, scopeRegistry);

        PhysicalModelGenerator physicalModelGenerator =
                createPhysicalModelGenerator(logicalComponentManager, metaDataStore);

        LogicalModelInstantiator logicalModelInstantiator = createLogicalModelGenerator(logicalComponentManager);

        return new RuntimeDomain(allocator,
                                 metaDataStore,
                                 physicalModelGenerator,
                                 logicalModelInstantiator,
                                 logicalComponentManager,
                                 bindingSelector,
                                 routingService);
    }

    private static LogicalModelInstantiator createLogicalModelGenerator(LogicalComponentManager logicalComponentManager) {
        PromotionResolutionService promotionResolutionService = new DefaultPromotionResolutionService();
        List<TargetResolutionService> targetResolutionServices = new ArrayList<TargetResolutionService>();
        targetResolutionServices.add(new ExplicitTargetResolutionService());
        targetResolutionServices.add(new TypeBasedAutowireResolutionService());
        ResolutionService resolutionService = new ResolutionServiceImpl(promotionResolutionService, targetResolutionServices);

        PromotionNormalizer normalizer = new PromotionNormalizerImpl();
        DocumentLoader documentLoader = new DocumentLoaderImpl();
        AtomicComponentInstantiator atomicComponentInstantiator = new AtomicComponentInstantiator(documentLoader);

        WireInstantiator wireInstantiator = new WireInstantiatorImpl();
        CompositeComponentInstantiator compositeComponentInstantiator =
                new CompositeComponentInstantiator(atomicComponentInstantiator, wireInstantiator, documentLoader);
        return new LogicalModelInstantiatorImpl(resolutionService,
                                                normalizer,
                                                logicalComponentManager,
                                                atomicComponentInstantiator,
                                                compositeComponentInstantiator,
                                                wireInstantiator);
    }

    private static CommandExecutorRegistry createCommandExecutorRegistry(MonitorFactory monitorFactory,
                                                                         ClassLoaderRegistry classLoaderRegistry,
                                                                         ScopeRegistry scopeRegistry,
                                                                         ComponentManager componentManager,
                                                                         MBeanServer mbeanServer,
                                                                         MetaDataStore metaDataStore,
                                                                         String jmxSubDomain,
                                                                         HostInfo info) {

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
        sourceAttachers.put(SingletonWireSourceDefinition.class, new SingletonSourceWireAttacher(componentManager));

        sourceAttachers.put(JMXWireSourceDefinition.class, new JMXWireAttacher(mbeanServer, classLoaderRegistry, jmxSubDomain));

        Map<Class<? extends PhysicalWireTargetDefinition>, TargetWireAttacher<? extends PhysicalWireTargetDefinition>> targetAttachers =
                new ConcurrentHashMap<Class<? extends PhysicalWireTargetDefinition>, TargetWireAttacher<? extends PhysicalWireTargetDefinition>>();
        targetAttachers.put(SingletonWireTargetDefinition.class, new SingletonTargetWireAttacher(componentManager));
        targetAttachers.put(SystemWireTargetDefinition.class, new SystemTargetWireAttacher(componentManager, classLoaderRegistry));
        targetAttachers.put(MonitorWireTargetDefinition.class, new MonitorWireAttacher(monitorFactory, classLoaderRegistry));

        ConnectorImpl connector = new ConnectorImpl();
        connector.setSourceAttachers(sourceAttachers);
        connector.setTargetAttachers(targetAttachers);

        ClasspathProcessorRegistry cpRegistry = new ClasspathProcessorRegistryImpl();

        ClassLoaderBuilder classLoaderBuilder = createClassLoaderBuilder(classLoaderRegistry, cpRegistry, metaDataStore, info);

        CommandExecutorRegistryImpl commandRegistry = new CommandExecutorRegistryImpl();

        commandRegistry.register(StartCompositeContextCommand.class, new StartCompositeContextCommandExecutor(scopeRegistry));
        commandRegistry.register(InitializeComponentCommand.class, new InitializeComponentCommandExecutor(scopeRegistry, componentManager));
        commandRegistry.register(BuildComponentCommand.class, new BuildComponentCommandExecutor(registry, componentManager));
        commandRegistry.register(AttachWireCommand.class, new AttachWireCommandExecutor(connector));
        commandRegistry.register(StartComponentCommand.class, new StartComponentCommandExecutor(componentManager));
        commandRegistry.register(ProvisionClassloaderCommand.class, new ProvisionClassloaderCommandExecutor(classLoaderBuilder));

        return commandRegistry;

    }

    private static ClassLoaderBuilder createClassLoaderBuilder(ClassLoaderRegistry classLoaderRegistry,
                                                               ClasspathProcessorRegistry cpRegistry,
                                                               MetaDataStore metaDataStore,
                                                               HostInfo info) {

        LocalContributionUriResolver resolver = new LocalContributionUriResolver(metaDataStore);
        JarClasspathProcessor classpathProcessor = new JarClasspathProcessor(cpRegistry);
        classpathProcessor.init();
        return new ClassLoaderBuilderImpl(classLoaderRegistry, resolver, cpRegistry, info);
    }

    private static PhysicalModelGenerator createPhysicalModelGenerator(LogicalComponentManager logicalComponentManager,
                                                                       MetaDataStore metaDataStore) {

        GeneratorRegistry generatorRegistry = createGeneratorRegistry();
        PhysicalOperationHelper physicalOperationHelper = new PhysicalOperationHelperImpl();
        PhysicalWireGenerator wireGenerator = new PhysicalWireGeneratorImpl(generatorRegistry, new NullPolicyResolver(), physicalOperationHelper);

        ClassLoaderCommandGenerator classLoaderCommandGenerator = new ClassLoaderCommandGeneratorImpl(metaDataStore);

        List<AddCommandGenerator> commandGenerators = new ArrayList<AddCommandGenerator>();
        commandGenerators.add(new BuildComponentCommandGenerator(generatorRegistry, 1));
        commandGenerators.add(new LocalWireCommandGenerator(wireGenerator, logicalComponentManager, 2));
        commandGenerators.add(new ServiceWireCommandGenerator(wireGenerator, 2));
        commandGenerators.add(new ResourceWireCommandGenerator(wireGenerator, 2));
        commandGenerators.add(new StartComponentCommandGenerator(3));
        commandGenerators.add(new StartCompositeContextCommandGenerator(4));
        commandGenerators.add(new InitializeComponentCommandGenerator(5));
        List<RemoveCommandGenerator> removeCmdGenerator = new ArrayList<RemoveCommandGenerator>(2);
        removeCmdGenerator.add(new StopCompositeContextCommandGenerator(0));
        removeCmdGenerator.add(new StopComponentCommandGenerator(1));
        return new PhysicalModelGeneratorImpl(commandGenerators, removeCmdGenerator, classLoaderCommandGenerator);
    }

    private static GeneratorRegistry createGeneratorRegistry() {
        GeneratorRegistryImpl registry = new GeneratorRegistryImpl();
        GenerationHelperImpl helper = new GenerationHelperImpl();
        new SystemComponentGenerator(registry, helper);
        new SingletonComponentGenerator(registry);
        registry.register(JMXBinding.class, new JMXBindingGenerator());
        new MonitorWireGenerator(registry).init();
        return registry;
    }

}
