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
package org.fabric3.mock;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.policy.Policy;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class MockComponentGenerator implements ComponentGenerator<LogicalComponent<ImplementationMock>> {

    /**
     * Generates the component definition.
     */
    public MockComponentDefinition generate(LogicalComponent<ImplementationMock> component) throws GenerationException {

        MockComponentDefinition componentDefinition = new MockComponentDefinition();

        ImplementationMock implementationMock = component.getDefinition().getImplementation();
        MockComponentType componentType = implementationMock.getComponentType();

        componentDefinition.setInterfaces(implementationMock.getMockedInterfaces());

        componentDefinition.setScope(componentType.getScope());


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
