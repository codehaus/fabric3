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
package org.fabric3.groovy;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.instancefactory.InstanceFactoryGenerationHelper;
import org.fabric3.pojo.processor.PojoComponentType;
import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.scdl.ComponentDefinition;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class GroovyComponentGenerator implements ComponentGenerator<LogicalComponent<GroovyImplementation>> {
    private final InstanceFactoryGenerationHelper helper;
    private final ClassLoaderGenerator classLoaderGenerator;

    public GroovyComponentGenerator(@Reference GeneratorRegistry registry,
                                    @Reference ClassLoaderGenerator classLoaderGenerator,
                                    @Reference InstanceFactoryGenerationHelper helper) {
        this.classLoaderGenerator = classLoaderGenerator;
        registry.register(GroovyImplementation.class, this);
        this.helper = helper;
    }

    public void generate(LogicalComponent<GroovyImplementation> component, GeneratorContext context)
            throws GenerationException {

        ComponentDefinition<GroovyImplementation> definition = component.getDefinition();
        GroovyImplementation implementation = definition.getImplementation();
        PojoComponentType type = implementation.getComponentType();

        // create the instance factory definition
        GroovyInstanceFactoryDefinition providerDefinition = new GroovyInstanceFactoryDefinition();
        providerDefinition.setInitMethod(type.getInitMethod());
        providerDefinition.setDestroyMethod(type.getDestroyMethod());
        providerDefinition.setImplementationClass(implementation.getClassName());
        providerDefinition.setScriptName(implementation.getScriptName());
        helper.processConstructorArguments(type.getConstructorDefinition(), providerDefinition);
        helper.processConstructorSites(type, providerDefinition);
        helper.processReferenceSites(type, providerDefinition);
        // TODO process callbacks
        // TODO process properties

        // create the physical component definition
        URI componentId = component.getUri();
        GroovyComponentDefinition physical = new GroovyComponentDefinition();
        physical.setComponentId(componentId);
        physical.setGroupId(componentId.resolve("."));
        physical.setScope(type.getImplementationScope());
        physical.setInitLevel(helper.getInitLevel(definition, type));
        physical.setInstanceFactoryProviderDefinition(providerDefinition);
        helper.processProperties(physical, definition);
        // generate the classloader resource definition
        URI classLoaderId = classLoaderGenerator.generate(component, context);
        physical.setClassLoaderId(classLoaderId);
        context.getPhysicalChangeSet().addComponentDefinition(physical);
    }

    public PhysicalWireSourceDefinition generateWireSource(LogicalComponent<GroovyImplementation> source,
                                                           LogicalReference reference,
                                                           boolean optimizable,
                                                           GeneratorContext context)
            throws GenerationException {
        GroovyWireSourceDefinition wireDefinition = new GroovyWireSourceDefinition();
        wireDefinition.setUri(reference.getUri());
        wireDefinition.setOptimizable(optimizable);
        wireDefinition.setConversational(reference.getDefinition().getServiceContract().isConversational());
        return wireDefinition;
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service,
                                                           LogicalComponent<GroovyImplementation> target,
                                                           GeneratorContext context)
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
}
