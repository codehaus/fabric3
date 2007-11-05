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
import java.util.concurrent.ConcurrentHashMap;

import org.osoa.sca.ServiceRuntimeException;
import org.osoa.sca.ServiceUnavailableException;
import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class BindingChannelImpl implements BindingChannel {
    private Map<URI, Wire> wires = new ConcurrentHashMap<URI, Wire>();

    public void registerDestinationWire(URI uri, Wire wire) {
        wires.put(uri, wire);
    }

    public Message send(URI destination, String operation, Message msg) {
        Wire wire = wires.get(destination);
        if (wire == null) {
            throw new ServiceUnavailableException("No destination registered for [" + destination + "]");
        }
        InvocationChain chain = null;
        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            if (entry.getKey().getName().equals(operation)) {
                chain = entry.getValue();
            }
        }
        if (chain == null) {
            throw new ServiceRuntimeException("Operation on " + destination + " not found [" + operation + "]");
        }
        return chain.getHeadInterceptor().invoke(msg);
    }
}
