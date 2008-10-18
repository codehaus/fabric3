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
package org.fabric3.admin.api;

import java.net.URL;

/**
 * The interface for performing domain administrative functions.
 *
 * @version $Revision$ $Date$
 */
public interface DomainController {

    /**
     * Sets the base domain controller address.
     *
     * @param domain the domain controller address
     */
    void setControllerAddress(String domain);

    /**
     * Sets the username to authenticate with.
     *
     * @param username a valid domain admin username
     */
    void setUsername(String username);

    /**
     * Sets the password to authenticate with.
     *
     * @param password a valid domain admin password
     */
    void setPassword(String password);

    /**
     * Installs a contribution in the domain.
     *
     * @param contribution a URL pointing to the contribution artifact
     * @param name         the name to assign the contribution. Names must be unique in the domain.
     * @throws AdministrationException if an exception occurs executing the operation
     */
    void install(URL contribution, String name) throws AdministrationException;

    /**
     * Deploys a contribution.
     *
     * @param name the contribution name.
     * @throws AdministrationException if an exception occurs executing the operation
     */
    void deploy(String name) throws AdministrationException;

}
