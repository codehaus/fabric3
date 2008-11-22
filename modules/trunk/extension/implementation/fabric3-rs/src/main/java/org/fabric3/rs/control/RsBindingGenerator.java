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

import java.net.URI;
import org.fabric3.rs.scdl.RsBindingDefinition;
import org.fabric3.rs.provision.RsWireSourceDefinition;
import org.fabric3.rs.provision.RsWireTargetDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.Policy;
import org.osoa.sca.annotations.EagerInit;

/**
 * Implementation of the REST binding generator.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class RsBindingGenerator implements BindingGenerator<RsWireSourceDefinition, RsWireTargetDefinition, RsBindingDefinition> {

    public RsWireSourceDefinition generateWireSource(LogicalBinding<RsBindingDefinition> logicalBinding,
            Policy policy,
            ServiceDefinition serviceDefinition)
            throws GenerationException {

        RsWireSourceDefinition rwsd = new RsWireSourceDefinition();
        rwsd.setUri(logicalBinding.getDefinition().getTargetUri());
        rwsd.setInterfaceName(serviceDefinition.getServiceContract().getInterfaceName());
        rwsd.setIsResource(logicalBinding.getDefinition().isResource());
        rwsd.setIsProvider(logicalBinding.getDefinition().isProvider());


        return rwsd;

    }

    public RsWireTargetDefinition generateWireTarget(LogicalBinding<RsBindingDefinition> logicalBinding,
            Policy policy,
            ReferenceDefinition referenceDefinition)
            throws GenerationException {
        throw new GenerationException("Not supported");

    }
}
