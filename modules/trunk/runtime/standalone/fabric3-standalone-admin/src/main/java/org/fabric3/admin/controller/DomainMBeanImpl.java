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
import org.fabric3.management.domain.DeploymentManagementException;
import org.fabric3.management.domain.DomainMBean;
import org.fabric3.management.domain.InvalidDeploymentException;
import org.fabric3.spi.domain.Domain;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.MetaDataStore;

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
        Contribution contribution = store.find(uri);
        if (contribution == null) {
            // FIXME
            throw new AssertionError("Invalid contribution");
        }
        for (Deployable deployable : contribution.getManifest().getDeployables()) {
            try {
                domain.include(deployable.getName());
            } catch (AssemblyException e) {
                monitor.error("Error deploying " + uri, e);
                List<String> errors = new ArrayList<String>();
                for (AssemblyFailure error : e.getErrors()) {
                    errors.add(error.getMessage());
                }
                throw new InvalidDeploymentException("Error deploying " + uri, errors);
            } catch (DeploymentException e) {
                reportError(uri, e);
            }
        }
    }

    public void deploy(URI uri, String plan) throws DeploymentManagementException {
        Contribution contribution = store.find(uri);
        if (contribution == null) {
            // FIXME
            throw new AssertionError("Invalid contribution");
        }
        for (Deployable deployable : contribution.getManifest().getDeployables()) {
            try {
                domain.include(deployable.getName(), plan);
            } catch (AssemblyException e) {
                monitor.error("Error deploying " + uri, e);
                List<String> errors = new ArrayList<String>();
                for (AssemblyFailure error : e.getErrors()) {
                    errors.add(error.getMessage());
                }
                throw new InvalidDeploymentException("Error deploying " + uri, errors);
            } catch (DeploymentException e) {
                reportError(uri, e);
            }

        }
    }

    private void reportError(URI uri, DeploymentException e) throws DeploymentManagementException {
        monitor.error("Error deploying " + uri, e);
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        throw new DeploymentManagementException(cause.getMessage());
    }

}
