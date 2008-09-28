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
package org.fabric3.spi.services.discovery;

import java.util.Set;
import java.net.URI;

import org.fabric3.spi.model.topology.RuntimeInfo;

/**
 * Defines the abstraction for getting domain wide information of nodes
 * participating in the federated domain.
 *
 * @version $Revsion$ $Date$
 */
public interface DiscoveryService {

    /**
     * Returns information on the nodes participating in the same domain
     * as the current node. Each element in the returned list will
     * correspond to a federated runtime participating in the domain.
     *
     * @return List of runtimes participating in the domain.
     */
    Set<RuntimeInfo> getParticipatingRuntimes();

    /**
     * Gets the runtime info for the given runtime id.
     *
     * @param runtimeId Runtime id.
     * @return Runtime info.
     */
    RuntimeInfo getRuntimeInfo(URI runtimeId);

    void joinDomain(long timeout) throws DiscoveryException;

}
