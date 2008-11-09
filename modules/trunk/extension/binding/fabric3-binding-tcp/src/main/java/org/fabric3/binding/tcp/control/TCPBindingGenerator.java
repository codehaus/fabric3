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
package org.fabric3.binding.tcp.control;

import java.net.URI;

import org.fabric3.binding.tcp.provision.TCPWireSourceDefinition;
import org.fabric3.binding.tcp.provision.TCPWireTargetDefinition;
import org.fabric3.binding.tcp.scdl.TCPBindingDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.Policy;

/**
 * @version $Revision$ $Date$
 */
public class TCPBindingGenerator implements
        BindingGenerator<TCPWireSourceDefinition, TCPWireTargetDefinition, TCPBindingDefinition> {

    /**
     * {@inheritDoc}
     */
    public TCPWireSourceDefinition generateWireSource(LogicalBinding<TCPBindingDefinition> binding, Policy policy,
                                                      ServiceDefinition serviceDefinition) throws GenerationException {

        ServiceContract<?> serviceContract = serviceDefinition.getServiceContract();
        if (serviceContract.getOperations().size() != 1) {
            throw new GenerationException("Expects only one operation");
        }

        URI classLoaderId = binding.getParent().getParent().getParent().getUri();
        TCPWireSourceDefinition hwsd = new TCPWireSourceDefinition();
        hwsd.setClassLoaderId(classLoaderId);
        URI targetUri = binding.getDefinition().getTargetUri();
        hwsd.setUri(targetUri);

        return hwsd;

    }

    /**
     * {@inheritDoc}
     */
    public TCPWireTargetDefinition generateWireTarget(LogicalBinding<TCPBindingDefinition> binding, Policy policy,
                                                      ReferenceDefinition referenceDefinition)
            throws GenerationException {

        ServiceContract<?> serviceContract = referenceDefinition.getServiceContract();
        if (serviceContract.getOperations().size() != 1) {
            throw new GenerationException("Expects only one operation");
        }

        URI classLoaderId = binding.getParent().getParent().getParent().getUri();

        TCPWireTargetDefinition hwtd = new TCPWireTargetDefinition();
        hwtd.setClassLoaderId(classLoaderId);
        hwtd.setUri(binding.getDefinition().getTargetUri());

        return hwtd;

    }

}
