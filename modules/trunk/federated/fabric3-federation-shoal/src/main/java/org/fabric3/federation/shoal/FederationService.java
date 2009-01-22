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
 * Responsible for managing communications between runtimes in a domain.
 * <p/>
 * Federated communications are segmented between domain-wide communications and 1 to N zones. This service handles both domain-wide communication as
 * well communication within a particular zone.
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
     * Returns the name of the zone the runtime is a member of.
     *
     * @return the name of the zone the runtime is a member of
     */
    String getZoneName();

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
     * Returns the underlying Shoal GMS for the zone the runtime is a member of.
     *
     * @return the underlying Shoal GMS for the zone the runtime is a member of
     */
    GroupManagementService getZoneGMS();

    /**
     * Registers a callback for domain messages destined to the given service.
     *
     * @param serviceName the service name
     * @param callback    the callback.
     */
    void registerDomainCallback(String serviceName, FederationCallback callback);

    /**
     * Registers a callback for zone messages destined to the given service.
     *
     * @param serviceName the service name
     * @param callback    the callback.
     */
    void registerZoneCallback(String serviceName, FederationCallback callback);

}
