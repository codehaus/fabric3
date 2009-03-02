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

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.junit.provision.JUnitWireSourceDefinition;
import org.fabric3.junit.scdl.JUnitBindingDefinition;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.policy.Policy;

/**
 * Attaches wires to Junit components to the WireHolder.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class JUnitBindingGenerator implements BindingGenerator<JUnitWireSourceDefinition, PhysicalWireTargetDefinition, JUnitBindingDefinition> {

    public JUnitWireSourceDefinition generateWireSource(LogicalBinding<JUnitBindingDefinition> bindingDefinition,
                                                        Policy policy,
                                                        ServiceDefinition serviceDefinition) throws GenerationException {
        ComponentDefinition<?> definition = bindingDefinition.getParent().getParent().getDefinition();
        String testName = definition.getName();
        return new JUnitWireSourceDefinition(testName);
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalBinding<JUnitBindingDefinition> bindingDefinition,
                                                           Policy policy,
                                                           ServiceContract<?> contract) throws GenerationException {
        throw new UnsupportedOperationException();
    }
}
