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
package org.fabric3.binding.test;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.Policy;

/**
 * Implementation of the test binding generator.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class TestBindingGenerator implements BindingGenerator<TestBindingSourceDefinition, TestBindingTargetDefinition, TestBindingDefinition> {

    public TestBindingSourceDefinition generateWireSource(LogicalBinding<TestBindingDefinition> logicalBinding,
                                                          Policy policy,
                                                          ServiceDefinition serviceDefinition)
            throws GenerationException {
        TestBindingSourceDefinition definition = new TestBindingSourceDefinition();
        definition.setUri(logicalBinding.getDefinition().getTargetUri());
        return definition;
    }

    public TestBindingTargetDefinition generateWireTarget(LogicalBinding<TestBindingDefinition> logicalBinding,
                                                          Policy policy,
                                                          ReferenceDefinition referenceDefinition)
            throws GenerationException {

        TestBindingTargetDefinition definition = new TestBindingTargetDefinition();
        definition.setUri(logicalBinding.getDefinition().getTargetUri());
        return definition;
    }


}
