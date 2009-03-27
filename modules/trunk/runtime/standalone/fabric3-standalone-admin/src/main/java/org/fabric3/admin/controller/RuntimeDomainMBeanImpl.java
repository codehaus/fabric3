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
package org.fabric3.admin.controller;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.domain.UndeploymentException;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.management.domain.ContributionNotFoundException;
import org.fabric3.management.domain.DeploymentManagementException;
import org.fabric3.management.domain.RuntimeDomainMBean;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.services.lcm.LogicalComponentManager;

/**
 * @version $Revision$ $Date$
 */
public class RuntimeDomainMBeanImpl extends AbstractDomainMBean implements RuntimeDomainMBean {
    private ContributionService contributionService;

    public RuntimeDomainMBeanImpl(@Reference(name = "domain") Domain domain,
                                  @Reference ContributionService contributionService,
                                  @Reference MetaDataStore store,
                                  @Reference LogicalComponentManager lcm,
                                  @Reference HostInfo info,
                                  @Monitor DomainMBeanMonitor monitor) {
        super(domain, store, lcm, info, monitor);
        this.contributionService = contributionService;
    }

    public void deployProfile(URI profileUri) throws DeploymentManagementException {
        List<URI> uris = contributionService.getContributionsInProfile(profileUri);
        try {
            for (Iterator<URI> it = uris.iterator(); it.hasNext();) {
                URI uri = it.next();
                Contribution contribution = store.find(uri);
                if (contribution.isLocked()) {
                    // only include contributions in the profile that were not previously deployed 
                    it.remove();
                }
            }
            domain.include(uris, false);
        } catch (DeploymentException e) {
            throw new DeploymentManagementException("Error deploying profile " + profileUri + ":" + e.getMessage());
        }
    }

    public void undeployProfile(URI uri) throws DeploymentManagementException {
        // the contributions must be undeployed by dependency
        List<URI> uris = contributionService.getSortedContributionsInProfile(uri);
        for (URI contributionUri : uris) {
            Contribution contribution = store.find(contributionUri);
            if (contribution == null) {
                throw new ContributionNotFoundException("Contribution not found: " + contributionUri);
            }
            for (Deployable deployable : contribution.getManifest().getDeployables()) {
                try {
                    domain.undeploy(deployable.getName());
                } catch (UndeploymentException e) {
                    reportError(contributionUri, e);
                }

            }
        }

    }


}