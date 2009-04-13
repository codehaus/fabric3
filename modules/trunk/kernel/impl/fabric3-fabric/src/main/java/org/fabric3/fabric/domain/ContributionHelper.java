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
package org.fabric3.fabric.domain;

import java.net.URI;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.host.RuntimeMode;
import org.fabric3.host.domain.CompositeAlreadyDeployedException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.model.type.component.Composite;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.plan.DeploymentPlan;

/**
 * Provides utility functions for working with contributions.
 *
 * @version $Revision$ $Date$
 */
public interface ContributionHelper {

    /**
     * Returns the list of deployable composites contained in the list of contributions that are configured to run in the specified runtime mode.
     *
     * @param contributions the contributions containing the deployables
     * @param runtimeMode   the runtime mode
     * @return the list of deployables
     */
    List<Composite> getDeployables(Set<Contribution> contributions, RuntimeMode runtimeMode);

    /**
     * Returns a list of deployment plans contained in the list of contributions.
     *
     * @param contributions the contributions plans
     * @return the deployment plans
     */
    List<DeploymentPlan> getDeploymentPlans(Set<Contribution> contributions);

    /**
     * Resolves a deployable by name.
     *
     * @param deployable the deployable name
     * @return the deployable
     * @throws DeploymentException if the deployable cannot be resolved
     */
    Composite resolveComposite(QName deployable) throws DeploymentException;

    /**
     * Resolves a deployment plan by name.
     *
     * @param plan the deployment plan name
     * @return the resolved deployment plan
     * @throws DeploymentException if the plan cannot be resolved
     */
    DeploymentPlan resolvePlan(String plan) throws DeploymentException;

    /**
     * Resolves the contributions from the list of URIs.
     *
     * @param uris the contribution  URIs
     * @return the set of contributions
     */
    Set<Contribution> resolveContributions(List<URI> uris);

    /**
     * Locks a set of contributions. The lock owners are the deployables in the contribution.
     *
     * @param contributions the cotnributions
     * @throws CompositeAlreadyDeployedException
     *          if a deployable is already deployed
     */
    void lock(Set<Contribution> contributions) throws CompositeAlreadyDeployedException;

    /**
     * Releases locks held on a set of contributions.
     *
     * @param contributions the contributions to release locks on
     */
    void releaseLocks(Set<Contribution> contributions);

}
