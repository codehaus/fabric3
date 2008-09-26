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
package org.fabric3.fabric.services.discovery;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.model.topology.RuntimeInfo;
import org.fabric3.spi.services.discovery.DiscoveryException;
import org.fabric3.spi.services.discovery.DiscoveryService;
import org.fabric3.spi.services.discovery.DiscoveryServiceRegistry;
import org.fabric3.spi.services.runtime.RuntimeInfoService;

/**
 * A DiscoveryService implementaton that delegates accross mutliple discovery protocols.
 *
 * @version $Rev$ $Date$
 */
public class DelegatingDiscoveryService implements DiscoveryService {
    private DiscoveryServiceRegistry registry;
    private RuntimeInfoService runtimeInfoService;

    public DelegatingDiscoveryService(@Reference DiscoveryServiceRegistry registry,
                                      @Reference RuntimeInfoService runtimeInfoService) {
        this.registry = registry;
        this.runtimeInfoService = runtimeInfoService;
    }

    public Set<RuntimeInfo> getParticipatingRuntimes() {
        Set<RuntimeInfo> infos = new HashSet<RuntimeInfo>();
        for (DiscoveryService service : registry.getServices()) {
            infos.addAll(service.getParticipatingRuntimes());
        }
        return infos;
    }

    public RuntimeInfo getRuntimeInfo(URI runtimeId) {
        if (runtimeId == null) {
            // null runtime id denotes the current runtime
            return runtimeInfoService.getRuntimeInfo();
        }
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