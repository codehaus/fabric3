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
package org.fabric3.binding.rmi.model.physical;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.binding.rmi.model.logical.RmiBindingDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.Policy;

@EagerInit
public class RmiBindingGenerator implements BindingGenerator<RmiWireSourceDefinition, RmiWireTargetDefinition, RmiBindingDefinition> {

    public RmiWireSourceDefinition generateWireSource(
            LogicalBinding<RmiBindingDefinition> logicalBinding,
            Policy policy,
            ServiceDefinition serviceDefinition)
            throws GenerationException {

        // TODO Pass the contract information to physical
        // TODO ignoring intents for now
        RmiWireSourceDefinition ewsd = new RmiWireSourceDefinition();
        ewsd.setUri(logicalBinding.getDefinition().getTargetUri());
        ewsd.setBindingDefinition(logicalBinding.getDefinition());
        ServiceContract<?> contract = serviceDefinition.getServiceContract();
        ewsd.setInterfaceName(contract.getQualifiedInterfaceName());

        URI classloaderId = logicalBinding.getParent().getParent().getClassLoaderId();
        ewsd.setClassLoaderId(classloaderId);
        return ewsd;

    }

    public RmiWireTargetDefinition generateWireTarget(
            LogicalBinding<RmiBindingDefinition> logicalBinding,
            Policy policy,
            ReferenceDefinition referenceDefinition)
            throws GenerationException {

        // TODO Pass the contract information to the physical

        RmiWireTargetDefinition ewtd = new RmiWireTargetDefinition();
        ewtd.setUri(logicalBinding.getDefinition().getTargetUri());
        ewtd.setBindingDefinition(logicalBinding.getDefinition());
        ServiceContract<?> contract = referenceDefinition.getServiceContract();
        ewtd.setInterfaceName(contract.getQualifiedInterfaceName());

        URI classloaderId = logicalBinding.getParent().getParent().getClassLoaderId();
        ewtd.setClassLoaderURI(classloaderId);
        return ewtd;

    }

}
