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
package org.fabric3.federation.shoal;

import com.sun.enterprise.ee.cms.core.GroupManagementService;

/**
 * Responsible for managing communications between runtimes in a domain. Federated communications are segmented between domain-wide communications and
 * 1 to N zones. Implementations handle domain-wide communication as well communication within a particular zone.
 *
 * @version $Revision$ $Date$
 */
public interface FederationService {

    /**
     * Returns the domain name.
     *
     * @return the domain name
     */
    String getDomainName();

    /**
     * Returns the runtime name.
     *
     * @return the runtime name
     */
    String getRuntimeName();

    /**
     * Returns the underlying Shoal GMS for the domain.
     *
     * @return the underlying Shoal GMS for the domain
     */
    GroupManagementService getDomainGMS();

    /**
     * Registers a callback for messages destined to the given service.
     *
     * @param serviceName the service name
     * @param callback    the callback.
     */
    void registerCallback(String serviceName, FederationCallback callback);

}
