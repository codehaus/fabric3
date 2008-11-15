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
package org.fabric3.host.domain;

import java.net.URI;
import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.scdl.Composite;

/**
 * Represents a domain.
 *
 * @version $Rev$ $Date$
 */
public interface Domain {

    /**
     * Include a deployable composite in the domain.
     *
     * @param deployable the name of the deployable composite to include
     * @throws DeploymentException if an error is encountered during inclusion
     */
    void include(QName deployable) throws DeploymentException;

    /**
     * Include a deployable composite in the domain using the specified DeploymentPlan.
     *
     * @param deployable the name of the deployable composite to include
     * @param plan       the deploymant plan name
     * @throws DeploymentException if an error is encountered during inclusion
     */
    void include(QName deployable, String plan) throws DeploymentException;

    /**
     * Include a deployable composite in the domain.
     *
     * @param deployable    the name of the deployable composite to include
     * @param transactional if true, the deployment operation will be done transactionally. That is, changes to the logical model will only be applied
     *                      after componnets have been deployed to a runtime or runtimes.
     * @throws DeploymentException if an error is encountered during inclusion
     */
    void include(QName deployable, boolean transactional) throws DeploymentException;

    /**
     * Include a deployable composite in the domain using the specified DeploymentPlan.
     *
     * @param deployable    the name of the deployable composite to include
     * @param plan          the deploymant plan name
     * @param transactional if true, the deployment operation will be done transactionally. That is, changes to the logical model will only be applied
     *                      after componnets have been deployed to a runtime or runtimes.
     * @throws DeploymentException if an error is encountered during inclusion
     */
    void include(QName deployable, String plan, boolean transactional) throws DeploymentException;

    /**
     * Include a Composite in the domain.
     *
     * @param composite the composite to include
     * @throws DeploymentException if an error is encountered during inclusion
     */
    void include(Composite composite) throws DeploymentException;

    /**
     * Include all deployables contained in the list of contributions in the domain. If deployment plans are present in the composites, they will be
     * used. This operation is intended for composites that are synthesized from multiple deployable composites that are associated with individual
     * deployment plans.
     *
     * @param uris          the contributions to deploy
     * @param transactional if true, the deployment operation will be done transactionally. That is, changes to the logical model will only be applied
     *                      after componnets have been deployed to a runtime or runtimes.
     * @throws DeploymentException if an error is encountered during inclusion
     */
    void include(List<URI> uris, boolean transactional) throws DeploymentException;

    /**
     * Remove a deployable Composite from the domain.
     *
     * @param deployable the name of the deployable composite to remove
     * @throws UndeploymentException if an error is encountered during undeployment
     */
    void undeploy(QName deployable) throws UndeploymentException;

    /**
     * Remove a deployable Composite from the domain.
     *
     * @param deployable    the name of the deployable composite to remove
     * @param transactional if true, the deployment operation will be done transactionally. That is, changes to the logical model will only be applied
     *                      after componnets have been deployed to a runtime or runtimes.
     * @throws UndeploymentException if an error is encountered during undeployment
     */
    void undeploy(QName deployable, boolean transactional) throws UndeploymentException;

}
