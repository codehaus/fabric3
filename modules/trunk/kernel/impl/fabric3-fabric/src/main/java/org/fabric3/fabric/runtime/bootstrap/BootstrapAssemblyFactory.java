/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.fabric.runtime.bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.management.MBeanServer;

import org.fabric3.contribution.ClasspathProcessorRegistryImpl;
import org.fabric3.contribution.LocalContributionUriResolver;
import org.fabric3.contribution.archive.JarClasspathProcessor;
import org.fabric3.contribution.generator.JavaContributionWireGeneratorImpl;
import org.fabric3.contribution.generator.LocationContributionWireGeneratorImpl;
import org.fabric3.contribution.wire.JavaContributionWire;
import org.fabric3.contribution.wire.LocationContributionWire;
import org.fabric3.fabric.binding.BindingSelector;
import org.fabric3.fabric.binding.BindingSelectorImpl;
import org.fabric3.fabric.builder.ConnectorImpl;
import org.fabric3.fabric.builder.classloader.ClassLoaderBuilder;
import org.fabric3.fabric.builder.classloader.ClassLoaderBuilderImpl;
import org.fabric3.fabric.builder.classloader.ClassLoaderWireBuilderImpl;
import org.fabric3.fabric.builder.component.DefaultComponentBuilderRegistry;
import org.fabric3.fabric.collector.Collector;
import org.fabric3.fabric.collector.CollectorImpl;
import org.fabric3.fabric.command.AttachWireCommand;
import org.fabric3.fabric.command.BuildComponentCommand;
import org.fabric3.fabric.command.ConnectionCommand;
import org.fabric3.fabric.command.ProvisionClassloaderCommand;
import org.fabric3.fabric.command.StartComponentCommand;
import org.fabric3.fabric.command.StartContextCommand;
import org.fabric3.fabric.documentloader.DocumentLoader;
import org.fabric3.fabric.documentloader.DocumentLoaderImpl;
import org.fabric3.fabric.domain.ContributionHelper;
import org.fabric3.fabric.domain.ContributionHelperImpl;
import org.fabric3.fabric.domain.LocalRoutingService;
import org.fabric3.fabric.domain.RuntimeDomain;
import org.fabric3.fabric.executor.AttachWireCommandExecutor;
import org.fabric3.fabric.executor.BuildComponentCommandExecutor;
import org.fabric3.fabric.executor.CommandExecutorRegistryImpl;
import org.fabric3.fabric.executor.ProvisionClassloaderCommandExecutor;
import org.fabric3.fabric.executor.ReferenceConnectionCommandExecutor;
import org.fabric3.fabric.executor.StartComponentCommandExecutor;
import org.fabric3.fabric.executor.StartContextCommandExecutor;
import org.fabric3.fabric.generator.Generator;
import org.fabric3.fabric.generator.GeneratorRegistry;
import org.fabric3.fabric.generator.classloader.ClassLoaderCommandGenerator;
import org.fabric3.fabric.generator.classloader.ClassLoaderCommandGeneratorImpl;
import org.fabric3.fabric.generator.collator.ContributionCollator;
import org.fabric3.fabric.generator.collator.ContributionCollatorImpl;
import org.fabric3.fabric.generator.component.BuildComponentCommandGenerator;
import org.fabric3.fabric.generator.component.StartComponentCommandGenerator;
import org.fabric3.fabric.generator.context.StartContextCommandGenerator;
import org.fabric3.fabric.generator.context.StartContextCommandGeneratorImpl;
import org.fabric3.fabric.generator.context.StopContextCommandGenerator;
import org.fabric3.fabric.generator.context.StopContextCommandGeneratorImpl;
import org.fabric3.fabric.generator.impl.GeneratorImpl;
import org.fabric3.fabric.generator.impl.GeneratorRegistryImpl;
import org.fabric3.fabric.generator.wire.LocalWireCommandGenerator;
import org.fabric3.fabric.generator.wire.PhysicalOperationMapper;
import org.fabric3.fabric.generator.wire.PhysicalOperationMapperImpl;
import org.fabric3.fabric.generator.wire.ResourceWireCommandGenerator;
import org.fabric3.fabric.generator.wire.ServiceWireCommandGenerator;
import org.fabric3.fabric.generator.wire.WireGenerator;
import org.fabric3.fabric.generator.wire.WireGeneratorImpl;
import org.fabric3.fabric.instantiator.LogicalModelInstantiator;
import org.fabric3.fabric.instantiator.LogicalModelInstantiatorImpl;
import org.fabric3.fabric.instantiator.PromotionNormalizer;
import org.fabric3.fabric.instantiator.PromotionResolutionService;
import org.fabric3.fabric.instantiator.ResolutionService;
import org.fabric3.fabric.instantiator.ResolutionServiceImpl;
import org.fabric3.fabric.instantiator.WireInstantiator;
import org.fabric3.fabric.instantiator.component.AtomicComponentInstantiator;
import org.fabric3.fabric.instantiator.component.CompositeComponentInstantiator;
import org.fabric3.fabric.instantiator.component.WireInstantiatorImpl;
import org.fabric3.fabric.instantiator.normalize.PromotionNormalizerImpl;
import org.fabric3.fabric.instantiator.promotion.DefaultPromotionResolutionService;
import org.fabric3.fabric.instantiator.target.ExplicitTargetResolutionService;
import org.fabric3.fabric.instantiator.target.ServiceContractResolver;
import org.fabric3.fabric.instantiator.target.ServiceContractResolverImpl;
import org.fabric3.fabric.instantiator.target.TypeBasedAutowireResolutionService;
import org.fabric3.fabric.monitor.MonitorResource;
import org.fabric3.fabric.monitor.MonitorTargetDefinition;
import org.fabric3.fabric.monitor.MonitorWireAttacher;
import org.fabric3.fabric.monitor.MonitorWireGenerator;
import org.fabric3.fabric.policy.NullPolicyAttacher;
import org.fabric3.fabric.policy.NullPolicyResolver;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.jmx.control.JMXBindingGenerator;
import org.fabric3.jmx.provision.JMXSourceDefinition;
import org.fabric3.jmx.runtime.JMXWireAttacher;
import org.fabric3.pojo.generator.GenerationHelperImpl;
import org.fabric3.pojo.instancefactory.BuildHelperImpl;
import org.fabric3.pojo.instancefactory.DefaultInstanceFactoryBuilderRegistry;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuildHelper;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.reflection.ReflectiveInstanceFactoryBuilder;
import org.fabric3.spi.builder.classloader.ClassLoaderWireBuilder;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.contribution.ContributionUriResolver;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.generator.ClassLoaderWireGenerator;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.model.type.JMXBinding;
import org.fabric3.spi.policy.PolicyAttacher;
import org.fabric3.spi.policy.PolicyResolver;
import org.fabric3.spi.transform.PullTransformerRegistry;
import org.fabric3.system.generator.SystemComponentGenerator;
import org.fabric3.system.model.SystemImplementation;
import org.fabric3.system.provision.SystemComponentDefinition;
import org.fabric3.system.provision.SystemSourceDefinition;
import org.fabric3.system.provision.SystemTargetDefinition;
import org.fabric3.system.runtime.SystemComponentBuilder;
import org.fabric3.system.runtime.SystemSourceWireAttacher;
import org.fabric3.system.runtime.SystemTargetWireAttacher;
import org.fabric3.system.singleton.SingletonComponentGenerator;
import org.fabric3.system.singleton.SingletonImplementation;
import org.fabric3.system.singleton.SingletonSourceDefinition;
import org.fabric3.system.singleton.SingletonSourceWireAttacher;
import org.fabric3.system.singleton.SingletonTargetDefinition;
import org.fabric3.system.singleton.SingletonTargetWireAttacher;
import org.fabric3.transform.DefaultPullTransformerRegistry;
import org.fabric3.transform.dom2java.String2Boolean;
import org.fabric3.transform.dom2java.String2Class;
import org.fabric3.transform.dom2java.String2Integer;
import org.fabric3.transform.dom2java.String2QName;
import org.fabric3.transform.dom2java.String2String;
import org.fabric3.transform.dom2java.generics.list.String2ListOfQName;
import org.fabric3.transform.dom2java.generics.list.String2ListOfString;
import org.fabric3.transform.dom2java.generics.map.String2MapOfString2String;

/**
 * Bootstraps services required for instantiation, generation, and deployment.
 *
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
        PolicyAttacher policyAttacher = new NullPolicyAttacher();
        PolicyResolver policyResolver = new NullPolicyResolver();
        Generator generator = createGenerator(logicalComponentManager, metaDataStore, policyResolver);

        LogicalModelInstantiator logicalModelInstantiator = createLogicalModelGenerator(logicalComponentManager);
        Collector collector = new CollectorImpl();
        ContributionHelper contributionHelper = new ContributionHelperImpl(metaDataStore);

        return new RuntimeDomain(metaDataStore,
                                 generator,
                                 logicalModelInstantiator,
                                 policyAttacher,
                                 logicalComponentManager,
                                 bindingSelector,
                                 routingService,
                                 collector,
                                 contributionHelper,
                                 info);
    }

    private static LogicalModelInstantiator createLogicalModelGenerator(LogicalComponentManager logicalComponentManager) {
        PromotionResolutionService promotionResolutionService = new DefaultPromotionResolutionService();
        ServiceContractResolver serviceContractResolver = new ServiceContractResolverImpl();
        ExplicitTargetResolutionService explicitTargetResolutionService = new ExplicitTargetResolutionService(serviceContractResolver);
        TypeBasedAutowireResolutionService autowireResolutionService = new TypeBasedAutowireResolutionService(serviceContractResolver);
        ResolutionService resolutionService =
                new ResolutionServiceImpl(promotionResolutionService, explicitTargetResolutionService, autowireResolutionService);

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

        PullTransformerRegistry transformerRegistry = new DefaultPullTransformerRegistry();
        transformerRegistry.register(new String2String());
        transformerRegistry.register(new String2Integer());
        transformerRegistry.register(new String2Boolean());
        transformerRegistry.register(new String2MapOfString2String());
        transformerRegistry.register(new String2Class(classLoaderRegistry));
        transformerRegistry.register(new String2QName());
        transformerRegistry.register(new String2ListOfString());
        transformerRegistry.register(new String2ListOfQName());

        DefaultComponentBuilderRegistry registry = new DefaultComponentBuilderRegistry();

        SystemComponentBuilder<?> builder = new SystemComponentBuilder<Object>(scopeRegistry,
                                                                               providerRegistry,
                                                                               classLoaderRegistry,
                                                                               transformerRegistry);

        registry.register(SystemComponentDefinition.class, builder);

        Map<Class<? extends PhysicalSourceDefinition>, SourceWireAttacher<? extends PhysicalSourceDefinition>> sourceAttachers =
                new ConcurrentHashMap<Class<? extends PhysicalSourceDefinition>, SourceWireAttacher<? extends PhysicalSourceDefinition>>();
        sourceAttachers.put(SystemSourceDefinition.class,
                            new SystemSourceWireAttacher(componentManager, transformerRegistry, classLoaderRegistry));
        sourceAttachers.put(SingletonSourceDefinition.class, new SingletonSourceWireAttacher(componentManager));

        sourceAttachers.put(JMXSourceDefinition.class, new JMXWireAttacher(mbeanServer, classLoaderRegistry, jmxSubDomain));

        Map<Class<? extends PhysicalTargetDefinition>, TargetWireAttacher<? extends PhysicalTargetDefinition>> targetAttachers =
                new ConcurrentHashMap<Class<? extends PhysicalTargetDefinition>, TargetWireAttacher<? extends PhysicalTargetDefinition>>();
        targetAttachers.put(SingletonTargetDefinition.class, new SingletonTargetWireAttacher(componentManager));
        targetAttachers.put(SystemTargetDefinition.class, new SystemTargetWireAttacher(componentManager, classLoaderRegistry));
        targetAttachers.put(MonitorTargetDefinition.class, new MonitorWireAttacher(monitorFactory, classLoaderRegistry));

        ConnectorImpl connector = new ConnectorImpl();
        connector.setSourceAttachers(sourceAttachers);
        connector.setTargetAttachers(targetAttachers);

        ClasspathProcessorRegistry cpRegistry = new ClasspathProcessorRegistryImpl();

        ClassLoaderBuilder classLoaderBuilder = createClassLoaderBuilder(classLoaderRegistry, cpRegistry, metaDataStore, componentManager, info);

        CommandExecutorRegistryImpl commandRegistry = new CommandExecutorRegistryImpl();

        commandRegistry.register(StartContextCommand.class, new StartContextCommandExecutor(scopeRegistry));
        commandRegistry.register(BuildComponentCommand.class, new BuildComponentCommandExecutor(registry, componentManager));
        commandRegistry.register(AttachWireCommand.class, new AttachWireCommandExecutor(connector));
        commandRegistry.register(StartComponentCommand.class, new StartComponentCommandExecutor(componentManager));
        commandRegistry.register(ProvisionClassloaderCommand.class, new ProvisionClassloaderCommandExecutor(classLoaderBuilder));
        commandRegistry.register(ConnectionCommand.class, new ReferenceConnectionCommandExecutor(commandRegistry));

        return commandRegistry;

    }

    private static ClassLoaderBuilder createClassLoaderBuilder(ClassLoaderRegistry classLoaderRegistry,
                                                               ClasspathProcessorRegistry cpRegistry,
                                                               MetaDataStore metaDataStore,
                                                               ComponentManager componentManager,
                                                               HostInfo info) {

        LocalContributionUriResolver resolver = new LocalContributionUriResolver(metaDataStore);
        Map<String, ContributionUriResolver> resolvers = new HashMap<String, ContributionUriResolver>();
        resolvers.put(ContributionUriResolver.LOCAL_SCHEME, resolver);
        JarClasspathProcessor classpathProcessor = new JarClasspathProcessor(cpRegistry, info);
        classpathProcessor.init();
        ClassLoaderWireBuilder wireBuilder = new ClassLoaderWireBuilderImpl(classLoaderRegistry);
        ClassLoaderBuilderImpl builder = new ClassLoaderBuilderImpl(wireBuilder, classLoaderRegistry, cpRegistry, componentManager, info);
        builder.setContributionUriResolver(resolvers);
        return builder;
    }

    private static Generator createGenerator(LogicalComponentManager lcm, MetaDataStore metaDataStore, PolicyResolver policyResolver) {

        GeneratorRegistry generatorRegistry = createGeneratorRegistry();
        PhysicalOperationMapper mapper = new PhysicalOperationMapperImpl();
        WireGenerator wireGenerator = new WireGeneratorImpl(generatorRegistry, policyResolver, mapper);

        ClassLoaderWireGenerator<?> javaGenerator = new JavaContributionWireGeneratorImpl();
        ClassLoaderWireGenerator<?> locationGenerator = new LocationContributionWireGeneratorImpl();
        Map<Class<? extends ContributionWire<?, ?>>, ClassLoaderWireGenerator<?>> generators =
                new HashMap<Class<? extends ContributionWire<?, ?>>, ClassLoaderWireGenerator<?>>();
        generators.put(JavaContributionWire.class, javaGenerator);
        generators.put(LocationContributionWire.class, locationGenerator);
        ClassLoaderCommandGenerator classLoaderCommandGenerator = new ClassLoaderCommandGeneratorImpl(generators);

        List<CommandGenerator> commandGenerators = new ArrayList<CommandGenerator>();
        commandGenerators.add(new BuildComponentCommandGenerator(generatorRegistry, 1));
        commandGenerators.add(new LocalWireCommandGenerator(wireGenerator, lcm, 2));
        commandGenerators.add(new ServiceWireCommandGenerator(wireGenerator, 2));
        commandGenerators.add(new ResourceWireCommandGenerator(wireGenerator, 2));
        commandGenerators.add(new StartComponentCommandGenerator(3));
        StopContextCommandGenerator stopContextGenerator = new StopContextCommandGeneratorImpl();
        StartContextCommandGenerator startContextGenerator = new StartContextCommandGeneratorImpl();
        ContributionCollator collator = new ContributionCollatorImpl(metaDataStore);
        return new GeneratorImpl(commandGenerators, collator, classLoaderCommandGenerator, startContextGenerator, stopContextGenerator);
    }

    @SuppressWarnings({"unchecked"})
    private static GeneratorRegistry createGeneratorRegistry() {
        GeneratorRegistryImpl registry = new GeneratorRegistryImpl();
        GenerationHelperImpl helper = new GenerationHelperImpl();
        ComponentGenerator systemComponentGenerator = new SystemComponentGenerator(helper);
        ComponentGenerator singletonComponentGenerator = new SingletonComponentGenerator();
        registry.register(SystemImplementation.class, systemComponentGenerator);
        registry.register(SingletonImplementation.class, singletonComponentGenerator);
        registry.register(JMXBinding.class, new JMXBindingGenerator());
        registry.register(MonitorResource.class, new MonitorWireGenerator());
        return registry;
    }

}
