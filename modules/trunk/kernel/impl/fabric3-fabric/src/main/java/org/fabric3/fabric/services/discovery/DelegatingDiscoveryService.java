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
package org.fabric3.fabric.services.discovery;

import java.util.HashSet;
import java.util.Set;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.model.topology.RuntimeInfo;
import org.fabric3.spi.services.discovery.DiscoveryException;
import org.fabric3.spi.services.discovery.DiscoveryService;
import org.fabric3.spi.services.discovery.DiscoveryServiceRegistry;

/**
 * A DiscoveryService implementaton that delegates accross mutliple discovery protocols.
 *
 * @version $Rev$ $Date$
 */
public class DelegatingDiscoveryService implements DiscoveryService {
    private DiscoveryServiceRegistry registry;

    public DelegatingDiscoveryService(@Reference DiscoveryServiceRegistry registry) {
        this.registry = registry;
    }

    public Set<RuntimeInfo> getParticipatingRuntimes() {
        Set<RuntimeInfo> infos = new HashSet<RuntimeInfo>();
        for (DiscoveryService service : registry.getServices()) {
            infos.addAll(service.getParticipatingRuntimes());
        }
        return infos;
    }

    public RuntimeInfo getRuntimeInfo(String runtimeId) {
        for (DiscoveryService service : registry.getServices()) {
            RuntimeInfo info = service.getRuntimeInfo(runtimeId);
            if (info != null) {
                return info;
            }
        }
        return null;
    }

    public void joinDomain(long timeout) throws DiscoveryException {
        long elapsed;
        long start = System.currentTimeMillis();
        for (DiscoveryService service : registry.getServices()) {
            if (timeout > -1) {
                elapsed = System.currentTimeMillis() - start;
                if (elapsed > timeout) {
                    throw new DiscoveryTimeoutException("Timeout joining domain");
                }
                service.joinDomain(timeout - elapsed);

            } else {
                service.joinDomain(timeout);
            }
        }
    }
}