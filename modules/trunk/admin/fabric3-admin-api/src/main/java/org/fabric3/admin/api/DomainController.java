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

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Set;

/**
 * The interface for performing domain administrative functions.
 *
 * @version $Revision$ $Date$
 */
public interface DomainController {

    /**
     * Sets the base domain controller address.
     *
     * @param address the domain controller address
     */
    void setControllerAddress(String address);

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
     * Returns true if a connection to the domain controller is open.
     *
     * @return true if a connection to the domain controller is open
     */
    boolean isConnected();

    /**
     * Open a connection to the domain controller.
     *
     * @throws IOException if a connection cannot be established
     */
    void connect() throws IOException;

    /**
     * Closes an open connection to the domain controller.
     *
     * @throws IOException if there is an error closing the connection.
     */
    void disconnect() throws IOException;

    /**
     * Returns a set of installed contributions in the domain.
     *
     * @return the set of installed contributions.
     * @throws CommunicationException if there is an error communicating with the domain controller
     */
    public Set<URI> list() throws CommunicationException;

    /**
     * Installs a contribution in the domain.
     *
     * @param contribution a URL pointing to the contribution artifact
     * @param uri          the URI to assign the contribution.
     * @throws CommunicationException if there is an error communicating with the domain controller
     * @throws ContributionException  if there is an error installing the contribution. See InstallException subtypes for specific errors that may be
     *                                thrown.
     */
    void install(URL contribution, URI uri) throws CommunicationException, ContributionException;

    /**
     * Deploys all deployables in a contribution.
     *
     * @param uri the contribution uri.
     * @throws CommunicationException if there is an error communicating with the domain controller
     * @throws DeploymentException    if there is an error deploying the contribution. See InstallException subtypes for specific errors that may be
     *                                thrown.
     */
    void deploy(URI uri) throws CommunicationException, DeploymentException;

    /**
     * Deploys all deployables in a contribution.
     *
     * @param uri  the contribution URI.
     * @param plan the name of the deployment plan
     * @throws CommunicationException if there is an error communicating with the domain controller
     * @throws DeploymentException    if there is an error deploying the contribution. See InstallException subtypes for specific errors that may be
     *                                thrown.
     */
    void deploy(URI uri, String plan) throws CommunicationException, DeploymentException;

    /**
     * Removes a contribution from storage in a domain.
     *
     * @param uri the contribution URI
     * @throws CommunicationException if there is an error communicating with the domain controller
     * @throws ContributionException  if the is an error removing the contribution
     */
    void remove(URI uri) throws CommunicationException, ContributionException;

}
