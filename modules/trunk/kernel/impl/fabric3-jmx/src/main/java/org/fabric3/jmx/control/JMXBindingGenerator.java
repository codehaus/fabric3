/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.jmx.control;

import java.net.URI;

import org.fabric3.jmx.provision.JMXWireSourceDefinition;
import org.fabric3.jmx.scdl.JMXBinding;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.policy.Policy;

/**
 * @version $Rev$ $Date$
 */
public class JMXBindingGenerator implements BindingGenerator<JMXWireSourceDefinition, PhysicalWireTargetDefinition, JMXBinding> {

    public JMXWireSourceDefinition generateWireSource(LogicalBinding<JMXBinding> binding,
                                                      Policy policy,
                                                      ServiceDefinition serviceDefinition) throws GenerationException {
        LogicalComponent<?> logicalComponent = binding.getParent().getParent();

        JMXWireSourceDefinition definition = new JMXWireSourceDefinition();
        URI uri = binding.getUri();
        if (uri == null) {
            uri = logicalComponent.getUri();
        }
        definition.setUri(uri);
        definition.setClassLoaderId(logicalComponent.getParent().getUri());
        definition.setInterfaceName(serviceDefinition.getServiceContract().getQualifiedInterfaceName());
        definition.setOptimizable(true);
        return definition;
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalBinding<JMXBinding> binding,
                                                           Policy policy,
                                                           ReferenceDefinition referenceDefinition) throws GenerationException {
        // TODO we might need this for notifications but leave it out for now
        throw new UnsupportedOperationException();
    }
}