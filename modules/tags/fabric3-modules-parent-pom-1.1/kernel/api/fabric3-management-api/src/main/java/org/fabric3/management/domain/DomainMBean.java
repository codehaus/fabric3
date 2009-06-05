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
package org.fabric3.management.domain;

import java.net.URI;
import java.util.List;

import org.fabric3.api.annotation.Management;

/**
 * MBean for invoking domain operations.
 *
 * @version $Revision$ $Date$
 */
@Management
public interface DomainMBean {

    /**
     * Deploys a contribution to the domain.  All contained deployables will be included in the domain composite.
     *
     * @param uri the contribution URI.
     * @throws DeploymentManagementException if an exception deploying the contribution is encountered
     */
    void deploy(URI uri) throws DeploymentManagementException;


    /**
     * Deploys a contribution to the domain using the specified deployment plan.  All contained deployables will be included in the domain composite.
     *
     * @param uri  the contribution URI.
     * @param plan the deployment plan name
     * @throws DeploymentManagementException if an exception deploying the contribution is encountered
     */
    void deploy(URI uri, String plan) throws DeploymentManagementException;

    /**
     * Undeploys deployables contained in a contribution.
     *
     * @param uri the contribution URI.
     * @throws DeploymentManagementException if an exception undeploying the contribution is encountered
     */
    void undeploy(URI uri) throws DeploymentManagementException;

    /**
     * Returns a list of ComponentInfo instances representing the components deployed to the given composite path. The path "/" is interpreted as the
     * domain composite.
     *
     * @param path the path
     * @return the components
     * @throws InvalidPathException if the path is not found
     */
    List<ComponentInfo> getDeployedComponents(String path) throws InvalidPathException;

}
