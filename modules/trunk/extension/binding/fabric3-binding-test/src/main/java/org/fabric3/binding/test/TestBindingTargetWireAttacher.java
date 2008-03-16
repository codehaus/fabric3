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
package org.fabric3.binding.test;

import java.net.URI;
import java.util.Map;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacherRegistry;
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
    private final TargetWireAttacherRegistry targetWireAttacherRegistry;
    private final BindingChannel channel;

    public TestBindingTargetWireAttacher(@Reference TargetWireAttacherRegistry targetWireAttacherRegistry,
                                         @Reference BindingChannel channel) {
        this.targetWireAttacherRegistry = targetWireAttacherRegistry;
        this.channel = channel;
    }

    @Init
    public void init() {
        targetWireAttacherRegistry.register(TestBindingTargetDefinition.class, this);
    }

    @Destroy
    public void stop() {
        targetWireAttacherRegistry.unregister(TestBindingTargetDefinition.class, this);
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