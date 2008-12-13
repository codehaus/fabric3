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
import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.domain.AssemblyException;
import org.fabric3.host.domain.AssemblyFailure;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.ContributionNotInstalledException;
import org.fabric3.host.domain.UndeploymentException;
import org.fabric3.host.domain.DomainException;
import org.fabric3.host.domain.CompositeAlreadyDeployedException;
import org.fabric3.host.domain.DeployableNotFoundException;
import org.fabric3.host.domain.Domain;
import org.fabric3.management.domain.ContributionNotFoundException;
import org.fabric3.management.domain.DeploymentManagementException;
import org.fabric3.management.domain.DomainMBean;
import org.fabric3.management.domain.InvalidDeploymentException;
import org.fabric3.management.domain.ContributionNotInstalledManagementException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;

/**
 * @version $Revision$ $Date$
 */
public class DomainMBeanImpl implements DomainMBean {
    private Domain domain;
    private MetaDataStore store;
    private DomainMBeanMonitor monitor;

    public DomainMBeanImpl(@Reference(name = "domain") Domain domain, @Reference MetaDataStore store, @Monitor DomainMBeanMonitor monitor) {
        this.domain = domain;
        this.store = store;
        this.monitor = monitor;
    }

    public void deploy(URI uri) throws DeploymentManagementException {
        deploy(uri, null);
    }

    public void deploy(URI uri, String plan) throws DeploymentManagementException {
        Contribution contribution = store.find(uri);
        if (contribution == null) {
            throw new ContributionNotFoundException("Contribution not found: " + uri);
        }
        for (Deployable deployable : contribution.getManifest().getDeployables()) {
            try {
                if (plan == null) {
                    domain.include(deployable.getName());
                } else {
                    domain.include(deployable.getName(), plan);
                }
            } catch (ContributionNotInstalledException e) {
                throw new ContributionNotInstalledManagementException(e.getMessage());
            } catch (AssemblyException e) {
                List<String> errors = new ArrayList<String>();
                for (AssemblyFailure error : e.getErrors()) {
                    errors.add(error.getMessage());
                }
                throw new InvalidDeploymentException("Error deploying " + uri, errors);
            } catch (CompositeAlreadyDeployedException e) {
                throw new ContributionNotInstalledManagementException(e.getMessage());
            } catch (DeployableNotFoundException e) {
                throw new ContributionNotInstalledManagementException(e.getMessage());
            } catch (DeploymentException e) {
                reportError(uri, e);
            }

        }
    }

    public void undeploy(URI uri) throws DeploymentManagementException {
        Contribution contribution = store.find(uri);
        if (contribution == null) {
            throw new ContributionNotFoundException("Contribution not found: " + uri);
        }
        for (Deployable deployable : contribution.getManifest().getDeployables()) {
            try {
                domain.undeploy(deployable.getName());
            } catch (UndeploymentException e) {
                reportError(uri, e);
            }

        }
    }

    private void reportError(URI uri, DomainException e) throws DeploymentManagementException {
        monitor.error("Error deploying " + uri, e);
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        throw new DeploymentManagementException(cause.getMessage());
    }

}
