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
package org.fabric3.binding.ejb.control;

import org.fabric3.binding.ejb.provision.EjbWireSourceDefinition;
import org.fabric3.binding.ejb.provision.EjbWireTargetDefinition;
import org.fabric3.binding.ejb.scdl.EjbBindingDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.Policy;
import org.osoa.sca.annotations.EagerInit;

/**
 * Implementation of the EJB binding generator.
 *
 * @version $Revision: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
@EagerInit
public class EjbBindingGenerator implements BindingGenerator<EjbWireSourceDefinition, EjbWireTargetDefinition, EjbBindingDefinition> {

    public EjbWireSourceDefinition generateWireSource(LogicalBinding<EjbBindingDefinition> logicalBinding,
                                                      Policy policy,
                                                      ServiceDefinition serviceDefinition)
        throws GenerationException {

        // TODO Pass the contract information to physical

        EjbWireSourceDefinition ewsd = new EjbWireSourceDefinition();
        ewsd.setUri(logicalBinding.getBinding().getTargetUri());
        ewsd.setBindingDefinition(logicalBinding.getBinding());
        ServiceContract<?> contract = serviceDefinition.getServiceContract();
        ewsd.setInterfaceName(contract.getQualifiedInterfaceName());
        ewsd.setClassLoaderId(logicalBinding.getParent().getParent().getClassLoaderId());

        return ewsd;
    }

    public EjbWireTargetDefinition generateWireTarget(LogicalBinding<EjbBindingDefinition> logicalBinding,
                                                      Policy policy,
                                                      ReferenceDefinition referenceDefinition)
        throws GenerationException {

        // TODO Pass the contract information to the physical

        EjbWireTargetDefinition ewtd = new EjbWireTargetDefinition();
        ewtd.setUri(logicalBinding.getBinding().getTargetUri());
        ewtd.setBindingDefinition(logicalBinding.getBinding());
        ServiceContract<?> contract = referenceDefinition.getServiceContract();
        ewtd.setInterfaceName(contract.getQualifiedInterfaceName());
        ewtd.setClassLoaderURI(logicalBinding.getParent().getParent().getClassLoaderId());

        return ewtd;
    }

}
