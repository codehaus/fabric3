package org.fabric3.junit;

import java.net.URI;

import org.fabric3.java.JavaComponentDefinition;
import org.fabric3.java.JavaWireSourceDefinition;
import org.fabric3.java.JavaWireTargetDefinition;
import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.pojo.instancefactory.InstanceFactoryGenerationHelper;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.scdl.ValueSource;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.policy.Policy;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class JUnitComponentGenerator implements ComponentGenerator<LogicalComponent<ImplementationJUnit>> {

    private final GeneratorRegistry registry;
    private final ClassLoaderGenerator classLoaderGenerator;
    private final InstanceFactoryGenerationHelper helper;

    public JUnitComponentGenerator(@Reference GeneratorRegistry registry,
                                   @Reference ClassLoaderGenerator classLoaderGenerator,
                                   @Reference InstanceFactoryGenerationHelper helper) {
        this.registry = registry;
        this.classLoaderGenerator = classLoaderGenerator;
        this.helper = helper;
    }

    @Init
    public void init() {
        registry.register(ImplementationJUnit.class, this);
    }

    public PhysicalComponentDefinition generate(LogicalComponent<ImplementationJUnit> component,
                                                GeneratorContext context)
            throws GenerationException {

        ComponentDefinition<ImplementationJUnit> definition = component.getDefinition();
        ImplementationJUnit implementation = definition.getImplementation();
        PojoComponentType type = implementation.getComponentType();
        Integer level = helper.getInitLevel(definition, type);
        URI componentId = component.getUri();

        InstanceFactoryDefinition providerDefinition = new InstanceFactoryDefinition();
        providerDefinition.setConstructor(type.getConstructorDefinition().getSignature());
        providerDefinition.setInitMethod(type.getInitMethod());
        providerDefinition.setDestroyMethod(type.getDestroyMethod());
        providerDefinition.setImplementationClass(implementation.getImplementationClass());
        helper.processInjectionSites(component, providerDefinition);

        JavaComponentDefinition physical = new JavaComponentDefinition();
        physical.setGroupId(component.getParent().getUri());
        physical.setComponentId(componentId);

        URI classLoaderId = classLoaderGenerator.generate(component, context);
        physical.setClassLoaderId(classLoaderId);

        physical.setScope(type.getImplementationScope());
        physical.setInitLevel(level);
        physical.setInstanceFactoryProviderDefinition(providerDefinition);
        helper.processPropertyValues(component, physical);
        return physical;
    }

    public PhysicalWireSourceDefinition generateWireSource(LogicalComponent<ImplementationJUnit> source,
                                                           LogicalReference reference,
                                                           Policy policy,
                                                           GeneratorContext context) throws GenerationException {
        URI uri = reference.getUri();
        ServiceContract<?> serviceContract = reference.getDefinition().getServiceContract();
        String interfaceName = getInterfaceName(serviceContract);
        URI classLoaderId = classLoaderGenerator.generate(source, context);

        JavaWireSourceDefinition wireDefinition = new JavaWireSourceDefinition();
        wireDefinition.setUri(uri);
        wireDefinition.setValueSource(new ValueSource(ValueSource.ValueSourceType.REFERENCE, uri.getFragment()));
        wireDefinition.setInterfaceName(interfaceName);
        wireDefinition.setConversational(serviceContract.isConversational());

        // assume for now that any wire from a JUnit component can be optimized
        wireDefinition.setOptimizable(true);

        wireDefinition.setClassLoaderId(classLoaderId);
        return wireDefinition;
    }

    public PhysicalWireSourceDefinition generateCallbackWireSource(LogicalComponent<ImplementationJUnit> source,
                                                                   ServiceContract<?> serviceContract,
                                                                   Policy policy,
                                                                   GeneratorContext context) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<ImplementationJUnit> source,
                                                                   LogicalResource<?> resource,
                                                                   GeneratorContext context)
            throws GenerationException {

        URI uri = resource.getUri();
        ServiceContract<?> serviceContract = resource.getResourceDefinition().getServiceContract();
        String interfaceName = getInterfaceName(serviceContract);
        URI classLoaderId = classLoaderGenerator.generate(source, context);

        JavaWireSourceDefinition wireDefinition = new JavaWireSourceDefinition();
        wireDefinition.setUri(uri);
        wireDefinition.setValueSource(new ValueSource(ValueSource.ValueSourceType.RESOURCE, uri.getFragment()));
        wireDefinition.setConversational(false);
        wireDefinition.setClassLoaderId(classLoaderId);
        wireDefinition.setInterfaceName(interfaceName);
        return wireDefinition;
    }

    private String getInterfaceName(ServiceContract<?> contract) {
        return contract.getQualifiedInterfaceName();
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service,
                                                           LogicalComponent<ImplementationJUnit> target,
                                                           Policy policy,
                                                           GeneratorContext context) throws GenerationException {
        JavaWireTargetDefinition wireDefinition = new JavaWireTargetDefinition();
        wireDefinition.setUri(service.getUri());
        return wireDefinition;
    }
}
