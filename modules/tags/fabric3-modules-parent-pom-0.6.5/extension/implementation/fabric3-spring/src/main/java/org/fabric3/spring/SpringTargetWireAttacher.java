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