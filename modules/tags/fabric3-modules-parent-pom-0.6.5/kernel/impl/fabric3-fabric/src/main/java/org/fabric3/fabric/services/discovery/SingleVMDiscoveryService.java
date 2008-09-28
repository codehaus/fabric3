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

import java.util.Collections;
import java.util.Set;
import java.net.URI;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.model.topology.RuntimeInfo;
import org.fabric3.spi.services.discovery.DiscoveryService;
import org.fabric3.spi.services.runtime.RuntimeInfoService;

/**
 * A single node discovery service
 *
 * @version $Rev$ $Date$
 */
public class SingleVMDiscoveryService implements DiscoveryService {
    private RuntimeInfoService service;

    public SingleVMDiscoveryService(@Reference RuntimeInfoService service) {
        this.service = service;
    }

    public Set<RuntimeInfo> getParticipatingRuntimes() {
        return Collections.emptySet();
    }

    public RuntimeInfo getRuntimeInfo(URI runtimeId) {
        return service.getRuntimeInfo();
    }

    public void joinDomain(long timeout) {

    }
}
