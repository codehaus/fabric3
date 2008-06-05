/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
        URI id = logicalBinding.getParent().getParent().getParent().getUri();
        rwsd.setClassLoaderId(id);
        rwsd.setUri(logicalBinding.getBinding().getTargetUri());
        rwsd.setInterfaceName(serviceDefinition.getServiceContract().getInterfaceName());
        rwsd.setIsResource(logicalBinding.getBinding().isResource());
        rwsd.setIsProvider(logicalBinding.getBinding().isProvider());


        return rwsd;

    }

    public RsWireTargetDefinition generateWireTarget(LogicalBinding<RsBindingDefinition> logicalBinding,
            Policy policy,
            ReferenceDefinition referenceDefinition)
            throws GenerationException {
        throw new GenerationException("Not supported");

    }
}
