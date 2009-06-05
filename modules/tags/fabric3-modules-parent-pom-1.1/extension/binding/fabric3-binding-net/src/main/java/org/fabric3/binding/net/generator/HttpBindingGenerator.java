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
package org.fabric3.binding.net.generator;

import java.util.List;

import org.fabric3.binding.net.model.HttpBindingDefinition;
import org.fabric3.binding.net.provision.HttpWireSourceDefinition;
import org.fabric3.binding.net.provision.HttpWireTargetDefinition;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.policy.Policy;

/**
 * Generates source and target wire definitions for the HTTP binding.
 *
 * @version $Revision$ $Date$
 */
public class HttpBindingGenerator implements BindingGenerator<HttpBindingDefinition> {

    public PhysicalWireSourceDefinition generateWireSource(LogicalBinding<HttpBindingDefinition> binding,
                                                           ServiceContract<?> contract,
                                                           List<LogicalOperation> operations,
                                                           Policy policy) throws GenerationException {
        HttpWireSourceDefinition sourceDefinition = new HttpWireSourceDefinition();
        HttpBindingDefinition bindingDefinition = binding.getDefinition();
        sourceDefinition.setConfig(bindingDefinition.getConfig());
        sourceDefinition.setUri(bindingDefinition.getTargetUri());
        return sourceDefinition;
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalBinding<HttpBindingDefinition> binding,
                                                           ServiceContract<?> contract,
                                                           List<LogicalOperation> operations,
                                                           Policy policy) throws GenerationException {
        HttpWireTargetDefinition targetDefinition = new HttpWireTargetDefinition();
        HttpBindingDefinition bindingDefinition = binding.getDefinition();
        targetDefinition.setConfig(bindingDefinition.getConfig());
        targetDefinition.setUri(binding.getDefinition().getTargetUri());
        return targetDefinition;
    }
}
