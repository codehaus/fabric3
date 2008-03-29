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
package org.fabric3.groovy.control;

import java.net.URI;

import org.fabric3.pojo.instancefactory.InstanceFactoryGenerationHelper;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectableAttributeType;
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
import org.fabric3.groovy.scdl.GroovyImplementation;
import org.fabric3.groovy.provision.GroovyComponentDefinition;
import org.fabric3.groovy.provision.GroovyInstanceFactoryDefinition;
import org.fabric3.groovy.provision.GroovyWireSourceDefinition;
import org.fabric3.groovy.provision.GroovyWireTargetDefinition;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

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
        URI componentId = component.getUri();
        GroovyComponentDefinition physical = new GroovyComponentDefinition();
        physical.setComponentId(componentId);
        physical.setGroupId(component.getParent().getUri());
        physical.setScope(type.getScope());
        physical.setInitLevel(helper.getInitLevel(definition, type));
        physical.setInstanceFactoryProviderDefinition(providerDefinition);
        helper.processPropertyValues(component, physical);
        // generate the classloader resource definition
        URI classLoaderId = component.getParent().getUri();
        physical.setClassLoaderId(classLoaderId);
        
        return physical;
    }

    public PhysicalWireSourceDefinition generateWireSource(LogicalComponent<GroovyImplementation> source,
                                                           LogicalReference reference,
                                                           Policy policy)
            throws GenerationException {
        URI uri = reference.getUri();
        ServiceContract<?> serviceContract = reference.getDefinition().getServiceContract();
        String interfaceName = serviceContract.getQualifiedInterfaceName();
        URI classLoaderId = source.getParent().getUri();

        GroovyWireSourceDefinition wireDefinition = new GroovyWireSourceDefinition();
        wireDefinition.setUri(uri);
        wireDefinition.setValueSource(new InjectableAttribute(InjectableAttributeType.REFERENCE, uri.getFragment()));
        wireDefinition.setInterfaceName(interfaceName);
        wireDefinition.setConversational(reference.getDefinition().getServiceContract().isConversational());
        // assume for now that any wire from a Groovy component can be optimized
        wireDefinition.setOptimizable(true);

        wireDefinition.setClassLoaderId(classLoaderId);
        return wireDefinition;
    }

    public PhysicalWireSourceDefinition generateCallbackWireSource(LogicalComponent<GroovyImplementation> source,
                                                                   ServiceContract<?> serviceContract,
                                                                   Policy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service,
                                                           LogicalComponent<GroovyImplementation> target, 
                                                           Policy policy)
            throws GenerationException {
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

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<GroovyImplementation> source, 
                                                                   LogicalResource<?> resource) throws GenerationException {
        GroovyWireSourceDefinition wireDefinition = new GroovyWireSourceDefinition();
        wireDefinition.setUri(resource.getUri());
        wireDefinition.setConversational(false);
        return wireDefinition;
    }
}
