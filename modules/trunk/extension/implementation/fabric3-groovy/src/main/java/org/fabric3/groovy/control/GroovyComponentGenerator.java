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
package org.fabric3.groovy.control;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.groovy.provision.GroovyComponentDefinition;
import org.fabric3.groovy.provision.GroovyInstanceFactoryDefinition;
import org.fabric3.groovy.provision.GroovyWireSourceDefinition;
import org.fabric3.groovy.provision.GroovyWireTargetDefinition;
import org.fabric3.groovy.scdl.GroovyImplementation;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.java.InjectableAttributeType;
import org.fabric3.model.type.java.InjectingComponentType;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.pojo.control.InstanceFactoryGenerationHelper;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
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

    public GroovyComponentGenerator(@Reference InstanceFactoryGenerationHelper helper) {
        this.helper = helper;
    }

    public PhysicalComponentDefinition generate(LogicalComponent<GroovyImplementation> component) throws GenerationException {

        ComponentDefinition<GroovyImplementation> definition = component.getDefinition();
        GroovyImplementation implementation = definition.getImplementation();
        InjectingComponentType type = implementation.getComponentType();

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
