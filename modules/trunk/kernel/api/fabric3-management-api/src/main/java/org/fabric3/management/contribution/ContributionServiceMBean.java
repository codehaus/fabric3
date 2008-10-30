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
package org.fabric3.management.contribution;

import java.net.URI;
import java.util.Set;

import org.fabric3.api.annotation.Management;

/**
 * MBean for managing contributions.
 *
 * @version $Revision$ $Date$
 */
@Management
public interface ContributionServiceMBean {

    /**
     * Returns the base address for installing contribution artifacts in a domain.
     *
     * @return the base address
     */
    String getContributionServiceAddress();

    /**
     * Returns the URIs of contributions in the domain.
     *
     * @return the URIs of contributions in the domain.
     */
    Set<URI> getContributions();

    /**
     * Installs a stored contribution in the domain.
     *
     * @param uri the contribution URI
     * @throws ContributionManagementException
     *          if an error occurs during installation. Exception types may vary, for example, a contribution may be invalid. See subtypes for
     *          specifics.
     */
    void install(URI uri) throws ContributionManagementException;

    /**
     * Removes a stored contribution in the domain.
     *
     * @param uri the contribution URI
     * @throws ContributionManagementException
     *          if an error occurs during removal.
     */
    void remove(URI uri) throws ContributionManagementException;
}
