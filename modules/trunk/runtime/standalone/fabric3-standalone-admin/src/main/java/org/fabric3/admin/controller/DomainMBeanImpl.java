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

import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.domain.AssemblyException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.management.domain.DomainMBean;
import org.fabric3.spi.domain.Domain;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.MetaDataStore;

/**
 * @version $Revision$ $Date$
 */
public class DomainMBeanImpl implements DomainMBean {
    private Domain domain;
    private MetaDataStore store;

    public DomainMBeanImpl(@Reference(name = "domain") Domain domain, @Reference MetaDataStore store) {
        this.domain = domain;
        this.store = store;
    }

    public void deploy(URI contributionUri) {
        Contribution contribution = store.find(contributionUri);
        if (contribution == null) {
            // FIXME
            throw new AssertionError("Invalid contribution");
        }
        for (Deployable deployable : contribution.getManifest().getDeployables()) {
            try {
                domain.include(deployable.getName());
            } catch (AssemblyException e) {
                e.printStackTrace();
            } catch (DeploymentException e) {
                // FIXME
                e.printStackTrace();
            }
        }
    }

    public void deploy(URI contributionUri, String plan) {
        Contribution contribution = store.find(contributionUri);
        if (contribution == null) {
            // FIXME
            throw new AssertionError("Invalid contribution");
        }
        for (Deployable deployable : contribution.getManifest().getDeployables()) {
            try {
                domain.include(deployable.getName(), plan);
            } catch (AssemblyException e) {
                e.printStackTrace();
            } catch (DeploymentException e) {
                // FIXME
                e.printStackTrace();
            }

        }
    }

}
