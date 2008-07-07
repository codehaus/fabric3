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
package org.fabric3.mock;

import java.net.URI;

import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.policy.Policy;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class MockComponentGenerator implements ComponentGenerator<LogicalComponent<ImplementationMock>> {

    private final GeneratorRegistry registry;

    /**
     * Initializes the generator registry.
     *
     * @param registry Generator registry.
     */
    public MockComponentGenerator(@Reference GeneratorRegistry registry) {
        this.registry = registry;
    }

    /**
     * Registers with the generator registry.
     */
    @Init
    public void init() {
        registry.register(ImplementationMock.class, this);
    }

    /**
     * Generates the component definition.
     */
    public MockComponentDefinition generate(LogicalComponent<ImplementationMock> component) throws GenerationException {

        MockComponentDefinition componentDefinition = new MockComponentDefinition();

        ImplementationMock implementationMock = component.getDefinition().getImplementation();
        MockComponentType componentType = implementationMock.getComponentType();

        componentDefinition.setInterfaces(implementationMock.getMockedInterfaces());

        componentDefinition.setComponentId(component.getUri());
        componentDefinition.setScope(componentType.getScope());

        URI classLoaderId = component.getClassLoaderId();
        componentDefinition.setClassLoaderId(classLoaderId);

        return componentDefinition;

    }

    /**
     * Generates the wire target definition.
     */
    public MockWireTargetDefinition generateWireTarget(LogicalService service,
                                                       LogicalComponent<ImplementationMock> component,
                                                       Policy policy) throws GenerationException {

        MockWireTargetDefinition definition = new MockWireTargetDefinition();
        definition.setUri(service.getUri());
        URI classLoaderId = component.getClassLoaderId();
        definition.setClassLoaderId(classLoaderId);
        ServiceContract<?> serviceContract = service.getDefinition().getServiceContract();

        definition.setMockedInterface(serviceContract.getQualifiedInterfaceName());

        return definition;

    }

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<ImplementationMock> component,
                                                                   LogicalResource<?> resource) {
        throw new UnsupportedOperationException("Mock objects cannot have resources");
    }

    public PhysicalWireSourceDefinition generateWireSource(LogicalComponent<ImplementationMock> component,
                                                           LogicalReference reference,
                                                           Policy policy) {
        throw new UnsupportedOperationException("Mock objects cannot be source of a wire");
    }

    public PhysicalWireSourceDefinition generateCallbackWireSource(LogicalComponent<ImplementationMock> source,
                                                                   ServiceContract<?> serviceContract,
                                                                   Policy policy) throws GenerationException {
        return new MockWireSourceDefinition();
    }

}
