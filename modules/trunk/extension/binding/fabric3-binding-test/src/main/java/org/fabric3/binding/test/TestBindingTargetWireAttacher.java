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
package org.fabric3.binding.test;

import java.net.URI;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class TestBindingTargetWireAttacher implements TargetWireAttacher<TestBindingTargetDefinition> {
    private final BindingChannel channel;

    public TestBindingTargetWireAttacher(@Reference BindingChannel channel) {
        this.channel = channel;
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, TestBindingTargetDefinition target, Wire wire) throws WiringException {
        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            URI destination;
            if (target.isCallback()) {
                destination = target.getCallbackUri();
            } else {
                destination = target.getUri();
            }
            String name = entry.getKey().getName();
            Interceptor interceptor = new TestBindingInterceptor(channel, destination, name);
            entry.getValue().addInterceptor(interceptor);
        }
    }

    public ObjectFactory<?> createObjectFactory(TestBindingTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }
}