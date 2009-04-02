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
package org.fabric3.rs.control;

import java.util.List;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.rs.provision.RsWireSourceDefinition;
import org.fabric3.rs.provision.RsWireTargetDefinition;
import org.fabric3.rs.scdl.RsBindingDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.policy.Policy;

/**
 * Implementation of the REST binding generator.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class RsBindingGenerator implements BindingGenerator<RsBindingDefinition> {

    public RsWireSourceDefinition generateWireSource(LogicalBinding<RsBindingDefinition> logicalBinding,
                                                     ServiceContract<?> contract,
                                                     List<LogicalOperation> operations,
                                                     Policy policy) throws GenerationException {

        RsWireSourceDefinition rwsd = new RsWireSourceDefinition();
        rwsd.setUri(logicalBinding.getDefinition().getTargetUri());
        rwsd.setInterfaceName(contract.getInterfaceName());
        rwsd.setIsResource(logicalBinding.getDefinition().isResource());
        rwsd.setIsProvider(logicalBinding.getDefinition().isProvider());


        return rwsd;

    }

    public RsWireTargetDefinition generateWireTarget(LogicalBinding<RsBindingDefinition> logicalBinding,
                                                     ServiceContract<?> contract,
                                                     List<LogicalOperation> operations,
                                                     Policy policy) throws GenerationException {
        throw new GenerationException("Not supported");

    }
}
