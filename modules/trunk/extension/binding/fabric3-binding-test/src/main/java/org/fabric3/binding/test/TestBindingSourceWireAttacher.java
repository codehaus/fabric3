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

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.SourceWireAttacherRegistry;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class TestBindingSourceWireAttacher implements SourceWireAttacher<TestBindingSourceDefinition> {
    private final SourceWireAttacherRegistry sourceWireAttacherRegistry;
    private final BindingChannel channel;

    public TestBindingSourceWireAttacher(@Reference SourceWireAttacherRegistry sourceWireAttacherRegistry,
                                         @Reference BindingChannel channel) {
        this.sourceWireAttacherRegistry = sourceWireAttacherRegistry;
        this.channel = channel;
    }

    @Init
    public void init() {
        sourceWireAttacherRegistry.register(TestBindingSourceDefinition.class, this);
    }

    @Destroy
    public void stop() {
        sourceWireAttacherRegistry.unregister(TestBindingSourceDefinition.class, this);
    }

    public void attachToSource(TestBindingSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        // register the wire to the bound service so it can be invoked through the channel from a bound reference
        URI callbackUri = target.getCallbackUri();
        channel.registerDestinationWire(source.getUri(), wire, callbackUri);
    }

    public void attachObjectFactory(TestBindingSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        throw new AssertionError();
    }
}
