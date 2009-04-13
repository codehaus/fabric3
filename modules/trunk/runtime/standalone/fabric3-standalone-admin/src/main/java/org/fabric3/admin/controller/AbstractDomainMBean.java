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
import javax.xml.namespace.QName;

import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.domain.AssemblyException;
import org.fabric3.host.domain.AssemblyFailure;
import org.fabric3.host.domain.CompositeAlreadyDeployedException;
import org.fabric3.host.domain.ContributionNotInstalledException;
import org.fabric3.host.domain.DeployableNotFoundException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.domain.DomainException;
import org.fabric3.host.domain.UndeploymentException;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.management.domain.ComponentInfo;
import org.fabric3.management.domain.ContributionNotFoundException;
import org.fabric3.management.domain.ContributionNotInstalledManagementException;
import org.fabric3.management.domain.DeploymentManagementException;
import org.fabric3.management.domain.InvalidDeploymentException;
import org.fabric3.management.domain.InvalidPathException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.services.lcm.LogicalComponentManager;

/**
 * @version $Revision$ $Date$
 */
public abstract class AbstractDomainMBean {
    protected Domain domain;
    protected MetaDataStore store;
    protected LogicalComponentManager lcm;
    protected HostInfo info;
    protected DomainMBeanMonitor monitor;
    protected String domainUri;

    public AbstractDomainMBean(Domain domain, MetaDataStore store, LogicalComponentManager lcm, HostInfo info, DomainMBeanMonitor monitor) {
        this.domain = domain;
        this.store = store;
        this.lcm = lcm;
        this.info = info;
        this.domainUri = info.getDomain().toString();
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
        try {
            domain.activateDefinitions(uri, true, false);
        } catch (DeploymentException e) {
            throw new ContributionNotInstalledManagementException(e.getMessage());
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
                    errors.add(error.getMessage() + " (" + error.getContributionUri() + ")");
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
        try {
            domain.deactivateDefinitions(uri, false);
        } catch (DeploymentException e) {
            throw new ContributionNotInstalledManagementException(e.getMessage());
        }
    }

    public List<ComponentInfo> getDeployedComponents(String path) throws InvalidPathException {
        String tokens[] = path.split("/");
        LogicalCompositeComponent currentComponent = lcm.getRootComponent();
        List<ComponentInfo> infos = new ArrayList<ComponentInfo>();
        String currentPath = domainUri;
        if (tokens.length > 0 && !domainUri.endsWith(tokens[0]) && !tokens[0].equals("/")) {
            throw new InvalidPathException("Path not found: " + path);
        }
        for (int i = 1; i < tokens.length; i++) {
            currentPath = currentPath + "/" + tokens[i];
            LogicalComponent<?> component = currentComponent.getComponent(URI.create(currentPath));
            if (component == null) {
                throw new InvalidPathException("Deployed composite not exist: " + path);
            } else if (!(component instanceof LogicalCompositeComponent)) {
                throw new InvalidPathException("Component is not a composite: " + path);
            } else {
                currentComponent = (LogicalCompositeComponent) component;
            }
        }
        for (LogicalComponent<?> component : currentComponent.getComponents()) {
            URI uri = component.getUri();
            URI contributionUri = component.getDefinition().getContributionUri();
            QName deployable = component.getDeployable();
            String zone = component.getZone();
            ComponentInfo info = new ComponentInfo(uri, contributionUri, deployable, zone);
            infos.add(info);
        }
        return infos;
    }

    protected void reportError(URI uri, DomainException e) throws DeploymentManagementException {
        monitor.error("Error deploying " + uri, e);
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        throw new DeploymentManagementException(cause.getMessage());
    }

}