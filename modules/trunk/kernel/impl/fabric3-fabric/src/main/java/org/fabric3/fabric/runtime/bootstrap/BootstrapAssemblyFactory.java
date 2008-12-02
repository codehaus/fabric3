/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.fabric.runtime.bootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.management.MBeanServer;

import org.fabric3.fabric.binding.BindingSelector;
import org.fabric3.fabric.binding.BindingSelectorImpl;
import org.fabric3.fabric.builder.ConnectorImpl;
import org.fabric3.fabric.builder.classloader.ClassLoaderBuilder;
import org.fabric3.fabric.builder.classloader.ClassLoaderBuilderImpl;
import org.fabric3.fabric.builder.component.DefaultComponentBuilderRegistry;
import org.fabric3.fabric.collector.Collector;
import org.fabric3.fabric.collector.CollectorImpl;
import org.fabric3.fabric.command.AttachWireCommand;
import org.fabric3.fabric.command.BuildComponentCommand;
import org.fabric3.fabric.command.InitializeComponentCommand;
import org.fabric3.fabric.command.ProvisionClassloaderCommand;
import org.fabric3.fabric.command.StartComponentCommand;
import org.fabric3.fabric.command.StartContextCommand;
import org.fabric3.fabric.domain.RuntimeDomain;
import org.fabric3.fabric.executor.AttachWireCommandExecutor;
import org.fabric3.fabric.executor.BuildComponentCommandExecutor;
import org.fabric3.fabric.executor.CommandExecutorRegistryImpl;
import org.fabric3.fabric.executor.InitializeComponentCommandExecutor;
import org.fabric3.fabric.executor.ProvisionClassloaderCommandExecutor;
import org.fabric3.fabric.executor.StartComponentCommandExecutor;
import org.fabric3.fabric.executor.StartContextCommandExecutor;
import org.fabric3.fabric.generator.GeneratorRegistryImpl;
import org.fabric3.fabric.generator.PhysicalModelGenerator;
import org.fabric3.fabric.generator.PhysicalModelGeneratorImpl;
import org.fabric3.fabric.generator.classloader.ClassLoaderCommandGenerator;
import org.fabric3.fabric.generator.classloader.ClassLoaderCommandGeneratorImpl;
import org.fabric3.fabric.generator.component.BuildComponentCommandGenerator;
import org.fabric3.fabric.generator.component.InitializeComponentCommandGenerator;
import org.fabric3.fabric.generator.component.StartComponentCommandGenerator;
import org.fabric3.fabric.generator.component.StartContextCommandGenerator;
import org.fabric3.fabric.generator.wire.LocalWireCommandGenerator;
import org.fabric3.fabric.generator.wire.PhysicalOperationHelper;
import org.fabric3.fabric.generator.wire.PhysicalOperationHelperImpl;
import org.fabric3.fabric.generator.wire.PhysicalWireGenerator;
import org.fabric3.fabric.generator.wire.PhysicalWireGeneratorImpl;
import org.fabric3.fabric.generator.wire.ResourceWireCommandGenerator;
import org.fabric3.fabric.generator.wire.ServiceWireCommandGenerator;
import org.fabric3.fabric.instantiator.LogicalModelInstantiator;
import org.fabric3.fabric.instantiator.LogicalModelInstantiatorImpl;
import org.fabric3.fabric.instantiator.ResolutionService;
import org.fabric3.fabric.instantiator.ResolutionServiceImpl;
import org.fabric3.fabric.instantiator.TargetResolutionService;
import org.fabric3.fabric.instantiator.component.AtomicComponentInstantiator;
import org.fabric3.fabric.instantiator.component.CompositeComponentInstantiator;
import org.fabric3.fabric.instantiator.WireInstantiator;
import org.fabric3.fabric.instantiator.component.WireInstantiatorImpl;
import org.fabric3.fabric.instantiator.PromotionNormalizer;
import org.fabric3.fabric.instantiator.normalize.PromotionNormalizerImpl;
import org.fabric3.fabric.instantiator.promotion.DefaultPromotionResolutionService;
import org.fabric3.fabric.instantiator.PromotionResolutionService;
import org.fabric3.fabric.instantiator.target.ExplicitTargetResolutionService;
import org.fabric3.fabric.instantiator.target.ServiceContractResolver;
import org.fabric3.fabric.instantiator.target.ServiceContractResolverImpl;
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
import org.fabric3.fabric.services.routing.LocalRoutingService;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.jmx.control.JMXBindingGenerator;
import org.fabric3.jmx.provision.JMXWireSourceDefinition;
import org.fabric3.jmx.runtime.JMXWireAttacher;
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
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.type.JMXBinding;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.services.contribution.ClasspathProcessorRegistry;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.lcm.LogicalComponentManager;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.system.control.SystemComponentGenerator;
import org.fabric3.system.provision.SystemWireSourceDefinition;
import org.fabric3.system.provision.SystemWireTargetDefinition;
import org.fabric3.system.runtime.SystemComponentBuilder;
import org.fabric3.system.runtime.SystemSourceWireAttacher;
import org.fabric3.system.runtime.SystemTargetWireAttacher;
import org.fabric3.system.singleton.SingletonComponentGenerator;
import org.fabric3.system.singleton.SingletonSourceWireAttacher;
import org.fabric3.system.singleton.SingletonTargetWireAttacher;
import org.fabric3.system.singleton.SingletonWireSourceDefinition;
import org.fabric3.system.singleton.SingletonWireTargetDefinition;
import org.fabric3.transform.DefaultTransformerRegistry;
import org.fabric3.transform.dom2java.String2Boolean;
import org.fabric3.transform.dom2java.String2Class;
import org.fabric3.transform.dom2java.String2Integer;
import org.fabric3.transform.dom2java.String2QName;
import org.fabric3.transform.dom2java.String2String;
import org.fabric3.transform.dom2java.generics.list.String2ListOfQName;
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

        LocalRoutingService routingService = new LocalRoutingService(commandRegistry, scopeRegistry);

        PhysicalModelGenerator physicalModelGenerator =
                createPhysicalModelGenerator(logicalComponentManager, metaDataStore);

        LogicalModelInstantiator logicalModelInstantiator = createLogicalModelGenerator(logicalComponentManager);
        Collector collector = new CollectorImpl();
        return new RuntimeDomain(metaDataStore,
                                 physicalModelGenerator,
                                 logicalModelInstantiator,
                                 logicalComponentManager,
                                 bindingSelector,
                                 routingService,
                                 collector);
    }

    private static LogicalModelInstantiator createLogicalModelGenerator(LogicalComponentManager logicalComponentManager) {
        PromotionResolutionService promotionResolutionService = new DefaultPromotionResolutionService();
        List<TargetResolutionService> targetResolutionServices = new ArrayList<TargetResolutionService>();
        ServiceContractResolver serviceContractResolver = new ServiceContractResolverImpl();
        ExplicitTargetResolutionService explicitTargetResolutionService = new ExplicitTargetResolutionService(serviceContractResolver);
        targetResolutionServices.add(explicitTargetResolutionService);
        TypeBasedAutowireResolutionService autowireResolutionService = new TypeBasedAutowireResolutionService(serviceContractResolver);
        targetResolutionServices.add(autowireResolutionService);
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
        transformerRegistry.register(new String2ListOfQName());

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

        ClassLoaderBuilder classLoaderBuilder = createClassLoaderBuilder(classLoaderRegistry, cpRegistry, metaDataStore, componentManager, info);

        CommandExecutorRegistryImpl commandRegistry = new CommandExecutorRegistryImpl();

        commandRegistry.register(StartContextCommand.class, new StartContextCommandExecutor(scopeRegistry));
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
                                                               ComponentManager componentManager,
                                                               HostInfo info) {

        LocalContributionUriResolver resolver = new LocalContributionUriResolver(metaDataStore);
        JarClasspathProcessor classpathProcessor = new JarClasspathProcessor(cpRegistry);
        classpathProcessor.init();
        return new ClassLoaderBuilderImpl(classLoaderRegistry, resolver, cpRegistry, componentManager, info);
    }

    private static PhysicalModelGenerator createPhysicalModelGenerator(LogicalComponentManager logicalComponentManager,
                                                                       MetaDataStore metaDataStore) {

        GeneratorRegistry generatorRegistry = createGeneratorRegistry();
        PhysicalOperationHelper physicalOperationHelper = new PhysicalOperationHelperImpl();
        PhysicalWireGenerator wireGenerator = new PhysicalWireGeneratorImpl(generatorRegistry, new NullPolicyResolver(), physicalOperationHelper);

        ClassLoaderCommandGenerator classLoaderCommandGenerator = new ClassLoaderCommandGeneratorImpl(metaDataStore);

        List<CommandGenerator> commandGenerators = new ArrayList<CommandGenerator>();
        commandGenerators.add(new BuildComponentCommandGenerator(generatorRegistry, 1));
        commandGenerators.add(new LocalWireCommandGenerator(wireGenerator, logicalComponentManager, 2));
        commandGenerators.add(new ServiceWireCommandGenerator(wireGenerator, 2));
        commandGenerators.add(new ResourceWireCommandGenerator(wireGenerator, 2));
        commandGenerators.add(new StartComponentCommandGenerator(3));
        commandGenerators.add(new StartContextCommandGenerator(4));
        commandGenerators.add(new InitializeComponentCommandGenerator(5));
        return new PhysicalModelGeneratorImpl(commandGenerators, classLoaderCommandGenerator);
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
