/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.fabric.synthesizer;

import java.net.URI;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.instantiator.ComponentInstantiator;
import org.fabric3.fabric.instantiator.InstantiationContext;
import static org.fabric3.host.Names.BOOT_CONTRIBUTION;
import org.fabric3.host.domain.AssemblyException;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.Scope;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.contract.ContractProcessor;
import org.fabric3.spi.introspection.java.ImplementationProcessor;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.services.componentmanager.RegistrationException;
import org.fabric3.spi.services.lcm.LogicalComponentManager;
import org.fabric3.spi.synthesize.ComponentRegistrationException;
import org.fabric3.spi.synthesize.ComponentSynthesizer;
import org.fabric3.spi.synthesize.InvalidServiceContractException;
import org.fabric3.system.scdl.SystemImplementation;
import org.fabric3.system.singleton.SingletonComponent;
import org.fabric3.system.singleton.SingletonImplementation;

/**
 * Implementation that synthesizes a singleton component from an existing object instance.
 *
 * @version $Revision$ $Date$
 */
public class SingletonComponentSynthesizer implements ComponentSynthesizer {

    private ImplementationProcessor<SystemImplementation> implementationProcessor;
    private ComponentInstantiator instantiator;
    private LogicalComponentManager lcm;
    private ComponentManager componentManager;
    private ContractProcessor contractProcessor;
    private ScopeContainer scopeContainer;

    @Constructor
    public SingletonComponentSynthesizer(@Reference ImplementationProcessor<SystemImplementation> implementationProcessor,
                                         @Reference ComponentInstantiator instantiator,
                                         @Reference LogicalComponentManager lcm,
                                         @Reference ComponentManager componentManager,
                                         @Reference ContractProcessor contractProcessor,
                                         @Reference ScopeRegistry registry) {
        this(implementationProcessor, instantiator, lcm, componentManager, contractProcessor, registry.getScopeContainer(Scope.COMPOSITE));
    }

    public SingletonComponentSynthesizer(ImplementationProcessor<SystemImplementation> implementationProcessor,
                                         ComponentInstantiator instantiator,
                                         LogicalComponentManager lcm,
                                         ComponentManager componentManager,
                                         ContractProcessor contractProcessor,
                                         ScopeContainer scopeContainer) {
        this.implementationProcessor = implementationProcessor;
        this.instantiator = instantiator;
        this.lcm = lcm;
        this.componentManager = componentManager;
        this.contractProcessor = contractProcessor;
        this.scopeContainer = scopeContainer;
    }

    public <S, I extends S> void registerComponent(String name, Class<S> type, I instance, boolean introspect) throws ComponentRegistrationException {
        try {
            LogicalComponent<?> logical = createLogicalComponent(name, type, instance, introspect);
            AtomicComponent<I> physical = createPhysicalComponent(logical, instance);
            lcm.getRootComponent().addComponent(logical);
            componentManager.register(physical);
            scopeContainer.register(physical);
        } catch (RegistrationException e) {
            throw new ComponentRegistrationException(e);
        } catch (AssemblyException e) {
            throw new ComponentRegistrationException(e);
        }
    }


    private <S, I extends S> LogicalComponent<Implementation<?>> createLogicalComponent(String name, Class<S> type, I instance, boolean introspect)
            throws InvalidServiceContractException, AssemblyException {
        LogicalCompositeComponent domain = lcm.getRootComponent();
        ComponentDefinition<Implementation<?>> definition = createDefinition(name, type, instance, introspect);
        InstantiationContext context = new InstantiationContext(domain);
        LogicalComponent<Implementation<?>> logical = instantiator.instantiate(domain, domain.getPropertyValues(), definition, context);
        if (context.hasErrors()) {
            throw new AssemblyException(context.getErrors());
        }
        // mark singleton components as provisioned since instances are not created
        logical.setState(LogicalState.PROVISIONED);
        // all references are initially resolved since they are manually injected
        for (LogicalReference reference : logical.getReferences()) {
            reference.setResolved(true);
            for (LogicalWire wire : reference.getWires()) {
                wire.setState(LogicalState.PROVISIONED);
            }
        }
        return logical;
    }

    private <S, I extends S> ComponentDefinition<Implementation<?>> createDefinition(String name, Class<S> type, I instance, boolean introspect)
            throws InvalidServiceContractException {

        String implClassName = instance.getClass().getName();

        TypeMapping mapping = new TypeMapping();
        IntrospectionContext context = new DefaultIntrospectionContext(getClass().getClassLoader(), null, null, null, mapping);
        if (introspect) {
            // introspect the instance so it may be injected by the runtime with additional services
            SystemImplementation implementation = new SystemImplementation();
            implementation.setImplementationClass(implClassName);
            implementationProcessor.introspect(implementation, context);
            ComponentDefinition<Implementation<?>> def = new ComponentDefinition<Implementation<?>>(name);
            SingletonImplementation singletonImplementation = new SingletonImplementation(implementation.getComponentType(), implClassName);
            def.setImplementation(singletonImplementation);
            def.setContributionUri(BOOT_CONTRIBUTION);
            return def;
        } else {
            // instance does not have any services injected
            ServiceContract<?> contract = contractProcessor.introspect(mapping, type, context);
            if (context.hasErrors()) {
                throw new InvalidServiceContractException(context.getErrors());
            }
            String serviceName = contract.getInterfaceName();
            ServiceDefinition service = new ServiceDefinition(serviceName, contract);

            PojoComponentType componentType = new PojoComponentType(implClassName);
            componentType.add(service);

            SingletonImplementation implementation = new SingletonImplementation(componentType, implClassName);
            implementation.setComponentType(componentType);
            ComponentDefinition<Implementation<?>> def = new ComponentDefinition<Implementation<?>>(name);
            def.setImplementation(implementation);
            def.setContributionUri(BOOT_CONTRIBUTION);
            return def;
        }
    }

    private <I> AtomicComponent<I> createPhysicalComponent(LogicalComponent<?> logicalComponent, I instance) {
        URI uri = logicalComponent.getUri();
        PojoComponentType type = (PojoComponentType) logicalComponent.getDefinition().getComponentType();
        type.getInjectionSites();
        SingletonComponent<I> component = new SingletonComponent<I>(uri, instance, type.getInjectionSites());
        component.setClassLoaderId(BOOT_CONTRIBUTION);
        return component;
    }


}
