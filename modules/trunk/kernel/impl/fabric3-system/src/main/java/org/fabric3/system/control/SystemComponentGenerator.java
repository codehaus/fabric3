/* 
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version, with the following exception:
 *
 * Linking this software statically or dynamically with other modules is making a combined
 * work based on this software. Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software give you permission to
 * link this software with independent modules to produce an executable, regardless of the
 * license terms of these independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that module. An independent
 * module is a module which is not derived from or based on this software. If you modify this
 * software, you may extend this exception to your version of the software, but you are not
 * obligated to do so. If you do not wish to do so, delete this exception statement from
 * your version.
 *
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.system.control;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.java.InjectableAttributeType;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.pojo.control.InstanceFactoryGenerationHelper;
import org.fabric3.pojo.provision.InstanceFactoryDefinition;
import org.fabric3.pojo.scdl.PojoComponentType;
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
import org.fabric3.system.provision.SystemComponentDefinition;
import org.fabric3.system.provision.SystemWireSourceDefinition;
import org.fabric3.system.provision.SystemWireTargetDefinition;
import org.fabric3.system.scdl.SystemImplementation;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class SystemComponentGenerator implements ComponentGenerator<LogicalComponent<SystemImplementation>> {

    private final InstanceFactoryGenerationHelper helper;

    public SystemComponentGenerator(@Reference InstanceFactoryGenerationHelper helper) {
        this.helper = helper;
    }

    public PhysicalComponentDefinition generate(LogicalComponent<SystemImplementation> component) throws GenerationException {
        ComponentDefinition<SystemImplementation> definition = component.getDefinition();
        SystemImplementation implementation = definition.getImplementation();
        PojoComponentType type = implementation.getComponentType();

        InstanceFactoryDefinition providerDefinition = new InstanceFactoryDefinition();
        providerDefinition.setReinjectable(true);
        providerDefinition.setConstructor(type.getConstructor());
        providerDefinition.setInitMethod(type.getInitMethod());
        providerDefinition.setDestroyMethod(type.getDestroyMethod());
        providerDefinition.setImplementationClass(implementation.getImplementationClass());
        helper.processInjectionSites(component, providerDefinition);

        // create the physical component definition
        SystemComponentDefinition physical = new SystemComponentDefinition();
        physical.setScope(type.getScope());
        physical.setInitLevel(helper.getInitLevel(definition, type));
        physical.setProviderDefinition(providerDefinition);
        helper.processPropertyValues(component, physical);

        return physical;
    }

    public PhysicalWireSourceDefinition generateWireSource(LogicalComponent<SystemImplementation> source, LogicalReference reference, Policy policy)
            throws GenerationException {

        URI uri = reference.getUri();
        SystemWireSourceDefinition wireDefinition = new SystemWireSourceDefinition();
        wireDefinition.setOptimizable(true);
        wireDefinition.setUri(uri);
        wireDefinition.setValueSource(new InjectableAttribute(InjectableAttributeType.REFERENCE, uri.getFragment()));
        ServiceContract<?> serviceContract = reference.getDefinition().getServiceContract();
        String interfaceName = serviceContract.getQualifiedInterfaceName();
        wireDefinition.setInterfaceName(interfaceName);

        return wireDefinition;
    }

    public PhysicalWireSourceDefinition generateCallbackWireSource(LogicalComponent<SystemImplementation> source,
                                                                   ServiceContract<?> serviceContract,
                                                                   Policy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service, LogicalComponent<SystemImplementation> logical, Policy policy)
            throws GenerationException {
        SystemWireTargetDefinition wireDefinition = new SystemWireTargetDefinition();
        wireDefinition.setOptimizable(true);
        wireDefinition.setUri(service.getUri());
        return wireDefinition;
    }

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<SystemImplementation> source, LogicalResource<?> resource)
            throws GenerationException {
        URI uri = resource.getUri();
        SystemWireSourceDefinition wireDefinition = new SystemWireSourceDefinition();
        wireDefinition.setOptimizable(true);
        wireDefinition.setUri(uri);
        wireDefinition.setValueSource(new InjectableAttribute(InjectableAttributeType.RESOURCE, uri.getFragment()));

        return wireDefinition;
    }


}
