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
package org.fabric3.groovy.control;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.groovy.provision.GroovyComponentDefinition;
import org.fabric3.groovy.provision.GroovyInstanceFactoryDefinition;
import org.fabric3.groovy.provision.GroovyWireSourceDefinition;
import org.fabric3.groovy.provision.GroovyWireTargetDefinition;
import org.fabric3.groovy.scdl.GroovyImplementation;
import org.fabric3.pojo.control.InstanceFactoryGenerationHelper;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectableAttributeType;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.policy.Policy;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class GroovyComponentGenerator implements ComponentGenerator<LogicalComponent<GroovyImplementation>> {
    private final InstanceFactoryGenerationHelper helper;

    public GroovyComponentGenerator(@Reference GeneratorRegistry registry, @Reference InstanceFactoryGenerationHelper helper) {
        registry.register(GroovyImplementation.class, this);
        this.helper = helper;
    }

    public PhysicalComponentDefinition generate(LogicalComponent<GroovyImplementation> component) throws GenerationException {

        ComponentDefinition<GroovyImplementation> definition = component.getDefinition();
        GroovyImplementation implementation = definition.getImplementation();
        PojoComponentType type = implementation.getComponentType();

        // create the instance factory definition
        GroovyInstanceFactoryDefinition providerDefinition = new GroovyInstanceFactoryDefinition();
        providerDefinition.setConstructor(type.getConstructor());
        providerDefinition.setInitMethod(type.getInitMethod());
        providerDefinition.setDestroyMethod(type.getDestroyMethod());
        providerDefinition.setImplementationClass(implementation.getClassName());
        providerDefinition.setScriptName(implementation.getScriptName());
        helper.processInjectionSites(component, providerDefinition);

        // create the physical component definition
        GroovyComponentDefinition physical = new GroovyComponentDefinition();
        physical.setScope(type.getScope());
        physical.setInitLevel(helper.getInitLevel(definition, type));
        physical.setProviderDefinition(providerDefinition);
        helper.processPropertyValues(component, physical);
        // generate the classloader resource definition

        return physical;
    }

    public PhysicalWireSourceDefinition generateWireSource(LogicalComponent<GroovyImplementation> source,
                                                           LogicalReference reference,
                                                           Policy policy)
            throws GenerationException {
        URI uri = reference.getUri();
        ServiceContract<?> serviceContract = reference.getDefinition().getServiceContract();
        String interfaceName = serviceContract.getQualifiedInterfaceName();

        GroovyWireSourceDefinition wireDefinition = new GroovyWireSourceDefinition();
        wireDefinition.setUri(uri);
        wireDefinition.setValueSource(new InjectableAttribute(InjectableAttributeType.REFERENCE, uri.getFragment()));
        wireDefinition.setInterfaceName(interfaceName);
        // assume for now that any wire from a Groovy component can be optimized
        wireDefinition.setOptimizable(true);

        return wireDefinition;
    }

    public PhysicalWireSourceDefinition generateCallbackWireSource(LogicalComponent<GroovyImplementation> source,
                                                                   ServiceContract<?> serviceContract,
                                                                   Policy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service,
                                                           LogicalComponent<GroovyImplementation> target,
                                                           Policy policy) throws GenerationException {
        GroovyWireTargetDefinition wireDefinition = new GroovyWireTargetDefinition();
        URI uri;
        if (service != null) {
            uri = service.getUri();
        } else {
            // no service specified, use the default
            uri = target.getUri();
        }
        wireDefinition.setUri(uri);
        return wireDefinition;
    }

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<GroovyImplementation> source, LogicalResource<?> resource)
            throws GenerationException {
        GroovyWireSourceDefinition wireDefinition = new GroovyWireSourceDefinition();
        wireDefinition.setUri(resource.getUri());
        return wireDefinition;
    }
}
