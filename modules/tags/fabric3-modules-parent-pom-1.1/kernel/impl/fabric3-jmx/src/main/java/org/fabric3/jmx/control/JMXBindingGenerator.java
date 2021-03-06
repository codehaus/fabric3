/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.jmx.control;

import java.net.URI;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.jmx.provision.JMXWireSourceDefinition;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.type.JMXBinding;
import org.fabric3.spi.policy.Policy;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class JMXBindingGenerator implements BindingGenerator<JMXBinding> {

    public JMXWireSourceDefinition generateWireSource(LogicalBinding<JMXBinding> binding,
                                                      ServiceContract<?> contract,
                                                      List<LogicalOperation> operations,
                                                      Policy policy) throws GenerationException {
        Bindable logicalService = binding.getParent();

        JMXWireSourceDefinition definition = new JMXWireSourceDefinition();
        URI uri = binding.getUri();
        if (uri == null) {
            uri = logicalService.getUri();
        }
        definition.setUri(uri);
        definition.setInterfaceName(contract.getQualifiedInterfaceName());
        definition.setOptimizable(true);
        return definition;
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalBinding<JMXBinding> binding,
                                                           ServiceContract<?> contract,
                                                           List<LogicalOperation> operations,
                                                           Policy policy) throws GenerationException {

        // TODO we might need this for notifications but leave it out for now
        throw new UnsupportedOperationException();
    }
}