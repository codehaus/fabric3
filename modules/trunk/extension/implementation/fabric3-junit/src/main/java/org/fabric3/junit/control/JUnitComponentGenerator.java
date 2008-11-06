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
package org.fabric3.junit.control;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.java.provision.JavaComponentDefinition;
import org.fabric3.java.provision.JavaWireSourceDefinition;
import org.fabric3.java.provision.JavaWireTargetDefinition;
import org.fabric3.junit.scdl.JUnitImplementation;
import org.fabric3.pojo.control.InstanceFactoryGenerationHelper;
import org.fabric3.pojo.provision.InstanceFactoryDefinition;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectableAttributeType;
import org.fabric3.scdl.Scope;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.InteractionType;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.policy.Policy;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class JUnitComponentGenerator implements ComponentGenerator<LogicalComponent<JUnitImplementation>> {

    private final GeneratorRegistry registry;
    private final InstanceFactoryGenerationHelper helper;

    public JUnitComponentGenerator(@Reference GeneratorRegistry registry, @Reference InstanceFactoryGenerationHelper helper) {
        this.registry = registry;
        this.helper = helper;
    }

    @Init
    public void init() {
        registry.register(JUnitImplementation.class, this);
    }

    public PhysicalComponentDefinition generate(LogicalComponent<JUnitImplementation> component)
            throws GenerationException {

        ComponentDefinition<JUnitImplementation> definition = component.getDefinition();
        JUnitImplementation implementation = definition.getImplementation();
        PojoComponentType type = implementation.getComponentType();
        Integer level = helper.getInitLevel(definition, type);
        String scope = type.getScope();

        InstanceFactoryDefinition providerDefinition = new InstanceFactoryDefinition();
        providerDefinition.setReinjectable(Scope.COMPOSITE.getScope().equals(scope));
        providerDefinition.setConstructor(type.getConstructor());
        providerDefinition.setInitMethod(type.getInitMethod());
        providerDefinition.setDestroyMethod(type.getDestroyMethod());
        providerDefinition.setImplementationClass(implementation.getImplementationClass());
        helper.processInjectionSites(component, providerDefinition);

        JavaComponentDefinition physical = new JavaComponentDefinition();

        URI classLoaderId = component.getClassLoaderId();
        physical.setClassLoaderId(classLoaderId);

        physical.setScope(scope);
        physical.setInitLevel(level);
        physical.setProviderDefinition(providerDefinition);
        helper.processPropertyValues(component, physical);
        return physical;
    }

    public PhysicalWireSourceDefinition generateWireSource(LogicalComponent<JUnitImplementation> source,
                                                           LogicalReference reference,
                                                           Policy policy) throws GenerationException {
        URI uri = reference.getUri();
        ServiceContract<?> serviceContract = reference.getDefinition().getServiceContract();
        String interfaceName = getInterfaceName(serviceContract);
        URI classLoaderId = source.getClassLoaderId();

        JavaWireSourceDefinition wireDefinition = new JavaWireSourceDefinition();
        wireDefinition.setUri(uri);
        wireDefinition.setValueSource(new InjectableAttribute(InjectableAttributeType.REFERENCE, uri.getFragment()));
        wireDefinition.setInterfaceName(interfaceName);
        if (serviceContract.isConversational()) {
            wireDefinition.setInteractionType(InteractionType.CONVERSATIONAL);
        }

        // assume for now that any wire from a JUnit component can be optimized
        wireDefinition.setOptimizable(true);

        wireDefinition.setClassLoaderId(classLoaderId);
        return wireDefinition;
    }

    public PhysicalWireSourceDefinition generateCallbackWireSource(LogicalComponent<JUnitImplementation> source,
                                                                   ServiceContract<?> serviceContract,
                                                                   Policy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<JUnitImplementation> source,
                                                                   LogicalResource<?> resource)
            throws GenerationException {

        URI uri = resource.getUri();
        ServiceContract<?> serviceContract = resource.getResourceDefinition().getServiceContract();
        String interfaceName = getInterfaceName(serviceContract);
        URI classLoaderId = source.getClassLoaderId();

        JavaWireSourceDefinition wireDefinition = new JavaWireSourceDefinition();
        wireDefinition.setUri(uri);
        wireDefinition.setValueSource(new InjectableAttribute(InjectableAttributeType.RESOURCE, uri.getFragment()));
        wireDefinition.setClassLoaderId(classLoaderId);
        wireDefinition.setInterfaceName(interfaceName);
        return wireDefinition;
    }

    private String getInterfaceName(ServiceContract<?> contract) {
        return contract.getQualifiedInterfaceName();
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service,
                                                           LogicalComponent<JUnitImplementation> target,
                                                           Policy policy) throws GenerationException {
        JavaWireTargetDefinition wireDefinition = new JavaWireTargetDefinition();
        wireDefinition.setUri(service.getUri());
        URI classLoaderId = target.getClassLoaderId();
        wireDefinition.setClassLoaderId(classLoaderId);
        return wireDefinition;
    }
}
