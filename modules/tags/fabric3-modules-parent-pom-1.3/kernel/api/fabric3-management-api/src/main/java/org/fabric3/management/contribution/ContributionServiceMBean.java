/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.management.contribution;

import java.net.URI;
import java.util.Set;

import org.fabric3.api.annotation.Management;

/**
 * MBean for managing contributions.
 *
 * @version $Rev$ $Date$
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
     * Returns the base address for installing profile artifacts in a domain.
     *
     * @return the base address
     */
    String getProfileServiceAddress();

    /**
     * Returns ContributionInfos for contributions in the domain.
     *
     * @return the URIs of contributions in the domain.
     */
    Set<ContributionInfo> getContributions();

    /**
     * Installs a stored contribution in the domain.
     *
     * @param uri the contribution URI
     * @throws ContributionInstallException if an error occurs during installation. Exception types may vary, for example, a contribution may be
     *                                      invalid. See subtypes for specifics.
     */
    void install(URI uri) throws ContributionInstallException;

    /**
     * Uninstalls a contribution.
     *
     * @param uri the contribution URI
     * @throws ContributionUninstallException if an error occurs during deinstallation.
     */
    void uninstall(URI uri) throws ContributionUninstallException;

    /**
     * Removes a stored contribution in the domain.
     *
     * @param uri the contribution URI
     * @throws ContributionRemoveException if an error occurs during removal.
     */
    void remove(URI uri) throws ContributionRemoveException;

    /**
     * Installs a stored profile in the domain.
     *
     * @param uri the contribution URI
     * @throws ContributionInstallException if an error occurs during installation. Exception types may vary, for example, a contribution may be
     *                                      invalid. See subtypes for specifics.
     */
    void installProfile(URI uri) throws ContributionInstallException;

    /**
     * Uninstalls a profile.
     *
     * @param uri the contribution URI
     * @throws ContributionUninstallException if an error occurs during deinstallation.
     */
    void uninstallProfile(URI uri) throws ContributionUninstallException;

    /**
     * Removes a stored profile in the domain.
     *
     * @param uri the contribution URI
     * @throws ContributionRemoveException if an error occurs during removal.
     */
    void removeProfile(URI uri) throws ContributionRemoveException;

}
