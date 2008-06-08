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
package org.fabric3.spring;

import java.net.URI;
import java.util.Map;

import org.osoa.sca.annotations.Reference;

import org.fabric3.scdl.Signature;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.services.componentmanager.ComponentManager;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * The component builder for Spring implementation types. Responsible for creating the Component runtime artifact from a physical component
 * definition
 *
 * @version $Rev$ $Date$
 */
public class SpringTargetWireAttacher implements TargetWireAttacher<SpringWireTargetDefinition> {
    private final ComponentManager manager;

    private boolean debug = false;

    public SpringTargetWireAttacher(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition,
                               SpringWireTargetDefinition targetDefinition,
                               Wire wire) throws WireAttachException {
        URI targetName = UriHelper.getDefragmentedName(targetDefinition.getUri());
        Component component = manager.getComponent(targetName);
        assert component instanceof SpringComponent;
        SpringComponent<?> target = (SpringComponent) component;

        if (debug)
            System.out.println("##############in SpringTargetWireAttacher:attachToTarget" +
                    "; t.uri=" + targetDefinition.getUri() + "; targetName=" + targetName +
                    "; s.uri=" + sourceDefinition.getUri() + "; s.key=" + sourceDefinition.getKey() +
                    "; size=" + wire.getInvocationChains().entrySet().size());

        // attach the invoker interceptor to forward invocation chains
        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            PhysicalOperationDefinition operation = entry.getKey();

            Signature signature = new Signature(operation.getName(), operation.getParameters());

            if (debug)
                System.out.println("##############in SpringTargetWireAttacher operation=" + operation.getName());

            Interceptor targetInterceptor = new SpringTargetInterceptor(signature, target);
            InvocationChain chain = entry.getValue();
            chain.addInterceptor(targetInterceptor);
        }
    }

    public ObjectFactory<?> createObjectFactory(SpringWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }
}